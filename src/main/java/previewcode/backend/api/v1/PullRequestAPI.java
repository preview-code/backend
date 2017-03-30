package previewcode.backend.api.v1;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import previewcode.backend.DTO.Ordering;
import previewcode.backend.DTO.PRbody;
import previewcode.backend.DTO.PrNumber;
import previewcode.backend.DTO.StatusBody;
import previewcode.backend.services.FirebaseService;
import previewcode.backend.services.GithubService;

import com.google.inject.Inject;

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
        firebaseService.setOrdering(owner, name, number, body.ordering);
        StatusBody statusBody = new StatusBody();
        statusBody.status = "No reviewer assigned";
        firebaseService.setStatus(owner, name,
                Integer.toString(number.number), statusBody.status);
        return number;
    }

     /**
     * Checks if there is information stored of this pull request
     *    If not, add the data via a default template
     *
     * @param owner
     *          The owner of the repository on which the pull request is created
     * @param name
     *          The owner of the repository on which the pull request is created
     * @param number
     *          The number of the pull request
     * @param ordering
     *          The ordering of the pull request
     */
    @POST
    @Path("{branch}/check")
    @Consumes(MediaType.APPLICATION_JSON)
    public void isInformationPresent(@PathParam("owner") String owner,
                                     @PathParam("name") String name, @PathParam("branch") String number,
                                     Ordering ordering) {
        if (!ordering.diff.isEmpty()) {
            firebaseService.addDefaultData(owner.toLowerCase(), name.toLowerCase(),
                    number, ordering);
        }
    }
}
