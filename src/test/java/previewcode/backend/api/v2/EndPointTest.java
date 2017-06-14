package previewcode.backend.api.v2;

import io.atlassian.fugue.Unit;
import io.vavr.collection.Seq;
import org.junit.jupiter.api.Test;
import previewcode.backend.APIModule;
import previewcode.backend.DTO.ApproveRequest;
import previewcode.backend.DTO.ApprovedPullRequest;
import previewcode.backend.DTO.OrderingGroup;
import previewcode.backend.DTO.PullRequestIdentifier;
import previewcode.backend.test.helpers.ApiEndPointTest;
import previewcode.backend.database.PullRequestGroup;
import previewcode.backend.services.IDatabaseService;
import previewcode.backend.services.actiondsl.Interpreter;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.List;

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
        List<OrderingGroup> emptyList = new ArrayList<>();

        Response response = target
                .path("/v2/preview-code/backend/pulls/42/ordering")
                .request("application/json")
                .post(Entity.json(emptyList));

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
}

class TestModule extends APIModule implements IDatabaseService {

    public TestModule() {}

    @Override
    public Action<Unit> updateOrdering(PullRequestIdentifier pullRequestIdentifier, Seq<OrderingGroup> body) {
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


    @SuppressWarnings("unchecked")
    @Override
    public void configureServlets() {
        super.configureServlets();
        // The DatabaseService always returns a no-op action
        this.bind(IDatabaseService.class).toInstance(this);

        // The interpreter always evaluates any action to Unit
        this.bind(Interpreter.class).toInstance(
                interpret().on(NoOp.class).apply(x -> unit)
        );
    }
}
