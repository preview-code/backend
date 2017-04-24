package previewcode.backend.DTO;

import java.util.Optional;

/**
 * DTO representing a preview-code specific Ordering status.
 * This status is `pending` when the author of a PR needs to order his/her changes
 * and `success` when the changes are ordered.
 */
public class OrderingStatus extends GitHubStatus {
    private static final String CONTEXT = "preview-code/ordering";
    private static final String PENDING_DESCRIPTION = "Waiting for author to order changes.";
    private static final String SUCCESS_DESCRIPTION = "Changes have been ordered by the author.";

    public OrderingStatus(GitHubPullRequest pullRequest, GitHubRepository repository) {
        super("pending", PENDING_DESCRIPTION, CONTEXT, pullRequest.previewCodeUrl(repository));
    }

    private OrderingStatus(String state, String description, String url) {
        super(state, description, CONTEXT, url);
    }

    public Boolean isComplete() {
        return this.state.equals("success");
    }

    public Boolean isPending() {
        return this.state.equals("pending");
    }

    public OrderingStatus complete() {
        return new OrderingStatus("success", SUCCESS_DESCRIPTION, this.url);
    }

    public OrderingStatus pending() {
        return new OrderingStatus("pending", PENDING_DESCRIPTION, this.url);
    }

    public static Optional<OrderingStatus> fromGitHubStatus(GitHubStatus status) {
        if (status.context.equals(CONTEXT)) {
            return Optional.of(new OrderingStatus(status.state, status.description, status.url));
        } else {
            return Optional.empty();
        }
    }
}
