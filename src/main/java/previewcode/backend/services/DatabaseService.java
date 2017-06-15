package previewcode.backend.services;


import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import previewcode.backend.DTO.*;
import previewcode.backend.database.*;
import previewcode.backend.services.actions.DatabaseActions;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import static previewcode.backend.services.actiondsl.ActionDSL.*;
import static previewcode.backend.services.actions.DatabaseActions.*;
import static previewcode.backend.services.actions.DatabaseActions.fetchApprovals;

public class DatabaseService implements IDatabaseService {

    @Override
    public Action<Unit> updateOrdering(PullRequestIdentifier pull, List<OrderingGroup> groups) {
        return insertPullIfNotExists(pull)
                .then(this::clearExistingGroups)
                .then(dbPullId -> traverse(groups, createGroup(dbPullId))).toUnit();
    }

    @Override
    public Action<Unit> setApproval(PullRequestIdentifier pull, ApproveRequest approve) {
        return insertPullIfNotExists(pull).then(dbPullId ->
                setApprove(dbPullId, approve.hunkId, Integer.toString(approve.githubLogin), approve.isApproved));
    }

    @Override
    public Action<List<PullRequestGroup>> fetchPullRequestGroups(PullRequestIdentifier pull) {
        return fetchPull(pull).then(DatabaseActions::fetchGroups);
    }

    @Override
    public Action<ApprovedPullRequest> getApproval(PullRequestIdentifier pull) {
        Function<ApprovedGroup, Map<Long, ApprovedGroup>>  toMap = approvedGroup -> {
            Map<Long, ApprovedGroup> map = new HashMap<>();
            map.put(approvedGroup.groupID.id, approvedGroup);
            return map;
        };

        return fetchPullRequestGroups(pull).then(
                pullRequestGroups -> traverse(pullRequestGroups, group -> getGroupApproval(group.id))
                        .map(approvals -> isPullApproved(pullRequestGroups.length(), combineMaps(approvals.map(toMap))))
        );
    }

    public Function<OrderingGroup, Action<Unit>> createGroup(PullRequestID dbPullId) {
        return group ->
                newGroup(dbPullId, group.info.title, group.info.description).then(
                    groupID -> traverse(List.ofAll(group.diff), hunkId -> assignToGroup(groupID, hunkId))
                ).toUnit();
    }

    public Action<PullRequestID> clearExistingGroups(PullRequestID dbPullId) {
        return fetchGroups(dbPullId)
                .then(traverse(group -> delete(group.id)))
                .map(unit -> dbPullId);
    }

    @Override
    public Action<List<HunkApprovals>> getHunkApprovals(PullRequestIdentifier pull) {


        return fetchPullRequestGroups(pull)
                .then(pullRequestGroups -> traverse(pullRequestGroups, group -> fetchHunks(group.id)))
                .map(lists -> lists.fold(List.empty(), List::appendAll))
                .then(hunkIDS -> traverse(hunkIDS, id -> fetchApprovalsUser(id).map(approveStatuses ->
                        new HunkApprovals(id.hunkID, approveStatuses)
                )));
    }

    private static Action<ApprovedGroup> getGroupApproval(GroupID groupID) {
        Function<HunkID, Action<Map<String, ApproveStatus>>> fetchHunkApprovals =
                id -> fetchApprovals(id).map(statuses -> {
                    Map<String, ApproveStatus> map = new HashMap<>();
                    map.put(id.hunkID, isHunkApproved(statuses));
                    return map;
                });

        return fetchHunks(groupID).then(
                hunkIDS -> traverse(hunkIDS, fetchHunkApprovals)
                        .map(DatabaseService::combineMaps)
                        .map(hunkApprovals ->  new ApprovedGroup(
                                isGroupApproved(hunkIDS.length(), hunkApprovals),
                                hunkApprovals,
                                groupID)
                        )
        );
    }

    private static <A,B> Map<A,B> combineMaps(List<Map<A,B>> maps) {
        return maps.fold(new HashMap<>(), (a, b) -> {
            a.putAll(b);
            return a;
        });
    }

    private static ApprovedPullRequest isPullApproved(Integer count, Map<Long, ApprovedGroup> groups) {
        Map<Long, ApproveStatus> map = new HashMap<>();
        groups.forEach((a, approvedGroup) -> map.put(a, approvedGroup.approved));
        return new ApprovedPullRequest(isGroupApproved(count, map), groups);
    }

    private static <A> ApproveStatus isGroupApproved(Integer count, Map<A, ApproveStatus> statuses) {
        if (statuses.containsValue(ApproveStatus.DISAPPROVED)) {
            return ApproveStatus.DISAPPROVED;
        } else if (statuses.containsValue(ApproveStatus.NONE)) {
            return ApproveStatus.NONE;
        } else if (count.equals(statuses.size())) {
            return ApproveStatus.APPROVED;
        } else {
            return ApproveStatus.NONE;
        }
    }

    private static ApproveStatus isHunkApproved(List<ApproveStatus> statuses) {
        if (statuses.contains(ApproveStatus.DISAPPROVED)) {
            return ApproveStatus.DISAPPROVED;
        } else if (statuses.contains(ApproveStatus.APPROVED)) {
            return ApproveStatus.APPROVED;
        } else {
            return ApproveStatus.NONE;
        }
    }

}
