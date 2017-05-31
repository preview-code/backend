package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The data of a newly made pull request
 *
 */
public class ID {
    /**
     * The ID of a group/hunk
     */
    public final String id;

    @JsonCreator
    public ID(@JsonProperty("id") String id) {
        this.id = id;
    }

    @JsonValue
    public String getId() {
        return id;
    }

}
