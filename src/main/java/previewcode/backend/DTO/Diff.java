package previewcode.backend.DTO;

import previewcode.backend.services.DiffParser;

import java.util.List;

/**
 * Diff from GitHub, calculates hunkChecksums
 */
public class Diff {
    /**
     * The diff from GitHub;
     */
    private final String diff;
    /**
     * The hunkChecksums for the diff.
     */
    private final List hunkChecksums;

    /**
     * @return the hunkChecksums
     */
    public List getHunkChecksums() {
        return hunkChecksums;
    }

    /**
     * Get diff and call parser
     * @param diff from GitHub
     */
    public Diff(String diff) {
        this.diff = diff;
        DiffParser parser = new DiffParser();
        hunkChecksums = parser.parseDiff(diff);
    }


}