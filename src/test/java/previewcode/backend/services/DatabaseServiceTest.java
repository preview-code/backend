package previewcode.backend.services;

import com.google.common.collect.Lists;
import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;
import previewcode.backend.DTO.*;
import previewcode.backend.database.*;
import previewcode.backend.DTO.HunkChecksum;
import previewcode.backend.services.actiondsl.Interpreter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.fail;
import static previewcode.backend.services.actiondsl.ActionDSL.*;
import static previewcode.backend.services.actions.DatabaseActions.*;

public class DatabaseServiceTest {

    // Service under test
    private IDatabaseService service = new DatabaseService();

    // Mock data to feed to the service
    private static final String owner = "preview-code";
    private static final String name = "backend";
    private static final Integer number = 42;

    private final PullRequestID pullRequestID = new PullRequestID(new Long(number));

    private PullRequestIdentifier pullIdentifier = new PullRequestIdentifier(owner, name, number);

    private List<PullRequestGroup> groups = List.of(
            new PullRequestGroup(new GroupID(42L), "Group A", "Description A"),
            new PullRequestGroup(new GroupID(24L), "Group B", "Description B")
    );

    private List<OrderingGroup> groupsWithoutHunks= groups.map(group ->
        new OrderingGroupWithID(group, Lists.newLinkedList())
    );

    private List<HunkChecksum> hunkIDs = List.of(
            new HunkChecksum("abcd"), new HunkChecksum("efgh"), new HunkChecksum("ijkl"));

    private List<OrderingGroup> groupsWithHunks = groups.map(group ->
        new OrderingGroupWithID(group, hunkIDs.map(id -> id.checksum).toJavaList())
    );

    private ApproveRequest approveStatus = new ApproveRequest("checksum", ApproveStatus.DISAPPROVED, "txsmith");

    @Test
    public void insertsPullIfNotExists() throws Exception {
        Action<Unit> dbAction = service.updateOrdering(pullIdentifier, List.empty());

        Consumer<InsertPullIfNotExists> assertions = action -> {
            assertThat(action.owner).isEqualTo(owner);
            assertThat(action.name).isEqualTo(name);
            assertThat(action.number).isEqualTo(number);
        };

        Interpreter interpreter =
                interpret().on(InsertPullIfNotExists.class).stop(assertions);

        assertThatExceptionOfType(Interpreter.StoppedException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(dbAction));
    }

    @Test
    public void removesExistingGroups() throws Exception {
        Action<Unit> dbAction = service.updateOrdering(pullIdentifier, List.empty());

        Collection<PullRequestGroup> removedGroups = Lists.newArrayList();

        Interpreter interpreter =
                interpret()
                .on(InsertPullIfNotExists.class).returnA(pullRequestID)
                .on(FetchGroupsForPull.class).returnA(groups)
                .on(DeleteGroup.class).apply(toUnit(action -> {
                    assertThat(groups).extracting("id").contains(action.groupID);
                    removedGroups.add(groups.find(group -> group.id.equals(action.groupID)).get());
                }));

        interpreter.unsafeEvaluate(dbAction);
        assertThat(removedGroups)
                .hasSameElementsAs(groups)
                .hasSameSizeAs(groups);
    }

    @Test
    public void doesNotRemoveGroups() throws Exception {
        Action<Unit> dbAction = service.updateOrdering(pullIdentifier, List.empty());

        Interpreter interpreter =
                interpret()
                .on(InsertPullIfNotExists.class).returnA(pullRequestID)
                .on(FetchGroupsForPull.class).returnA(List.empty());

        interpreter.unsafeEvaluate(dbAction);
    }

    @Test
    public void insertsNewGroupsWithoutHunks() throws Exception {
        Action<Unit> dbAction = service.updateOrdering(pullIdentifier, groupsWithoutHunks);

        Collection<PullRequestGroup> groupsAdded = Lists.newArrayList();

        Interpreter interpreter =
                interpret()
                .on(InsertPullIfNotExists.class).returnA(pullRequestID)
                .on(FetchGroupsForPull.class).returnA(List.empty())
                .on(NewGroup.class).apply(action -> {
                    assertThat(action.pullRequestId).isEqualTo(pullRequestID);
                    PullRequestGroup group = groups.find(g -> g.title.equals(action.title)).get();
                    assertThat(group.description).isEqualTo(action.description);
                    groupsAdded.add(group);
                    return group.id;
                });

        interpreter.unsafeEvaluate(dbAction);
        assertThat(groupsAdded)
                .hasSameElementsAs(groups)
                .hasSameSizeAs(groups);
    }

    @Test
    public void insertsNewGroupsWithHunks() throws Exception {
        Action<Unit> dbAction = service.updateOrdering(pullIdentifier, groupsWithHunks);

        Collection<HunkChecksum> hunksAdded = Lists.newArrayList();

        Interpreter interpreter =
                interpret()
                .on(InsertPullIfNotExists.class).returnA(pullRequestID)
                .on(FetchGroupsForPull.class).returnA(List.empty())
                .on(NewGroup.class).apply(action ->
                    groups.find(g -> g.title.equals(action.title)).get().id)
                .on(AssignHunkToGroup.class).apply(toUnit(action -> {
                    assertThat(groups.find(g -> g.id.equals(action.groupID))).isNotEmpty();
                    Option<HunkChecksum> hunkID = hunkIDs.find(id -> id.checksum.equals(action.hunkChecksum));
                    assertThat(hunkID).isNotEmpty();
                    hunksAdded.add(hunkID.get());
                }));

        interpreter.unsafeEvaluate(dbAction);
        assertThat(hunksAdded)
                .hasSameElementsAs(hunkIDs)
                .hasSize(hunkIDs.size() * groupsWithHunks.size());
    }

    @Test
    public void insertApproval() throws Exception {
        Action<Unit> dbAction = service.setApproval(pullIdentifier, approveStatus);

        Interpreter interpreter =
                interpret()
                        .on(InsertPullIfNotExists.class).returnA(pullRequestID)
                        .on(ApproveHunk.class).stop(approveHunk -> {
                            assertThat(approveHunk.status)
                                    .isEqualTo(ApproveStatus.DISAPPROVED);
                            assertThat(approveHunk.githubUser)
                                    .isEqualTo("txsmith");
                            assertThat(approveHunk.hunkChecksum)
                                    .isEqualTo("checksum");
                            assertThat(approveHunk.pullRequestID)
                                    .isEqualTo(pullRequestID);
                        });

        assertThatExceptionOfType(Interpreter.StoppedException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(dbAction));

    }


    @Test
    void getApproval_fetches_pull_pullRequest() {
        Action<?> dbAction = service.getApproval(pullIdentifier);

        Interpreter.Stepper<?> stepper = interpret().stepwiseEval(dbAction);
        List<Action<?>> peek = stepper.peek();
        assertThat(peek).containsOnly(fetchPull(pullIdentifier));
    }

    @Test
    void getApproval_fetches_pull_groups() throws Exception {
        Action<?> dbAction = service.getApproval(pullIdentifier);

        Interpreter.Stepper<?> stepper = interpret()
                .on(FetchPull.class).returnA(pullRequestID)
                .stepwiseEval(dbAction);
        List<Action<?>> next = stepper.next();
        assertThat(next).containsOnly(fetchGroups(pullRequestID));
    }

    @Test
    void getApproval_fetches_hunks() throws Exception {
        Action<?> dbAction = service.getApproval(pullIdentifier);

        List<PullRequestGroup> oneGroup = List.of(
                new PullRequestGroup(new GroupID(42L), "Group A", "Description A")
        );

        Interpreter.Stepper<?> stepper = interpret()
                .on(FetchPull.class).returnA(pullRequestID)
                .on(FetchGroupsForPull.class).returnA(oneGroup)
                .stepwiseEval(dbAction);
        stepper.next();
        List<Action<?>> next = stepper.next();
        assertThat(next).containsOnly(fetchHunks(oneGroup.head().id));
    }

    @Test
    void getApproval_fetches_hunk_approvals() throws Exception {
        Action<?> dbAction = service.getApproval(pullIdentifier);

        HunkChecksum checksum = new HunkChecksum("abcd");
        HunkID id = new HunkID(1L);
        List<Hunk> oneHunk = List.of(new Hunk(id, new GroupID(2L), checksum));

        List<HunkApproval> hunkApprovals =  List.of(new HunkApproval(ApproveStatus.APPROVED, "txsmith"));

        Interpreter.Stepper<?> stepper = interpret()
                .on(FetchPull.class).returnA(pullRequestID)
                .on(FetchGroupsForPull.class).returnA(groups)
                .on(FetchHunksForGroup.class).returnA(oneHunk)
                .on(FetchHunkApprovals.class).returnA(hunkApprovals)
                .stepwiseEval(dbAction);
        stepper.next();
        stepper.next();

        assertThat(stepper.next()).containsOnly(new FetchHunkApprovals(id));
        assertThat(stepper.next()).isEmpty();
    }

    @Test
    void getHunkApproval_fetches_pull_pullRequest() {
        Action<?> dbAction = service.getHunkApprovals(pullIdentifier);

        List<Action<?>> peek = interpret().stepwiseEval(dbAction).peek();
        assertThat(peek).containsOnly(fetchPull(pullIdentifier));
    }

    @Test
    void getHunkApproval_fetches_pull_groups() throws Exception {
        Action<?> dbAction = service.getHunkApprovals(pullIdentifier);

        Interpreter.Stepper<?> stepper = interpret()
                .on(FetchPull.class).returnA(pullRequestID)
                .stepwiseEval(dbAction);
        List<Action<?>> next = stepper.next();
        assertThat(next).containsOnly(fetchGroups(pullRequestID));
    }

    @Test
    void getHunkApproval_fetches_hunks() throws Exception {
        Action<?> dbAction = service.getHunkApprovals(pullIdentifier);

        List<PullRequestGroup> oneGroup = List.of(
                new PullRequestGroup(new GroupID(42L), "Group A", "Description A")
        );

        Interpreter.Stepper<?> stepper = interpret()
                .on(FetchPull.class).returnA(pullRequestID)
                .on(FetchGroupsForPull.class).returnA(oneGroup)
                .stepwiseEval(dbAction);
        stepper.next();
        List<Action<?>> next = stepper.next();
        assertThat(next).containsOnly(fetchHunks(oneGroup.head().id));
    }

    @Test
    void getHunkApproval_fetches_hunk_approvals() throws Exception {
        Action<?> dbAction = service.getHunkApprovals(pullIdentifier);

        HunkChecksum checksum = new HunkChecksum("abcd");
        HunkID id = new HunkID(1L);
        List<Hunk> oneHunk = List.of(new Hunk(id, new GroupID(2L), checksum));

        Interpreter.Stepper<?> stepper = interpret()
                .on(FetchPull.class).returnA(pullRequestID)
                .on(FetchGroupsForPull.class).returnA(groups)
                .on(FetchHunksForGroup.class).returnA(oneHunk)
                .stepwiseEval(dbAction);
        stepper.next();
        stepper.next();

        List<Action<?>> next = stepper.next();
        assertThat(next.length()).isEqualTo(2);
        assertThat(next).containsOnly(fetchApprovals(id));
    }

    @Test
    void getHunkApproval_no_action_after_fetching_hunkapprovals() throws Exception {
        Action<?> dbAction = service.getHunkApprovals(pullIdentifier);

        Interpreter.Stepper<?> stepper = interpret()
                .on(FetchPull.class).returnA(pullRequestID)
                .on(FetchGroupsForPull.class).returnA(groups)
                .on(FetchHunksForGroup.class).returnA(hunkIDs.map(id -> new Hunk(new HunkID(-1L), new GroupID(2L), id)))
                .on(FetchHunkApprovals.class).returnA(List.empty())
                .stepwiseEval(dbAction);
        stepper.next();
        stepper.next();
        stepper.next();

        List<Action<?>> next = stepper.next();
        assertThat(next).isEmpty();
    }
}
