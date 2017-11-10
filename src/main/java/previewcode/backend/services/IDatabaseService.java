package previewcode.backend.services;

import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import previewcode.backend.DTO.*;
import previewcode.backend.database.PullRequestGroup;

import static previewcode.backend.services.actiondsl.ActionDSL.*;

public interface IDatabaseService {
    Action<Unit> updateOrdering(PullRequestIdentifier pullRequestIdentifier, List<OrderingGroup> body);

    Action<Ordering> getOrdering(PullRequestIdentifier pullRequestIdentifier);

    Action<Unit> mergeNewHunks(PullRequestIdentifier pull, List<HunkChecksum> newHunks);

    Action<Unit> insertGroup(PullRequestIdentifier pullRequestIdentifier, OrderingGroup body);

    Action<Unit> setApproval(PullRequestIdentifier pullRequestIdentifier, ApproveRequest approval);

    Action<List<PullRequestGroup>> fetchPullRequestGroups(PullRequestIdentifier pull);

    Action<ApprovedPullRequest> getApproval(PullRequestIdentifier pull);
}
