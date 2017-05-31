package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


public class ApprovedGroup {

    /**
     * If the group is approved
     */
    @JsonProperty("approved")
    public ApproveStatus approved;

    /**
     * All the hunks in this group
     */
    @JsonProperty("hunks")
    public Map<String, String> hunks;

    @JsonCreator
    public ApprovedGroup(@JsonProperty("approved") ApproveStatus approved, @JsonProperty("hunks") Map<String, String> hunks ) {
        this.approved = approved;
        this.hunks = hunks;
    }

}