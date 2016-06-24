package me.previewcode.backend.services;

import java.io.IOException;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import me.previewcode.backend.DTO.PRComment;
import me.previewcode.backend.DTO.PRbody;
import me.previewcode.backend.DTO.PrNumber;

import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

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
            return number;
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
}
