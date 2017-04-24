package previewcode.backend.DTO;

/**
 * The data for a group comment
 *
 */
public class PRGroupComment extends PRComment {
    /**
     * The id of the group in which the comment is located
     */
    public String groupID;

    public PRGroupComment(String body) {
        super(body);
    }
}
