package previewcode.backend.api.v2;

import com.google.inject.name.Names;
import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import previewcode.backend.APIModule;
import previewcode.backend.DTO.*;
import previewcode.backend.services.actiondsl.Interpreter;
import previewcode.backend.test.helpers.ApiEndPointTest;
import previewcode.backend.database.PullRequestGroup;
import previewcode.backend.services.IDatabaseService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.*;
import static previewcode.backend.services.actiondsl.ActionDSL.*;

@ApiEndPointTest(TestModule.class)
public class EndPointTest {

    @Test
    public void testApiIsReachable(WebTarget target) {
        Response response = target
                .path("/v2/test")
                .request("application/json")
                .get();

        assertThat(response.getStatus()).isEqualTo(200);

        TestAPI.Response apiResponse = response.readEntity(TestAPI.Response.class);
        assertThat(apiResponse.apiVersion).isEqualTo("v2");
    }

    @Test
    public void orderingApiIsReachable(WebTarget target) {
        Response response = target
                .path("/v2/preview-code/backend/pulls/42/ordering")
                .request("application/json")
                .post(Entity.json(new ArrayList<>()));

        assertThat(response.getLength()).isZero();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void setApprovedApiIsReachable(WebTarget target) {
        Response response = target
                .path("/v2/preview-code/backend/pulls/42/setApprove")
                .request("application/json")
                .post(Entity.json("{}"));

        assertThat(response.getLength()).isZero();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    public void getApprovalsApiIsReachable(WebTarget target) {
        Response response = target
                .path("/v2/preview-code/backend/pulls/42/getApprovals")
                .request("application/json")
                .get();

        assertThat(response.getLength()).isZero();
        assertThat(response.getStatus()).isEqualTo(200);
    }


    @Test
    public void getHunkApprovalsApiIsReachable(WebTarget target) {
        Response response = target
                .path("/v2/preview-code/backend/pulls/42/getHunkApprovals")
                .request("application/json")
                .get();

        assertThat(response.getLength()).isZero();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}

class TestModule extends APIModule implements IDatabaseService {

    public TestModule() {}

    @Override
    public Action<Unit> updateOrdering(PullRequestIdentifier pullRequestIdentifier, List<OrderingGroup> body) {
        return new NoOp<>();
    }

    @Override
    public Action<Unit> insertDefaultGroup(PullRequestIdentifier pullRequestIdentifier, List<OrderingGroup> body) {
        return new NoOp<>();
    }

    @Override
    public Action<Unit> setApproval(PullRequestIdentifier pullRequestIdentifier, ApproveRequest approve) {
        return new NoOp<>();
    }

    @Override
    public Action<io.vavr.collection.List<PullRequestGroup>> fetchPullRequestGroups(PullRequestIdentifier pull) {
        return new NoOp<>();
    }

    @Override
    public Action<ApprovedPullRequest> getApproval(PullRequestIdentifier pull) {
        return new NoOp<>();
    }

    @Override
    public Action<List<HunkApprovals>> getHunkApprovals(PullRequestIdentifier pull) {
        return new NoOp<>();
    }


    @SuppressWarnings("unchecked")
    @Override
    public void configureServlets() {
        super.configureServlets();
        // The DatabaseService always returns a no-op action
        this.bind(IDatabaseService.class).toInstance(this);

//         The interpreter always evaluates any action to Unit
        this.bind(Interpreter.class).annotatedWith(Names.named("database-interp")).toInstance(
                interpret().on(NoOp.class).apply(x -> unit)
        );
    }
}
