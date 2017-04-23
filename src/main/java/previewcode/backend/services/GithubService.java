package previewcode.backend.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHMyself;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHPullRequestReviewComment;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import previewcode.backend.DTO.GitHubPullRequest;
import previewcode.backend.DTO.GitHubStatus;
import previewcode.backend.DTO.OrderingStatus;
import previewcode.backend.DTO.PRComment;
import previewcode.backend.DTO.PRLineComment;
import previewcode.backend.DTO.PRbody;
import previewcode.backend.DTO.PrNumber;
import previewcode.backend.DTO.PullRequestIdentifier;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * An abstract class that connects with github
 *
 */
@Singleton
public class GithubService {

    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();


    @Inject
    @Named("github.token.builder")
    private TokenBuilder tokenBuilder;

    /**
     * The GitHub provider.
     */
    protected Provider<GitHub> githubProvider;

    /**
     * Making a connection with GitHub
     *
     * @param gitHubProvider
     *            The provider for GitHub data
     * @throws IOException
     *
     */
    @Inject
    protected GithubService(@Named("github.user") final Provider<GitHub> gitHubProvider)
            throws IOException {
        githubProvider = gitHubProvider;
    }

    public GHMyself getLoggedInUser() throws IOException {
        return this.githubProvider.get().getMyself();
    }

    /**
     * A method to create a pull request
     *
     * @param owner
     *            The owner of the repository on which the pull request is
     *            created
     * @param name
     *            The owner of the repository on which the pull request is
     *            created
     * @param body
     *            The body of the pull request
     * @return The number of the newly made pull request
     */
    public PrNumber createPullRequest(String owner, String name, PRbody body) {
        try {
            PrNumber number = new PrNumber();
            GHRepository repo = this.githubProvider.get().getRepository(
                    owner.toLowerCase() + "/" + name.toLowerCase());
            GHPullRequest pr = repo.createPullRequest(body.title, body.head,
                    body.base, body.description);
            number.number = pr.getNumber();
            if (body.metadata) {
                pr.setBody(body.description + "\n\n---\n" +
                        "Review this pull request [on Preview Code](https://preview-code.com/projects/" +
                        owner + "/" + name + "/pulls/" + number.number + "/overview).");
            }
            return number;
        } catch (IOException e) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                JsonNode response = mapper.readTree(e.getMessage());
                JsonNode errors = response.get("errors");
                if (errors.isArray()) {
                    String error = errors.get(0).get("message").asText();
                    throw new IllegalArgumentException(error);
                }
            } catch (IOException ignored) {
            }
            throw new IllegalArgumentException("Unable to process request");
        }
    }

    /**
     * Posts the comment to GitHub
     *
     * @param owner
     *            The owner of the repository where the comment is placed
     * @param name
     *            The name of the repository where the comment is placed
     * @param number
     *            The number of the pull request
     * @param comment
     *            The comment that is placed.
     * @return The comment to be displayed in the frontend
     */
    public GHIssueComment postComment(String owner, String name, int number,
            PRComment comment) {
                try {
                    GHRepository repo = this.githubProvider.get().getRepository(
                            owner.toLowerCase() + "/" + name.toLowerCase());
                    GHPullRequest pr = repo.getPullRequest(number);
                    return pr.comment(comment.body);
                } catch (IOException e) {
                    throw new IllegalArgumentException(e);
                }
            }


    /**
     * Posts the comment to GitHub
     *
     * @param owner
     *            The owner of the repository where the comment is placed
     * @param name
     *            The name of the repository where the comment is placed
     * @param number
     *            The number of the pull request
     * @param comment
     *            The comment that is placed.
     * @return The comment to be displayed in the frontend
     */
    public GHPullRequestReviewComment postLineComment(String owner, String name, int number,
                                      PRLineComment comment) {
        try {
            GHRepository repo = this.githubProvider.get().getRepository(
                    owner.toLowerCase() + "/" + name.toLowerCase());
            GHPullRequest pr = repo.getPullRequest(number);
            return pr.createReviewComment(comment.body, comment.sha, comment.path, comment.position);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Checks if user is owner of pullrequest
     * @param owner
     *      The owner of the base repository
     * @param name
     *      The name of the base repository
     * @param number
     *      The number of the pull request
     * @return
     *     if the user is the owner
     */
    public Boolean isOwner(String owner, String name, int number){
        try {
            GHRepository repo = this.githubProvider.get().getRepository(
                    owner.toLowerCase() + "/" + name.toLowerCase());

            return repo.getPullRequest(number).getUser().getLogin().equals(this.getLoggedInUser().getLogin());
        }
        catch  (IOException e){
            return false;
        }

    }

    /**
     * GET a pull request from GitHub.
     *
     * @param identifier The identifier object containing owner, name and number of the pull to fetch.
     * @throws IOException when the request fails
     */
    public GitHubPullRequest fetchPullRequest(PullRequestIdentifier identifier) throws IOException {
        Request getPull = tokenBuilder.addToken(new Request.Builder())
                .url(identifier.toGitHubURL())
                .get()
                .build();
        Response response = OK_HTTP_CLIENT.newCall(getPull).execute();
        return fromJson(response, GitHubPullRequest.class);
    }


    /**
     * Sends a request to GitHub to place a comment at the given pull request.
     *
     * @param pullRequest The PR to place the comment on
     * @param comment The comment to place
     * @throws IOException when the request fails
     */
    public void placePullRequestComment(GitHubPullRequest pullRequest, PRComment comment) throws IOException {
        Request postComment = tokenBuilder.addToken(new Request.Builder())
                .url(pullRequest.links.comments)
                .post(toJson(comment))
                .build();

        OK_HTTP_CLIENT.newCall(postComment).execute();
    }

    /**
     * Send a request to GitHub to set the status on the `ordering` context.
     * @param pullRequest The pull request to set the status on.
     * @param status The status to set.
     * @throws IOException when the request fails
     */
    public void setOrderingStatus(GitHubPullRequest pullRequest, OrderingStatus status) throws IOException {
        Request createStatus = tokenBuilder.addToken(new Request.Builder())
                .url(pullRequest.links.statuses)
                .post(toJson(status))
                .build();

        OK_HTTP_CLIENT.newCall(createStatus).execute();
    }

    public Optional<OrderingStatus> getOrderingStatus(GitHubPullRequest pullRequest) throws IOException {
        Request getStatus = tokenBuilder.addToken(new Request.Builder())
                .url(pullRequest.links.statuses)
                .get()
                .build();

        Response response = OK_HTTP_CLIENT.newCall(getStatus).execute();
        return fromJson(response, new TypeReference<List<GitHubStatus>>(){}).stream()
                .map(OrderingStatus::fromGitHubStatus)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    private RequestBody toJson(Object value) throws JsonProcessingException {
        return RequestBody.create(MediaType.parse("application/json"), mapper.writeValueAsString(value));
    }

    private <T> T fromJson(Response response, TypeReference<T> typeReference) throws IOException {
        return mapper.readValue(response.body().string(), typeReference);
    }

    private <T> T fromJson(Response response, Class<T> destClass) throws IOException {
        return mapper.readValue(response.body().string(), destClass);
    }

    public interface TokenBuilder {
        Request.Builder addToken(Request.Builder builder);
    }

}
