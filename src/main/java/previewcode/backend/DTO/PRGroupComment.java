package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The data for a group comment
 *
 */
public class PRGroupComment extends PRComment {
    /**
     * The id of the group in which the comment is located
     */
    public final String groupID;

    @JsonCreator
    public PRGroupComment(@JsonProperty("groupID") String groupID, @JsonProperty("body") String body) {
        super(body);
        this.groupID = groupID;
    }
}
