package previewcode.backend.api.v1;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import previewcode.backend.DTO.StatusBody;
import previewcode.backend.services.FirebaseService;

import com.google.inject.Inject;

/**
 * API endpoint for the status of a pull request
 */
@Path("{owner}/{name}/pulls/{branch}/status/")
public class StatusAPI {

    @Inject
    private FirebaseService firebaseService;

    /**
     * Sets the status of a pull request
     * 
     * @param owner
     *            The owner of the repository where the pull request is located
     * @param name
     *            The owner of the repository where the pull request is located
     * @param number
     *            The number of the pull request
     * @param body
     *            The body in which the status is saved
     * @return The body of the status
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public StatusBody setStatus(@PathParam("owner") String owner,
            @PathParam("name") String name, @PathParam("branch") String number,
            StatusBody body) {
        firebaseService.setStatus(owner.toLowerCase(), name.toLowerCase(),
                number, body.status);

        return body;
    }
}
