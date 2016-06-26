package me.previewcode.backend.DTO;

/**
 * Information about approving hunks
 */
public class Approve {
    /**
     * The hunk which is approved
     */
    public String hunkId;
    /**
     * If the hunk is approved or disapproved
     */
    public boolean isApproved;
    /**
     * Which user approves this hunk
     */
    public int githubLogin;
    
    
}
