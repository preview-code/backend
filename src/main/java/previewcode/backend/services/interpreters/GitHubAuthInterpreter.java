package previewcode.backend.services.interpreters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Key;
import com.google.inject.name.Names;
import io.atlassian.fugue.Unit;
import okhttp3.CacheControl;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.codec.binary.Hex;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import previewcode.backend.DTO.GitHubUser;
import previewcode.backend.api.exceptionmapper.NotAuthorizedException;
import previewcode.backend.api.filter.GitHubAccessTokenFilter;
import previewcode.backend.api.filter.IJWTTokenCreator;
import previewcode.backend.services.http.IHttpRequestExecutor;
import previewcode.backend.services.GithubService;
import previewcode.backend.services.actiondsl.ActionCache;
import previewcode.backend.services.actiondsl.CachingInterpreter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.container.ContainerRequestContext;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import static previewcode.backend.services.actiondsl.ActionDSL.unit;
import static previewcode.backend.services.actions.GitHubActions.*;

public class GitHubAuthInterpreter extends CachingInterpreter {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String GITHUB_WEBHOOK_USER_AGENT_PREFIX = "GitHub-Hookshot/";
    private static final Logger logger = LoggerFactory.getLogger(GitHubAccessTokenFilter.class);
    private static final String CURRENT_USER_NAME = "github.user";
    private static final String CURRENT_TOKEN_BUILDER = "github.token.builder";
    private static final RequestBody EMPTY_REQUEST_BODY = RequestBody.create(null, new byte[]{});
    private static final String GH_MAN_MACHINE_PREVIEW_HEADER = "application/vnd.github.machine-man-preview+json";


    private final SecretKeySpec sharedSecret;
    private final IJWTTokenCreator jwtTokenCreator;
    private final IHttpRequestExecutor http;
    private final String integrationId;

    // Ugly, should be gone when dynamic binding to @Named injections is not necessary anymore.
    public ContainerRequestContext context;

    @Inject
    public GitHubAuthInterpreter(
            @Named("integration.id") String integrationId,
            @Named("github.webhook.secret") SecretKeySpec sharedSecret,
            IJWTTokenCreator jwtTokenCreator,
            IHttpRequestExecutor http,
            ActionCache cache) {
        super(cache);
        this.integrationId = integrationId;
        this.sharedSecret = sharedSecret;
        this.jwtTokenCreator = jwtTokenCreator;
        this.http = http;

        on(IsWebHookUserAgent.class).apply(this::isWebHook);
        on(VerifyWebhookSharedSecret.class).apply(this::verifySharedSecret);
        on(GetUser.class).apply(this::fetchUser);
        on(AuthenticateInstallation.class).apply(this::authInstallation);
    }

    public static ActionCache.Builder configure(ActionCache.Builder b) {
        return b.expire(GetUser.class).afterWrite(15, TimeUnit.MINUTES)
                .expire(AuthenticateInstallation.class).afterWrite(1, TimeUnit.HOURS);
    }

    protected Unit authInstallation(AuthenticateInstallation action) throws IOException {
        String token = jwtTokenCreator.create(integrationId);

        logger.info("Authenticating installation {" + action.installationID + "} as integration {" + integrationId + "}");
        String installationToken = this.authenticateInstallation(action.installationID.id, token);

        GithubService.TokenBuilder builder = (Request.Builder request) ->
                    request.header("Authorization", "token " + installationToken)
                        .addHeader("Accept", GH_MAN_MACHINE_PREVIEW_HEADER);

        context.setProperty(Key.get(GithubService.TokenBuilder.class, Names.named(CURRENT_TOKEN_BUILDER)).toString(), builder);
        return unit;
    }

    private String authenticateInstallation(String installationId, String integrationToken) throws IOException {
        Request request = new Request.Builder()
                .url("https://api.github.com/installations/" + installationId + "/access_tokens")
                .addHeader("Accept", GH_MAN_MACHINE_PREVIEW_HEADER)
                .addHeader("Authorization", "Bearer " + integrationToken)
                .post(EMPTY_REQUEST_BODY)
                .build();

        String response = http.execute(request);
        return MAPPER.readValue(response, JsonNode.class).get("token").asText();
    }

    protected GitHubUser fetchUser(GetUser action) throws RuntimeException, IOException {
        Request r = new Request.Builder()
                .addHeader("Authorization", "token " + action.token.token)
                .url("https://api.github.com/user")
                .cacheControl(new CacheControl.Builder()
                        .maxAge(15, TimeUnit.MINUTES)
                        .build())
                .get().build();

        logger.info("Authenticating user via OAuth");
        authViaOldApi(action);

        GithubService.TokenBuilder builder = (Request.Builder request) ->
                request.header("Authorization", "token " + action.token.token);

        context.setProperty(Key.get(GithubService.TokenBuilder.class, Names.named(CURRENT_TOKEN_BUILDER)).toString(), builder);

        return fromJson(http.execute(r), GitHubUser.class);
    }

    protected void authViaOldApi(GetUser action) throws IOException {
        GitHub gitHub = GitHub.connectUsingOAuth(action.token.token);
        context.setProperty(Key.get(GitHub.class, Names.named(CURRENT_USER_NAME)).toString(), gitHub);
    }

    protected Unit verifySharedSecret(VerifyWebhookSharedSecret action) {
        if (action.sha1 != null && action.sha1.startsWith("sha1=")) {
            try {
                Mac mac = Mac.getInstance("HmacSHA1");
                mac.init(sharedSecret);
                final String expectedHash = Hex.encodeHexString(mac.doFinal(action.requestBody.getBytes()));
                if (!action.sha1.equals("sha1="+ expectedHash)) {
                    throw new NotAuthorizedException("MAC verification failed");
                }
            } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                throw new RuntimeException("Error instantiating HmacSHA1.", e);
            }
        } else {
            throw new NotAuthorizedException("Missing or invalid MAC header.");
        }
        return unit;
    }

    protected Boolean isWebHook(IsWebHookUserAgent action) {
        return action.userAgent != null && action.userAgent.startsWith(GITHUB_WEBHOOK_USER_AGENT_PREFIX);
    }

    private <T> T fromJson(String body, Class<T> destClass) {
        try {
            return MAPPER.readValue(body, destClass);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
