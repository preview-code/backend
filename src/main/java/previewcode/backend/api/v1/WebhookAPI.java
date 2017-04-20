package previewcode.backend.api.v1;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import previewcode.backend.DTO.WebhookPullRequest;
import previewcode.backend.DTO.WebhookRepo;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Calendar;
import java.util.Date;


@Path("webhook/")
public class WebhookAPI {

    @Inject
    private Algorithm jwtSigningAlgorithm;

    private static final String INTEGRATION_ID = "2112";
    private static final RequestBody EMPTY_REQUEST_BODY = RequestBody.create(null, new byte[]{});
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    @POST
    public void onWebhookPost(String postData) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        JsonNode body = mapper.readTree(postData);

        if (body.get("action").asText().equals("opened")) {
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
            Response response = OK_HTTP_CLIENT.newCall(request).execute();
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
}
