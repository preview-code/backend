package previewcode.backend.services;

import com.google.common.collect.Lists;
import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import io.vavr.control.Option;
import org.junit.jupiter.api.Test;
import previewcode.backend.DTO.*;
import previewcode.backend.database.GroupID;
import previewcode.backend.database.HunkID;
import previewcode.backend.database.PullRequestGroup;
import previewcode.backend.database.PullRequestID;
import previewcode.backend.services.actiondsl.Interpreter;

import java.util.Collection;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.*;
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

    private List<HunkID> hunkIDs = List.of(
            new HunkID("abcd"), new HunkID("efgh"), new HunkID("ijkl"));

    private List<OrderingGroup> groupsWithHunks = groups.map(group ->
        new OrderingGroupWithID(group, hunkIDs.map(id -> id.hunkID).toJavaList())
    );



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

        Collection<HunkID> hunksAdded = Lists.newArrayList();

        Interpreter interpreter =
                interpret()
                .on(InsertPullIfNotExists.class).returnA(pullRequestID)
                .on(FetchGroupsForPull.class).returnA(List.empty())
                .on(NewGroup.class).apply(action ->
                    groups.find(g -> g.title.equals(action.title)).get().id)
                .on(AssignHunkToGroup.class).apply(toUnit(action -> {
                    assertThat(groups.find(g -> g.id.equals(action.groupID))).isNotEmpty();
                    Option<HunkID> hunkID = hunkIDs.find(id -> id.hunkID.equals(action.hunkIdentifier));
                    assertThat(hunkID).isNotEmpty();
                    hunksAdded.add(hunkID.get());
                }));

        interpreter.unsafeEvaluate(dbAction);
        assertThat(hunksAdded)
                .hasSameElementsAs(hunkIDs)
                .hasSize(hunkIDs.size() * groupsWithHunks.size());
    }
}
