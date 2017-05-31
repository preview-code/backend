package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Information about approving hunks
 */
public class ApproveRequest {
    /**
     * The hunk which is approved
     */
    @JsonProperty("hunkId")
    public final String hunkId;
    /**
     * If the hunk is approved or disapproved
     */
    @JsonProperty("isApproved")
    public final ApproveStatus isApproved;
    /**
     * Which user approves this hunk
     */
    @JsonProperty("githubLogin")
    public final int githubLogin;

    @JsonCreator
    public ApproveRequest(@JsonProperty("hunkId") String hunkId, @JsonProperty("isApproved")
            ApproveStatus isApproved, @JsonProperty("githubLogin") int githubLogin) {
        this.hunkId = hunkId;
        this.isApproved = isApproved;
        this.githubLogin = githubLogin;
    }
}
