package previewcode.backend.DTO;

import io.vavr.collection.List;
import previewcode.backend.services.DiffParser;


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
    private final List<HunkChecksum> hunkChecksums;

    /**
     * @return the hunkChecksums
     */
    public List<HunkChecksum> getHunkChecksums() {
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