package previewcode.backend.DTO;

import java.util.Map;

public class ApprovedGroup {

    /**
     * If the group is approved
     */
    public String approved;

    /**
     * All the hunks in this group
     */
    public Map<String, String> hunks;

}
