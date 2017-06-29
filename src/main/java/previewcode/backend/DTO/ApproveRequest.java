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
    @JsonProperty("hunkChecksum")
    public final String hunkChecksum;
    /**
     * If the hunk is approved or disapproved
     */
    @JsonProperty("isApproved")
    public final ApproveStatus isApproved;
    /**
     * Which user approves this hunk
     */
    @JsonProperty("githubLogin")
    public final String githubLogin;

    @JsonCreator
    public ApproveRequest(@JsonProperty("hunkChecksum") String hunkChecksum, @JsonProperty("isApproved")
            ApproveStatus isApproved, @JsonProperty("githubLogin") String githubLogin) {
        this.hunkChecksum = hunkChecksum;
        this.isApproved = isApproved;
        this.githubLogin = githubLogin;
    }
}
