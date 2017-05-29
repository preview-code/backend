package previewcode.backend.DTO;

import java.util.List;
import java.util.Map;

/**
 * Information if a pull request or it's groups/hunks are approved
 */
public class IsApproved {
    /**
     * If the pull request is approved
     */
    public String approved;

    /**
     * The groups of this pull request
     */
    public Map<String, ApprovedGroup> groups;

}
