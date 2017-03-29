package previewcode.backend.DTO;

/**
 * The data of a newly made pull request
 *
 */
public class PrNumber {
    /**
     * The number of the newly made pull request
     */
    public int number;

    
    @Override
    public String toString(){
        return Integer.toString(number);
     }
}
