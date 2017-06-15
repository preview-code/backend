package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import previewcode.backend.database.HunkID;

import java.util.Map;

/**
 * Information if a pull request or it's groups/hunks are approved
 */
public class HunkApprovals {


    @JsonProperty("hunkID")
    public String hunkID;

    /**
     * Per user the approvals status
     */
    @JsonProperty("approvals")
    public Map<String, ApproveStatus> approvals;

    public HunkApprovals(String hunkID, Map<String, ApproveStatus> approvals) {
        this.approvals = approvals;
        this.hunkID = hunkID;
    }


}