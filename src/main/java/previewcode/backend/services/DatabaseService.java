package previewcode.backend.services;


import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import previewcode.backend.DTO.*;
import previewcode.backend.database.*;
import previewcode.backend.services.actions.DatabaseActions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import static previewcode.backend.services.actiondsl.ActionDSL.*;
import static previewcode.backend.services.actions.DatabaseActions.*;

public class DatabaseService implements IDatabaseService {

    @Override
    public Action<Unit> updateOrdering(PullRequestIdentifier pull, List<OrderingGroup> newGroups) {
        return insertPullIfNotExists(pull).then(pullID ->
                fetchGroups(pullID).then(existingGroups ->
                        traverse(newGroups, createGroup(pullID))
                        .then(deriveDefaultGroup(existingGroups, newGroups)
                        .then(defaultGroup -> traverse(existingGroups, g -> delete(g.id)).pure(defaultGroup))
                        .then(defaultGroup -> {
                             if (!defaultGroup.isEmpty())
                                 return createGroup(pullID).apply(defaultGroup);
                             else
                                 return pure(unit);
                         }))
                )
        ).toUnit();
    }

    private Action<OrderingGroup> deriveDefaultGroup(List<PullRequestGroup> existingGroups, List<OrderingGroup> newGroups) {
        List<HunkChecksum> newHunks = newGroups.flatMap(orderingGroup -> orderingGroup.hunkChecksums);
        return traverse(existingGroups, g -> g.fetchHunks)
                .map(flatten())
                .map(hunks -> hunks.map(hunk -> hunk.checksum))
                .map(existingHunks ->
                        existingHunks.removeAll(newHunks))
                .map(OrderingGroup::newDefaultGoup);
    }

    public Action<Unit> mergeNewHunks(PullRequestIdentifier pull, List<HunkChecksum> newHunks) {
        Function<List<OrderingGroup>, List<OrderingGroup>> merge = existingGroups ->
                existingGroups.map(g -> g.intersect(newHunks)).filter(OrderingGroup::isEmpty);

        return insertPullIfNotExists(pull).then(pullID ->
                fetchGroups(pullID).then(existingGroups ->
                        traverse(existingGroups, g -> fetchGroupOrdering(g).then(o -> delete(g.id).pure(o)))
                        .map(merge)
                        .then(newGroups -> traverse(newGroups, createGroup(pullID)).pure(newGroups))
                        .map(newGroups ->
                                newHunks.removeAll(newGroups.flatMap(g -> g.hunkChecksums))
                        )
                        .map(OrderingGroup::newDefaultGoup)
                        .then(defaultGroup -> createGroup(pullID).apply(defaultGroup))
                )
        );
    }

    @Override
    public Action<Ordering> getOrdering(PullRequestIdentifier pull) {

        return fetchPullRequestGroups(pull).then(this::fetchPullOrdering);
    }

    public Action<List<HunkChecksum>> fetchGroupHunks(PullRequestGroup group) {
        return fetchHunks(group.id).map(hunks -> hunks.map(hunk -> hunk.checksum));
    }

    public Action<OrderingGroup> fetchGroupOrdering(PullRequestGroup group) {
        return fetchGroupHunks(group).map(hunkChecksums ->
                new OrderingGroup(group.title, group.description, hunkChecksums, group.defaultGroup));
    }

    public Ordering createOrdering(List<OrderingGroup> orderingGroups) {
       return new Ordering(
                orderingGroups.find(group -> group.defaultGroup).get(),
                orderingGroups.filter(group -> !group.defaultGroup));
    }

    public Action<Ordering> fetchPullOrdering(List<PullRequestGroup> pullRequestGroups) {
        return traverse(pullRequestGroups, this::fetchGroupOrdering).map(this::createOrdering);
    }


    @Override
    public Action<Unit> insertGroup(PullRequestIdentifier pull, OrderingGroup group) {
        return insertPullIfNotExists(pull)
                .then(dbPullId -> createGroup(dbPullId).apply(group)).toUnit();
    }

    @Override
    public Action<Unit> setApproval(PullRequestIdentifier pull, ApproveRequest approve) {
        return insertPullIfNotExists(pull).then(dbPullId ->
                setApprove(dbPullId, approve.hunkChecksum, approve.githubLogin, approve.isApproved));
    }

    @Override
    public Action<List<PullRequestGroup>> fetchPullRequestGroups(PullRequestIdentifier pull) {
        return fetchPull(pull).then(DatabaseActions::fetchGroups);
    }

    @Override
    public Action<ApprovedPullRequest> getApproval(PullRequestIdentifier pull) {
        Function<ApprovedGroup, Map<Long, ApprovedGroup>> toMap = approvedGroup -> {
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
                newGroup(dbPullId, group.info.title, group.info.description, group.defaultGroup).then(
                        groupID -> traverse(List.ofAll(group.hunkChecksums), hunkId -> assignToGroup(groupID, hunkId.checksum))
                ).toUnit();
    }

    private static Action<ApprovedGroup> getGroupApproval(GroupID groupID) {
        return fetchHunks(groupID).then(
                hunks -> traverse(hunks, (Hunk h) -> h.fetchApprovals.map(approvals -> {
                    Map<String, ApproveStatus> approvalMap = new HashMap<>();
                    approvals.forEach(approval -> approvalMap.put(approval.approver, approval.approveStatus));

                    Map<String, HunkApprovals> map = new HashMap<>();
                    HunkApprovals hApprovals = new HunkApprovals(h.checksum, isHunkApproved(approvals.map(a -> a.approveStatus)), approvalMap);
                    map.put(h.checksum.checksum, hApprovals);
                    return map;
                }))
                .map(DatabaseService::combineMaps)
                .map(approvals -> {
                    Map<String, ApproveStatus> approvalMap = new HashMap<>();
                    java.util.List<HunkApprovals> hunkList = new ArrayList<>();
                    approvals.forEach((hunk, approval) -> {
                        approvalMap.put(hunk, approval.approved);
                        hunkList.add(approval);
                    });

                    return new ApprovedGroup(
                            isGroupApproved(hunks.length(), approvalMap),
                            hunkList,
                            groupID);
                })
        );
    }


    private static <A, B> Map<A, B> combineMaps(List<Map<A, B>> maps) {
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


