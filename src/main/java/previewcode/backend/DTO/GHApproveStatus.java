package previewcode.backend.DTO;

public class GHApproveStatus extends GitHubStatus {

    private static final String CONTEXT = "preview-code/approve-status";
    private static final String PENDING_DESCRIPTION = "Not all hunks have been reviewed";
    private static final String SUCCESS_DESCRIPTION = "All hunks have been accepted";
    private static final String FAILURE_DESCRIPTION = "There are rejected hunks";

    public GHApproveStatus(GitHubPullRequest pullRequest) {
        super("pending", PENDING_DESCRIPTION, CONTEXT, pullRequest.previewCodeUrl());
    }

    private GHApproveStatus(String state, String description, String url) {
        super(state, description, CONTEXT, url);
    }

    public Boolean isComplete() {
        return this.state.equals("success");
    }

    public Boolean isPending() {
        return this.state.equals("pending");
    }

    public Boolean isFailure() {
        return this.state.equals("failure");
    }

    public GHApproveStatus complete() {
        return new GHApproveStatus("success", SUCCESS_DESCRIPTION, this.url);
    }

    public GHApproveStatus pending() {
        return new GHApproveStatus("pending", PENDING_DESCRIPTION, this.url);
    }

    public GHApproveStatus failure() {
        return new GHApproveStatus("failure", FAILURE_DESCRIPTION, this.url);
    }

    @Override
    public String toString() {
        return "GHApproveStatus{" + state + "}";
    }
}
