package me.previewcode.backend.services.github;

import java.io.IOException;

import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import me.previewcode.backend.GithubConnection;
import me.previewcode.backend.DTO.PRbody;
import me.previewcode.backend.DTO.PrNumber;
import me.previewcode.backend.DTO.StatusBody;
import me.previewcode.backend.services.firebase.StatusService;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * The service for creating a pull request on GitHub
 *
 */
@Singleton
public class PullRequestService extends GithubConnection {

    @Inject
    public PullRequestService(@Named("github.user") Provider<GitHub> gitHubProvider)
            throws IOException {
        super(gitHubProvider);
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
        PrNumber number = new PrNumber();
        try {
            GHRepository repo = this.githubProvider.get().getRepository(
                    owner.toLowerCase() + "/" + name.toLowerCase());
            GHPullRequest pr = repo.createPullRequest(body.title, body.head,
                    body.base, body.description);
            StatusBody statusBody = new StatusBody();
            statusBody.status = "No reviewer assigned";
            StatusService statusService = new StatusService();
            statusService.setStatus(repo.getOwnerName(), repo.getName(),
                    Integer.toString(pr.getNumber()), statusBody.status);
            number.number = pr.getNumber();
            return number;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
