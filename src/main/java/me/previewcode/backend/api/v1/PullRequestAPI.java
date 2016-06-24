package me.previewcode.backend.api.v1;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import me.previewcode.backend.DTO.PRbody;
import me.previewcode.backend.DTO.PrNumber;
import me.previewcode.backend.DTO.StatusBody;
import me.previewcode.backend.services.FirebaseService;
import me.previewcode.backend.services.GithubService;

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
            @PathParam("name") String name, PRbody body){
        PrNumber number = githubService.createPullRequest(owner, name, body);
        firebaseService.setOrdering(owner, name, number, body.ordering);       
        StatusBody statusBody = new StatusBody();
        statusBody.status = "No reviewer assigned";
        firebaseService.setStatus(owner, name,
                Integer.toString(number.number), statusBody.status);    
        return number;
    }
}
