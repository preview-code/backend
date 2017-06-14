package previewcode.backend.api.v2;

import io.atlassian.fugue.Unit;
import previewcode.backend.DTO.ApproveRequest;
import previewcode.backend.DTO.ApprovedPullRequest;
import previewcode.backend.DTO.PullRequestIdentifier;
import previewcode.backend.services.IDatabaseService;
import previewcode.backend.services.actiondsl.Interpreter;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static previewcode.backend.services.actiondsl.ActionDSL.*;

@Path("v2/{owner}/{name}/pulls/{number}/")
public class ApprovalsAPI {

    private final Interpreter interpreter;
    private final IDatabaseService databaseService;

    @Inject
    public ApprovalsAPI(Interpreter interpreter, IDatabaseService databaseService) {
        this.interpreter = interpreter;
        this.databaseService = databaseService;
    }

    @Path("getApprovals")
    @GET
    public Response getApprovals(@PathParam("owner") String owner,
                                 @PathParam("name") String name,
                                 @PathParam("number") Integer number) throws Exception {
        PullRequestIdentifier pull = new PullRequestIdentifier(owner, name, number);
        Action<ApprovedPullRequest> action = databaseService.getApproval(pull);
        return interpreter.evaluateToResponse(action);
    }

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
    @Path("setApprove")
    @POST
    public Response setApprove(@PathParam("owner") String owner,
                              @PathParam("name") String name,
                              @PathParam("number") Integer number,
                              ApproveRequest body) throws Exception {
//        GHMyself user = githubService.getLoggedInUser();
//        if (body.githubLogin != user.getId()) {
//            throw new IllegalArgumentException("Can not set status status of other user");
//        }
//        firebaseService.setApproved(owner, name, number, body);

        PullRequestIdentifier pull = new PullRequestIdentifier(owner, name, number);
        Action<Unit> action = databaseService.setApproval(pull, body);
        return interpreter.evaluateToResponse(action);
    }
}
