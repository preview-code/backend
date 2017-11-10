package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Information if a pull request or it's groups/hunks are approved
 */
public class HunkApprovals {


    @JsonProperty("hunkID")
    public String hunkChecksum;

    @JsonProperty("approved")
    public final ApproveStatus approved;
    /**
     * Per user the approvals status
     */
    @JsonProperty("approvals")
    public Map<String, ApproveStatus> approvals;

    public HunkApprovals(HunkChecksum hunkChecksum, ApproveStatus approved, Map<String, ApproveStatus> approvals) {
        this.approved = approved;
        this.approvals = approvals;
        this.hunkChecksum = hunkChecksum.checksum;
    }


}