package previewcode.backend.api.v2;

import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import previewcode.backend.DTO.*;
import previewcode.backend.services.IDatabaseService;
import previewcode.backend.services.actiondsl.Interpreter;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static previewcode.backend.services.actiondsl.ActionDSL.*;

/**
 * API for getting and setting the approvals on a pullrequest
 */
@Path("v2/{owner}/{name}/pulls/{number}/")
public class ApprovalsAPI {

    private final Interpreter interpreter;
    private final IDatabaseService databaseService;

    @Inject
    public ApprovalsAPI(Interpreter interpreter, IDatabaseService databaseService) {
        this.interpreter = interpreter;
        this.databaseService = databaseService;
    }

    /**
     * Fetches all approvals and shows if pr/groups/hunks are (dis)approved
     * @param owner     The owner of the repository
     * @param name      The name of the repository
     * @param number    The pullrequest number
     * @return if the pullrequest and the groups and hunks are disapproved or approved
     */
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
     * Fetches all approvals and shows per hunk whom (dis)approved
     * @param owner     The owner of the repository
     * @param name      The name of the repository
     * @param number    The pullrequest number
     * @return per hunk the approval status of the reviewers
     */
    @Path("getHunkApprovals")
    @GET
    public Response getHunkApprovals(@PathParam("owner") String owner,
                                     @PathParam("name") String name,
                                     @PathParam("number") Integer number) throws Exception {
        PullRequestIdentifier pull = new PullRequestIdentifier(owner, name, number);
        Action<List<HunkApprovals>> action = databaseService.getHunkApprovals(pull);

        return interpreter.evaluateToResponse(action);
    }

    /**
     * Sets the approval from a user on a hunk.
     *
     * @param owner     The owner of the repository
     * @param name      The name of the repository
     * @param number    The pullrequest number
     * @param body      Hunk approval information
     * @return A unit response
     */
    @Path("setApprove")
    @POST
    public Response setApprove(@PathParam("owner") String owner,
                              @PathParam("name") String name,
                              @PathParam("number") Integer number,
                              ApproveRequest body) throws Exception {

        //TODO: check if user may submit this approval pull-request
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
