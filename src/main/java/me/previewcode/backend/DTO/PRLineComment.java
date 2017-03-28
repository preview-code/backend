package me.previewcode.backend.DTO;

/**
 * The data for a group comment
 *
 */
public class PRLineComment extends PRComment {

    /*
     *   The SHA of the latest commit.
     */
    public String sha;

    /**
     * The relative file path
     */
    public String path;

    /**
     * The index of the line to comment on in the diff
     */
    public int position;


}
