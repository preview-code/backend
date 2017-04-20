package previewcode.backend.DTO;

/**
 * Information about approving hunks
 */
public class Track {
    /**
     * The id of the user
     */
    public String userID;

    /**
     * The new location of the current user
     */
    public String newPath;

    /**
     * The old location of the current user
     */
    public String oldPath;

    /**
     * The time of the request
     */
    public String time;
}
