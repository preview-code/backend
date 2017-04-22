package previewcode.backend.api.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import previewcode.backend.DTO.GitHubPullRequest;
import previewcode.backend.DTO.GitHubRepository;
import previewcode.backend.DTO.OrderingStatus;
import previewcode.backend.DTO.PRComment;
import previewcode.backend.services.GithubService;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


@Path("webhook/")
public class WebhookAPI {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String GITHUB_WEBHOOK_EVENT_HEADER = "X-GitHub-Event";

    private static final Response BAD_REQUEST = Response.status(Response.Status.BAD_REQUEST).build();
    private static final Response OK = Response.ok().build();

    @Inject
    private GithubService githubService;

    @POST
    public Response onWebhookPost(String postData, @HeaderParam(GITHUB_WEBHOOK_EVENT_HEADER) String eventType)
            throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException {

        // Respond to different webhook events
        if (eventType.equals("pull_request")) {
            JsonNode body = mapper.readTree(postData);

            if (body.get("action").asText().equals("edited")) {
                GitHubRepository repo = mapper.treeToValue(body.get("repository"), GitHubRepository.class);
                GitHubPullRequest pullRequest = mapper.treeToValue(body.get("pull_request"), GitHubPullRequest.class);

                PRComment comment = new PRComment(constructMarkdownComment(repo, pullRequest));
                OrderingStatus pendingStatus = new OrderingStatus(pullRequest, repo);
                githubService.placePullRequestComment(pullRequest, comment);
                githubService.setOrderingStatus(pullRequest, pendingStatus);
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

    private String constructMarkdownComment(GitHubRepository repo, GitHubPullRequest pullRequest) {
        return "This pull request can be reviewed with [on Preview Code](https://preview-code.com/projects/" + repo.fullName + "/pulls/" + pullRequest.number + ").\n" +
               "To speed up the review process and get better feedback on your changes, " +
               "please **[order your changes](" + pullRequest.previewCodeUrl(repo) + ").**\n";
    }
}

