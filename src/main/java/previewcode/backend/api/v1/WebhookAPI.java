package previewcode.backend.api.v1;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.commons.codec.binary.Hex;
import previewcode.backend.DTO.WebhookPullRequest;
import previewcode.backend.DTO.WebhookRepo;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.Date;


@Path("webhook/")
public class WebhookAPI {

    @Inject
    private Algorithm jwtSigningAlgorithm;

    @Inject
    @Named("github.webhook.secret")
    private SecretKeySpec webhookSecret;

    private static final String INTEGRATION_ID = "2150";
    private static final RequestBody EMPTY_REQUEST_BODY = RequestBody.create(null, new byte[]{});
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String GITHUB_WEBHOOK_EVENT_HEADER = "X-GitHub-Event";
    private static final String GITHUB_WEBHOOK_SECRET_HEADER = "X-Hub-Signature";

    private static final Response BAD_REQUEST = Response.status(Response.Status.BAD_REQUEST).build();
    private static final Response UNAUTHORIZED = Response.status(Response.Status.UNAUTHORIZED).build();
    private static final Response OK = Response.ok().build();

    @POST
    public Response onWebhookPost(String postData,
                                  @HeaderParam(GITHUB_WEBHOOK_EVENT_HEADER) String eventType,
                                  @HeaderParam(GITHUB_WEBHOOK_SECRET_HEADER) String hash)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {

        // Check the GitHub secret
        final Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(webhookSecret);
        final String expectedHash = Hex.encodeHexString(mac.doFinal(postData.getBytes()));
        if (!hash.equals("sha1="+ expectedHash)) {
            return UNAUTHORIZED;
        }

        // Respond to different webhook events
        if (eventType.equals("pull_request")) {
            JsonNode body = mapper.readTree(postData);
            if (body.get("action").asText().equals("opened")) {
                handleNewPullRequest(body);
            }
        } else if (eventType.equals("pull_request_review")) {
            // Respond to a review event
            return OK;
        } else if (eventType.equals("pull_request_review_comment")) {
            // Respond to a review comment or line comment event
            return OK;
        } else {
            // We'll also receive events related to issues which we do not need.
            return BAD_REQUEST;
        }
        return OK;
    }

    private void handleNewPullRequest(JsonNode body) throws IOException {
        String installationId = body.get("installation").get("id").asText();

        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        calendar.add(Calendar.MINUTE, 10);
        Date exp = calendar.getTime();

        String token = JWT.create()
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .withIssuer(INTEGRATION_ID)
                .sign(jwtSigningAlgorithm);

        Request request = new Request.Builder()
                .url("https://api.github.com/installations/" + installationId + "/access_tokens")
                .addHeader("Accept", "application/vnd.github.machine-man-preview+json")
                .addHeader("Authorization", "Bearer " + token)
                .post(EMPTY_REQUEST_BODY)
                .build();
        okhttp3.Response response = OK_HTTP_CLIENT.newCall(request).execute();
        String installationToken = mapper.readValue(response.body().string(), JsonNode.class)
                .get("token").asText();

        WebhookRepo repo = mapper.treeToValue(body.get("repository"), WebhookRepo.class);
        WebhookPullRequest editedPull = mapper.treeToValue(body.get("pull_request"), WebhookPullRequest.class)
                .addPreviewCodeSignature(repo);

        RequestBody editPullBody = RequestBody.create(MediaType.parse("application/json"), mapper.writeValueAsString(editedPull));

        Request editPull = new Request.Builder()
                .url(editedPull.url)
                .addHeader("Accept", "application/vnd.github.machine-man-preview+json")
                .addHeader("Authorization", "token " + installationToken)
                .patch(editPullBody)
                .build();

        OK_HTTP_CLIENT.newCall(editPull).execute();
    }
}
