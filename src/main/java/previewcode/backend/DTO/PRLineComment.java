package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The data for a group comment
 *
 */
public class PRLineComment extends PRComment {

    /*
     *   The SHA of the latest commit.
     */
    public final String sha;

    /**
     * The relative file path
     */
    public final String path;

    /**
     * The index of the line to comment on in the diff
     */
    public final int position;

    @JsonCreator
    public PRLineComment(
            @JsonProperty("body") String body,
            @JsonProperty("sha") String sha,
            @JsonProperty("path") String path,
            @JsonProperty("position") Integer position) {
        super(body);
        this.sha = sha;
        this.path = path;
        this.position = position;
    }
}
