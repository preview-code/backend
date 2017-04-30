package previewcode.backend.services.actions;

import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import previewcode.backend.DTO.PullRequestIdentifier;
import previewcode.backend.database.GroupID;
import previewcode.backend.database.HunkID;
import previewcode.backend.database.PullRequestGroup;
import previewcode.backend.database.PullRequestID;
import previewcode.backend.services.actiondsl.ActionDSL.*;

public class DatabaseActions {

    public static class DatabaseAction<A> extends Action<A> { }

    public static class FetchPull extends DatabaseAction<PullRequestID> {
        public final String owner;
        public final String name;
        public final Integer number;

        public FetchPull(PullRequestIdentifier pullRequestIdentifier) {
            this.owner = pullRequestIdentifier.owner;
            this.name = pullRequestIdentifier.name;
            this.number = pullRequestIdentifier.number;
        }
    }

    public static class InsertPullIfNotExists extends FetchPull {
        public InsertPullIfNotExists(PullRequestIdentifier pullRequestIdentifier) {
            super(pullRequestIdentifier);
        }
    }

    public static class NewGroup extends DatabaseAction<GroupID> {

        public final PullRequestID pullRequestId;
        public final String title;
        public final String description;

        public NewGroup(PullRequestID pullRequestId, String title, String description) {
            this.pullRequestId = pullRequestId;
            this.title = title;
            this.description = description;
        }
    }

    public static class AssignHunkToGroup extends DatabaseAction<Unit> {

        public final GroupID groupID;
        public final String hunkIdentifier;

        public AssignHunkToGroup(GroupID groupID, String hunkIdentifier) {
            this.groupID = groupID;
            this.hunkIdentifier = hunkIdentifier;
        }
    }

    public static class FetchGroupsForPull extends DatabaseAction<List<PullRequestGroup>> {
        public final PullRequestID pullRequestID;

        public FetchGroupsForPull(PullRequestID pullRequestID) {
            this.pullRequestID = pullRequestID;
        }
    }

    public static class FetchHunksForGroup extends DatabaseAction<List<HunkID>> {

        public final GroupID groupID;

        public FetchHunksForGroup(GroupID groupID) {
            this.groupID = groupID;
        }
    }

    public static class DeleteGroup extends DatabaseAction<Unit> {
        public final GroupID groupID;

        public DeleteGroup(GroupID groupID) {
            this.groupID = groupID;
        }
    }



    public static InsertPullIfNotExists insertPullIfNotExists(PullRequestIdentifier pull) {
        return new InsertPullIfNotExists(pull);
    }

    public static FetchPull fetchPull(PullRequestIdentifier pull) {
        return new FetchPull(pull);
    }

    public static FetchPull fetchPull(String owner, String name, Integer number) {
        return new FetchPull(new PullRequestIdentifier(owner, name, number));
    }

    public static NewGroup newGroup(PullRequestID pullRequestId, String title, String description) {
        return new NewGroup(pullRequestId, title, description);
    }

    public static AssignHunkToGroup assignToGroup(GroupID groupID, String hunkId) {
        return new AssignHunkToGroup(groupID, hunkId);
    }

    public static FetchGroupsForPull fetchGroups(PullRequestID pullRequestID) {
        return new FetchGroupsForPull(pullRequestID);
    }

    public static FetchHunksForGroup fetchHunks(GroupID groupID) {
        return new FetchHunksForGroup(groupID);
    }

    public static DeleteGroup delete(GroupID groupID) {
        return new DeleteGroup(groupID);
    }
}
