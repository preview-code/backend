package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import previewcode.backend.database.GroupID;

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
    public final Map<String, ApproveStatus> hunks;

    public final GroupID groupID;

    public ApprovedGroup(ApproveStatus approved, Map<String, ApproveStatus> hunks, GroupID groupID) {
        this.approved = approved;
        this.hunks = hunks;
        this.groupID = groupID;
    }

}