package previewcode.backend.services;

import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import previewcode.backend.DTO.ApproveRequest;
import previewcode.backend.DTO.ApprovedPullRequest;
import previewcode.backend.DTO.OrderingGroup;
import previewcode.backend.DTO.PullRequestIdentifier;
import previewcode.backend.database.PullRequestGroup;

import static previewcode.backend.services.actiondsl.ActionDSL.*;

public interface IDatabaseService {
    Action<Unit> updateOrdering(PullRequestIdentifier pullRequestIdentifier, Seq<OrderingGroup> body);

    Action<Unit> setApproval(PullRequestIdentifier pullRequestIdentifier, ApproveRequest approval);

    Action<List<PullRequestGroup>> fetchPullRequestGroups(PullRequestIdentifier pull);

    Action<ApprovedPullRequest> getApproval(PullRequestIdentifier pull);
}
