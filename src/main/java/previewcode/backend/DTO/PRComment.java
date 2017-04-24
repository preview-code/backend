package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The data for a standard pull request comment
 *
 */
public class PRComment {

    /**
     * The body of the comment
     */

    public String body;

    @JsonCreator
    public PRComment(@JsonProperty("body") String body) {
        this.body = body;
    }
}
