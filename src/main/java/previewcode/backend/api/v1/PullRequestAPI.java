package previewcode.backend.api.v1;

import com.google.inject.Inject;
import previewcode.backend.DTO.GitHubPullRequest;
import previewcode.backend.DTO.Ordering;
import previewcode.backend.DTO.OrderingStatus;
import previewcode.backend.DTO.PRbody;
import previewcode.backend.DTO.PrNumber;
import previewcode.backend.DTO.PullRequestIdentifier;
import previewcode.backend.DTO.StatusBody;
import previewcode.backend.services.FirebaseService;
import previewcode.backend.services.GithubService;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Path("{owner}/{name}/pulls/")
public class PullRequestAPI {

    @Inject
    private GithubService githubService;

    @Inject
    private FirebaseService firebaseService;


    /**
     * Creates a pull request
     * 
     * @param owner
     *            The owner of the repository on which the pull request is created
     * @param name
     *            The owner of the repository on which the pull request is created
     * @param body
     *            The body of the pull request
     * @return The number of the newly made pull request
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public PrNumber createPR(@PathParam("owner") String owner,
                             @PathParam("name") String name, PRbody body) {
        if (body.title.isEmpty() || body.description.isEmpty()) {
            throw new IllegalArgumentException("Title or body is empty");
        }
        PrNumber number = githubService.createPullRequest(owner, name, body);
        firebaseService.setOrdering(new PullRequestIdentifier(owner, name, number.number), body.ordering);
        StatusBody statusBody = new StatusBody();
        statusBody.status = "No reviewer assigned";
        firebaseService.setStatus(owner, name,
                Integer.toString(number.number), statusBody.status);
        return number;
    }


    /**
     * Updates the ordering
     *
     * @param owner
     *            The owner of the repository where the pull request was made
     * @param name
     *            The owner of the repository where the pull request was made
     * @param number
     *            The number of the pull request
     * @param body
     *            The new ordering of the pull request
     */
    @POST
    @Path("{number}/ordering")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateOrdering(@PathParam("owner") String owner,
                             @PathParam("name") String name,
                             @PathParam("number") Integer number, List<Ordering> body) throws IOException {
        if(githubService.isOwner(owner, name,  number)) {
            firebaseService.setOrdering(new PullRequestIdentifier(owner, name, number), body);
            updateOrderingStatus(owner, name, number);
        }
    }

    /**
     * Get the current ordering status from GitHub and change it to `success`
     * if the status is present.
     * @throws IOException when a call to GitHub fails.
     */
    private void updateOrderingStatus(String owner, String name, Integer number) throws IOException {
        PullRequestIdentifier id = new PullRequestIdentifier(owner, name, number);
        GitHubPullRequest pull = githubService.fetchPullRequest(id);
        githubService.getOrderingStatus(pull)
                .filter(OrderingStatus::isPending)
                .map(OrderingStatus::complete)
                .ifPresent(status -> { try {
                    githubService.setOrderingStatus(pull, status);
                } catch (IOException e) {
                    throw new UncheckedIOException(e.getMessage(), e); }
                });
    }
}
