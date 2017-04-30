package previewcode.backend.api.v1;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import previewcode.backend.DTO.Approve;
import previewcode.backend.services.FirebaseService;

import com.google.inject.Inject;
import previewcode.backend.services.GithubService;
import org.kohsuke.github.GHMyself;

import java.io.IOException;

/**
 * API endpoint for approving hunks
 *
 */
@Path("v1/{owner}/{name}/pulls/{number}/approve")
public class AssigneesAPI {

    @Inject
    private FirebaseService firebaseService;

    @Inject
    private GithubService githubService;

    /**
     * Creates a pull request
     * 
     * @param owner
     *            The owner of the repository on which the pull request is created
     * @param name
     *            The owner of the repository on which the pull request is created
     * @param body
     *            The body of the pull request
     * @param number
     *            The number of the pull request
     * @return The number of the newly made pull request
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Approve setApprove(@PathParam("owner") String owner,
                              @PathParam("name") String name,
                              @PathParam("number") String number,
                              Approve body) throws IOException {
        GHMyself user = githubService.getLoggedInUser();
        if (body.githubLogin != user.getId()) {
            throw new IllegalArgumentException("Can not set approve status of other user");
        }
        firebaseService.setApproved(owner, name, number, body);
        return body;
    }
}
