package previewcode.backend.services;


import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import previewcode.backend.DTO.ApproveRequest;
import previewcode.backend.DTO.OrderingGroup;
import previewcode.backend.DTO.PullRequestIdentifier;
import previewcode.backend.database.PullRequestGroup;
import previewcode.backend.database.PullRequestID;
import previewcode.backend.services.actions.DatabaseActions;

import java.util.function.Function;
import static previewcode.backend.services.actiondsl.ActionDSL.*;
import static previewcode.backend.services.actions.DatabaseActions.*;

public class DatabaseService implements IDatabaseService {

    @Override
    public Action<Unit> updateOrdering(PullRequestIdentifier pull, Seq<OrderingGroup> groups) {
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

}
