package previewcode.backend.api.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import previewcode.backend.DTO.*;
import previewcode.backend.database.DatabaseInterpreter;
import previewcode.backend.services.FirebaseService;
import previewcode.backend.services.GithubService;
import previewcode.backend.services.IDatabaseService;
import previewcode.backend.services.actiondsl.ActionDSL;
import previewcode.backend.services.actiondsl.Interpreter;

import javax.inject.Inject;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("v1/webhook")
public class WebhookAPI {
    private static  final Logger logger = LoggerFactory.getLogger(WebhookAPI.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private static final String GITHUB_WEBHOOK_EVENT_HEADER = "X-GitHub-Event";
    private static final String GITHUB_WEBHOOK_DELIVERY_HEADER = "X-GitHub-Delivery";

    private static final Response BAD_REQUEST = Response.status(Response.Status.BAD_REQUEST).build();
    private static final Response OK = Response.ok().build();

    private final IDatabaseService databaseService;
    private final Interpreter interpreter;


    @Inject
    private GithubService githubService;

    @Inject
    private FirebaseService firebaseService;

    @Inject
    public WebhookAPI(IDatabaseService databaseService, DatabaseInterpreter interpreter) {
        this.databaseService = databaseService;
        this.interpreter = interpreter;
    }

    @POST
    public Response onWebhookPost(
            String postData,
            @HeaderParam(GITHUB_WEBHOOK_EVENT_HEADER) String eventType,
            @HeaderParam(GITHUB_WEBHOOK_DELIVERY_HEADER) String delivery)
            throws Exception {

        logger.info("Receiving Webhook call {" + delivery + "} for event {" + eventType + "}");

        // Respond to different webhook events
        if (eventType.equals("pull_request")) {
            JsonNode body = mapper.readTree(postData);
            String action = body.get("action").asText();
            logger.info("Handling `"+ action +"` pull request...");

            if (action.equals("opened")) {
                Pair<GitHubRepository, GitHubPullRequest> repoAndPull = readRepoAndPullFromWebhook(body);
                PRComment comment = new PRComment(constructMarkdownComment(repoAndPull.first, repoAndPull.second));
                OrderingStatus pendingStatus = new OrderingStatus(repoAndPull.second, repoAndPull.first);

                firebaseService.addDefaultData(new PullRequestIdentifier(repoAndPull.first, repoAndPull.second));

                githubService.placePullRequestComment(repoAndPull.second, comment);
                githubService.setOrderingStatus(repoAndPull.second, pendingStatus);

                //Add hunks to database
                Diff diff = githubService.fetchDiff(repoAndPull.second);
                OrderingGroup defaultGroup = new OrderingGroup("Default group", "Default group", diff.getHunkChecksums());
                ActionDSL.Action<Unit> groupAction = databaseService.insertDefaultGroup(new PullRequestIdentifier(repoAndPull.first, repoAndPull.second), defaultGroup);
                interpreter.evaluateToResponse(groupAction);

            } else if (action.equals("synchronize")) {
                Pair<GitHubRepository, GitHubPullRequest> repoAndPull = readRepoAndPullFromWebhook(body);
                OrderingStatus pendingStatus = new OrderingStatus(repoAndPull.second, repoAndPull.first);
                githubService.setOrderingStatus(repoAndPull.second, pendingStatus);
            }
        } else if (eventType.equals("pull_request_review")) {
            // Respond to a review event
            return OK;
        } else if (eventType.equals("pull_request_review_comment")) {
            // Respond to a review comment or line comment event
            return OK;
        } else {
            // We'll also receive events related to issues which we do not need.
            logger.info("Did not recognize Webhook event.");
            return BAD_REQUEST;
        }
        return OK;
    }

    private String constructMarkdownComment(GitHubRepository repo, GitHubPullRequest pullRequest) {
        return "This pull request can be reviewed with [Preview Code](" + pullRequest.previewCodeUrl(repo) + ").\n" +
               "To speed up the review process and get better feedback on your changes, " +
               "please **[order your changes](" + pullRequest.previewCodeUrl(repo) + ").**\n";
    }

    private Pair<GitHubRepository, GitHubPullRequest> readRepoAndPullFromWebhook(JsonNode body) throws JsonProcessingException {
        GitHubRepository repo = mapper.treeToValue(body.get("repository"), GitHubRepository.class);
        GitHubPullRequest pullRequest = mapper.treeToValue(body.get("pull_request"), GitHubPullRequest.class);
        return new Pair<>(repo, pullRequest);
    }

    class Pair<A, B> {
        public final A first;
        public final B second;

        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }
    }
}

