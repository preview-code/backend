package previewcode.backend.DTO;

import java.util.List;

/**
 * The data for the newly made pull request
 */
public class PRbody extends TitleDescription{

    /**
     * The head branch of the newly made pull request
     */
    public String head;
    /**
     * The base branch of the newly made pull request
     */
    public String base;
    /**
     * The ordering of the changes of the pull request
     */
    public List<Ordering> ordering;
    /**
     * If the description should include metadata.
     */
    public boolean metadata;

}
