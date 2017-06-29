package previewcode.backend.api.v2;

import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import previewcode.backend.DTO.OrderingGroup;
import previewcode.backend.DTO.PullRequestIdentifier;
import previewcode.backend.services.IDatabaseService;
import static previewcode.backend.services.actiondsl.ActionDSL.*;

import previewcode.backend.services.actiondsl.Interpreter;

import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("v2/{owner}/{name}/pulls/{number}/ordering")
public class OrderingAPI {

    private final Interpreter interpreter;
    private final IDatabaseService databaseService;

    @Inject
    public OrderingAPI(Interpreter interpreter, IDatabaseService databaseService) {
        this.interpreter = interpreter;
        this.databaseService = databaseService;
    }

    @POST
    public Response updateOrdering(
            @PathParam("owner") String owner,
            @PathParam("name") String name,
            @PathParam("number") Integer number,
            java.util.List<OrderingGroup> body
    ){

        PullRequestIdentifier pull = new PullRequestIdentifier(owner, name, number);
        Action<Unit> action = databaseService.updateOrdering(pull, List.ofAll(body));
        return interpreter.evaluateToResponse(action);
    }

}
