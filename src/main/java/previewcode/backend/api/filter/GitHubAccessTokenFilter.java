package previewcode.backend.api.filter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.google.inject.Key;
import com.google.inject.name.Names;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GitHub;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

@Provider
@PreMatching
public class GitHubAccessTokenFilter implements ContainerRequestFilter {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();

    private static final String TOKEN_PARAMETER = "access_token";
    private static final String CURRENT_USER_NAME = "github.user";
    private static final String CURRENT_INSTALLATION_TOKEN = "github.installation.token";
    private static final String CURRENT_USER_TOKEN = "github.user.token";

    private static final String GITHUB_WEBHOOK_USER_AGENT_PREFIX = "GitHub-Hookshot/";
    private static final String GITHUB_WEBHOOK_SECRET_HEADER = "X-Hub-Signature";

    private static final String INTEGRATION_ID = "2150";
    private static final String TEST_INTEGRATION_ID = "2152";

    private static final Response UNAUTHORIZED = Response.status(Response.Status.UNAUTHORIZED).build();
    private static final RequestBody EMPTY_REQUEST_BODY = RequestBody.create(null, new byte[]{});

    @Inject
    private Algorithm jwtSigningAlgorithm;

    @Inject
    @Named("github.webhook.secret")
    private SecretKeySpec webhookSecret;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        checkForOAuthToken(containerRequestContext);
        checkForWehbook(containerRequestContext);
    }

    /**
     * This method checks whether the request originates from a GitHub Webhook call.
     * Is so, the call is verified against a shared webhook secret.
     * When the call is verified to originate from GitHub,
     * a request is made to receive a fresh Installation token.
     * This token is then bound to `github.installation.token` for usage with @Inject and @Named.
     *
     * Aborts the pending request with a 401 Unauthorized error if verification of the shared secret fails.
     *
     * @throws IOException when the token cannot be requested from GitHub
     */
    private void checkForWehbook(ContainerRequestContext context) throws IOException {
        String userAgent = context.getHeaderString("User-Agent");
        if (userAgent != null && userAgent.startsWith(GITHUB_WEBHOOK_USER_AGENT_PREFIX)) {
            String requestBody = IOUtils.toString(context.getEntityStream(), "UTF-8");
            try {
                verifyGitHubWebhookSecret(context, requestBody);
            } catch (Exception e) {
                context.abortWith(UNAUTHORIZED);
                return;
            }
            String installationToken = getGitHubInstallationToken(requestBody);
            context.setProperty(Key.get(String.class, Names.named(CURRENT_INSTALLATION_TOKEN)).toString(), installationToken);
        }
    }

    /**
     * Verify that the incomming request originates from the GitHub webhook servers.
     *
     * @throws NoSuchAlgorithmException when HMAC SHA1 is not available.
     * @throws InvalidKeyException if the webhook secret is inappropriate for initializing the MAC.
     * @throws NotAuthorizedException when the received hash is invalid or missing.
     */
    private void verifyGitHubWebhookSecret(ContainerRequestContext context, String requestBody)
            throws NoSuchAlgorithmException, InvalidKeyException, NotAuthorizedException {
        String receivedHash = context.getHeaderString(GITHUB_WEBHOOK_SECRET_HEADER);
        if (receivedHash != null && receivedHash.startsWith("sha1=")) {
            final Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(webhookSecret);
            final String expectedHash = Hex.encodeHexString(mac.doFinal(requestBody.getBytes()));
            if (!receivedHash.equals("sha1="+ expectedHash)) {
                throw new NotAuthorizedException("The received MAC does not match the configured MAC");
            }
        } else {
            throw new NotAuthorizedException("Expected to find an MAC hash in " + GITHUB_WEBHOOK_SECRET_HEADER);
        }
    }

    /**
     * Authenticate against the GitHub Integrations API and fetch a token for the current Installation.
     *
     * @return A fresh authorization token for the installation making the current request.
     * @throws IOException when the request body cannot be read or when the call to GitHub fails.
     */
    private String getGitHubInstallationToken(String requestBody) throws IOException {
        JsonNode body = mapper.readTree(requestBody);
        String installationId = body.get("installation").get("id").asText();

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.MINUTE, 10);
        Date exp = calendar.getTime();

        String token = JWT.create()
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .withIssuer(TEST_INTEGRATION_ID)
                .sign(jwtSigningAlgorithm);

        Request request = new Request.Builder()
                .url("https://api.github.com/installations/" + installationId + "/access_tokens")
                .addHeader("Accept", "application/vnd.github.machine-man-preview+json")
                .addHeader("Authorization", "Bearer " + token)
                .post(EMPTY_REQUEST_BODY)
                .build();
        okhttp3.Response response = OK_HTTP_CLIENT.newCall(request).execute();
        return mapper.readValue(response.body().string(), JsonNode.class)
                .get("token").asText();
    }

    /**
     * Checks whether the `access_token` query parameter is present.
     * This token is used to authenticate app users with OAuth.
     *
     * This method aborts the pending request with a 401 Unauthorized error
     * if the token is invalid.
     *
     * If the token is valid, the token will be bound to `github.user.token`, and
     * the GitHub object will be bound to `github.user`. These bindings can be used
     * with Guice @Inject and @Named annotations.
     *
     * @throws IOException when unable to connect to GitHub with the provided token.
     */
    private void checkForOAuthToken(ContainerRequestContext context) throws IOException {
        final MultivaluedMap<String, String> parameters = context.getUriInfo()
                .getQueryParameters();
        final String token = parameters.getFirst(TOKEN_PARAMETER);

        if (!Strings.isNullOrEmpty(token)) {
            try {
                final GitHub user = GitHub.connectUsingOAuth(token);
                context.setProperty(Key.get(GitHub.class, Names.named(CURRENT_USER_NAME)).toString(), user);
                context.setProperty(Key.get(String.class, Names.named(CURRENT_USER_TOKEN)).toString(), token);
            } catch (final NotAuthorizedException e) {
                context.abortWith(UNAUTHORIZED);
            }
        }
    }
}
