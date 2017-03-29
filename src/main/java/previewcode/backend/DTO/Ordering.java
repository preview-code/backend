package previewcode.backend.DTO;

import java.util.List;
/**
 * The ordering of the pull request
 *
 */
public class Ordering {

    /**
     * The list of diffs in the pul request
     */
    public List<String> diff;
    
    /**
     * The id of the group
     */
    public String id;
    
    /**
     * The body of the group
     */
    public TitleDescription info;
        
}
