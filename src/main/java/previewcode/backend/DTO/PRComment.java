package previewcode.backend.DTO;

/**
 * The data for a standard pull request comment
 *
 */
public class PRComment {

    /**
     * The body of the comment
     */
    public String body;

    public PRComment(String body) {
        this.body = body;
    }
}
