package previewcode.backend.services;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import previewcode.backend.DTO.PRComment;
import previewcode.backend.DTO.PRLineComment;
import previewcode.backend.DTO.PRbody;
import previewcode.backend.DTO.PrNumber;

import org.kohsuke.github.*;

/**
 * An abstract class that connects with github
 *
 */
@Singleton
public class GithubService {

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
}
