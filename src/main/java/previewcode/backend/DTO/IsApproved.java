package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;
import java.util.Optional;

/**
 * Information if a pull request or it's groups/hunks are approved
 */
public class IsApproved {
    /**
     * If the pull request is approved
     */
    @JsonProperty("approved")
    public Approving approved;

    /**
     * The groups of this pull request
     */
    @JsonProperty("groups")
    public Map<String, String> groups;

    @JsonCreator
    public IsApproved(@JsonProperty("approved") Approving approved, @JsonProperty("groups") Map<String, String> groups ) {
        this.approved = approved;
        this.groups = groups;
    }


}
