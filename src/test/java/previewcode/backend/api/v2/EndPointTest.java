package previewcode.backend.api.v2;

import com.google.inject.name.Names;
import io.atlassian.fugue.Try;
import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHMyself;
import previewcode.backend.APIModule;
import previewcode.backend.DTO.*;
import previewcode.backend.services.IGithubService;
import previewcode.backend.services.actiondsl.ActionDSL;
import previewcode.backend.services.actiondsl.Interpreter;
import previewcode.backend.test.helpers.ApiEndPointTest;
import previewcode.backend.database.PullRequestGroup;
import previewcode.backend.services.IDatabaseService;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import java.io.IOException;
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
}

class TestModule extends APIModule implements IDatabaseService, IGithubService {

    public TestModule() {}

    @SuppressWarnings("unchecked")
    @Override
    public void configureServlets() {
        super.configureServlets();
        // The DatabaseService always returns a no-op action
        this.bind(IDatabaseService.class).toInstance(this);
        this.bind(IGithubService.class).toInstance(this);
//         The interpreter always evaluates any action to Unit
        this.bind(Interpreter.class).to(TestInterpreter.class);
        this.bind(Interpreter.class).annotatedWith(Names.named("database-interp")).to(TestInterpreter.class);

    }

    public static class TestInterpreter extends Interpreter {

        @Override
        protected <A, X> Try<A> run(Action<A> action) {
            return Try.successful((A) unit);
        }
    }



    @Override
    public Action<Unit> updateOrdering(PullRequestIdentifier pullRequestIdentifier, List<OrderingGroup> body) {
        return new NoOp<>();
    }

    @Override
    public Action<Unit> insertGroup(PullRequestIdentifier pullRequestIdentifier, OrderingGroup body) {
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
    public Action<Ordering> getOrdering(PullRequestIdentifier pull) {
        return new NoOp<>();
    }

    @Override
    public Action<Unit> mergeNewHunks(PullRequestIdentifier pull, List<HunkChecksum> newHunks) {
        return new NoOp<>();
    }


    @Override
    public GHMyself getLoggedInUser() throws IOException {
        return null;
    }

    @Override
    public PrNumber createPullRequest(String owner, String name, PRbody body) {
        return null;
    }

    @Override
    public GitHubPullRequest fetchPullRequest(PullRequestIdentifier identifier) throws IOException {
        return null;
    }

    @Override
    public void setPRStatus(GitHubPullRequest pullRequest, ApproveStatus status) throws IOException {

    }
}
