package me.previewcode.backend.api.v1;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import me.previewcode.backend.DTO.Approve;
import me.previewcode.backend.services.firebase.AssigneesService;

import com.google.inject.Inject;

@Path("{owner}/{name}/pulls/{number}/approve")
public class AssigneesAPI {

    @Inject
    private AssigneesService assigneesService;

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
    public Approve setApprove (@PathParam("owner") String owner,
            @PathParam("name") String name,@PathParam("number") String number, Approve body) throws IOException {
            assigneesService.setLGTM(owner, name, number, body);
        
        return body;
    }
}
