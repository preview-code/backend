package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Information if a pull request or it's groups/hunks are approved
 */
public class IsApproved {
    /**
     * If the pull request is approved
     */
    @JsonProperty("approved")
    public ApproveStatus approved;

    /**
     * The groups of this pull request
     */
    @JsonProperty("groups")
    public Map<String, ApprovedGroup> groups;

    @JsonCreator
    public IsApproved(@JsonProperty("approved") ApproveStatus approved, @JsonProperty("groups") Map<String, ApprovedGroup> groups ) {
        this.approved = approved;
        this.groups = groups;
    }


}