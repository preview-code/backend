package previewcode.backend.DTO;

/**
 * Information about approving hunks
 */
public class Approve {
    /**
     * The hunk which is approved
     */
    public String hunkId;
    /**
     * If the hunk is approved, disapproved or none
     */
    public String status;
    /**
     * Which user approves this hunk
     */
    public int githubLogin;
    
    
}
