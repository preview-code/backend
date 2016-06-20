package me.previewcode.backend.api.v1;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import me.previewcode.backend.DTO.StatusBody;
import me.previewcode.backend.services.firebase.StatusService;

import com.google.inject.Inject;

/**
 * API endpoint for the status of a pull request
 */
@Path("{owner}/{name}/pulls/{branch}/status/")
public class StatusAPI {

    @Inject
    private StatusService statusService;

    /**
     * Sets the status of a pull request
     * 
     * @param owner
     *            The owner of the repository where the pull request is located
     * @param name
     *            The owner of the repository where the pull request is located
     * @param number
     *            The number of the pull request
     * @param data
     *            The body in which the status is saved
     * @return The body of the status
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public StatusBody setStatus(@PathParam("owner") String owner,
            @PathParam("name") String name, @PathParam("branch") String number,
            StatusBody body) {
        statusService.setStatus(owner.toLowerCase(), name.toLowerCase(),
                number, body.status);

        return body;
    }
}
