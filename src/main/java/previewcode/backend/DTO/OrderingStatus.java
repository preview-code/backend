package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OrderingStatus {
    @JsonProperty("state")
    public final String state;

    @JsonProperty("description")
    public final String description;

    @JsonProperty("context")
    public final String context = "preview-code/ordering";

    @JsonProperty("target_url")
    public final String url;

    private static final String PENDING_DESCRIPTION = "Waiting for author to order changes.";
    private static final String SUCCESS_DESCRIPTION = "Changes have been ordered by the author.";

    public OrderingStatus(GitHubPullRequest pullRequest, GitHubRepository repository) {
        this.state = "pending";
        this.description = PENDING_DESCRIPTION;
        this.url = pullRequest.previewCodeUrl(repository);
    }

    private OrderingStatus(String state, String description, String url) {
        this.state = state;
        this.description =  description;
        this.url = url;
    }

    public OrderingStatus complete() {
        return new OrderingStatus("success", SUCCESS_DESCRIPTION, this.url);
    }

    public OrderingStatus pending() {
        return new OrderingStatus("pending", PENDING_DESCRIPTION, this.url);
    }
}
