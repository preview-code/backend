package previewcode.backend.api.v2;

import com.google.inject.Inject;
import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import previewcode.backend.DTO.*;
import previewcode.backend.services.IDatabaseService;
import previewcode.backend.services.actiondsl.Interpreter;

import javax.inject.Named;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static previewcode.backend.services.actiondsl.ActionDSL.*;

/**
 * API for getting and setting the approvals on a pullrequest
 */
@Path("v2/{owner}/{name}/pulls/{number}/")
public class ApprovalsAPI {

    private Interpreter interpreter;
    private IDatabaseService databaseService;

    @Inject
    public ApprovalsAPI(@Named("database-interp") Interpreter interpreter, IDatabaseService databaseService) {
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
                                 @PathParam("number") Integer number) {
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
                                     @PathParam("number") Integer number) {
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
                              ApproveRequest body) {

        // TODO: check if user is a reviewer on this PR

        PullRequestIdentifier pull = new PullRequestIdentifier(owner, name, number);
        Action<Unit> action = databaseService.setApproval(pull, body);

        return interpreter.evaluateToResponse(action);
    }
}
