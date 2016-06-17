package me.previewcode.backend.services.github;

import java.io.IOException;

import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import me.previewcode.backend.GithubConnection;
import me.previewcode.backend.DTO.PRComment;
import me.previewcode.backend.DTO.PRGroupComment;
import me.previewcode.backend.services.firebase.FirebaseCommentsService;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Service for placing comments on GitHub
 */
@Singleton
public class GithubCommentsService extends GithubConnection {

    @Inject
    private FirebaseCommentsService pullService;

    @Inject
    public GithubCommentsService(@Named("github.user") Provider<GitHub> gitHubProvider)
            throws IOException {
        super(gitHubProvider);
    }

    /**
     * A method to set the standard pull request comments
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
    public GHIssueComment setGroupComment(String owner, String name,
            int number, PRGroupComment comment) {

        GHIssueComment newComment = postComment(owner, name, number, comment);
        pullService.setComments(owner, name, number, newComment.getId(),
                comment.groupID);

        return newComment;
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
