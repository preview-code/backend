package previewcode.backend.services.actions;

import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import previewcode.backend.DTO.ApproveStatus;
import previewcode.backend.DTO.PullRequestIdentifier;
import previewcode.backend.database.*;
import previewcode.backend.services.actiondsl.ActionDSL.*;

public class DatabaseActions {

    public static class FetchPull extends Action<PullRequestID> {
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

    public static class NewGroup extends Action<GroupID> {

        public final PullRequestID pullRequestId;
        public final String title;
        public final String description;

        public NewGroup(PullRequestID pullRequestId, String title, String description) {
            this.pullRequestId = pullRequestId;
            this.title = title;
            this.description = description;
        }
    }

    public static class AssignHunkToGroup extends Action<Unit> {

        public final GroupID groupID;
        public final String hunkIdentifier;

        public AssignHunkToGroup(GroupID groupID, String hunkIdentifier) {
            this.groupID = groupID;
            this.hunkIdentifier = hunkIdentifier;
        }
    }

    public static class FetchGroupsForPull extends Action<List<PullRequestGroup>> {
        public final PullRequestID pullRequestID;

        public FetchGroupsForPull(PullRequestID pullRequestID) {
            this.pullRequestID = pullRequestID;
        }
    }

    public static class FetchHunksForGroup extends Action<List<HunkID>> {

        public final GroupID groupID;

        public FetchHunksForGroup(GroupID groupID) {
            this.groupID = groupID;
        }
    }

    public static class FetchHunkApprovals extends Action<List<ApproveStatus>> {
        private final HunkID hunkID;

        public FetchHunkApprovals(HunkID hunkID) {
            this.hunkID = hunkID;
        }
    }

    public static class DeleteGroup extends Action<Unit> {
        public final GroupID groupID;

        public DeleteGroup(GroupID groupID) {
            this.groupID = groupID;
        }
    }

    public static class ApproveHunk extends Action<Unit> {
        public final PullRequestID pullRequestID;
        public final String hunkId;
        public final String githubUser;
        public final ApproveStatus status;

        public ApproveHunk(PullRequestID pullRequestID, String hunkId, String githubUser, ApproveStatus status) {

            this.pullRequestID = pullRequestID;
            this.hunkId = hunkId;
            this.githubUser = githubUser;
            this.status = status;
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

    public static FetchHunkApprovals fetchApprovals(HunkID hunkID) {
        return new FetchHunkApprovals(hunkID);
    }

    public static DeleteGroup delete(GroupID groupID) {
        return new DeleteGroup(groupID);
    }

    public static ApproveHunk setApprove(PullRequestID pullRequestID, String hunkId, String githubUser, ApproveStatus approve) {
        return new ApproveHunk(pullRequestID, hunkId, githubUser, approve);
    }

}
