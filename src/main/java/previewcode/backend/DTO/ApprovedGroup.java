package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import previewcode.backend.database.GroupID;

import java.util.List;
import java.util.Map;


public class ApprovedGroup {

    /**
     * If the group is approved
     */
    @JsonProperty("approved")
    public final ApproveStatus approved;

    /**
     * All the hunks in this group
     */
    @JsonProperty("hunks")
    public final List<HunkApprovals> hunks;

    public final GroupID groupID;

    public ApprovedGroup(ApproveStatus approved, List<HunkApprovals> hunks, GroupID groupID) {
        this.approved = approved;
        this.hunks = hunks;
        this.groupID = groupID;
    }

}