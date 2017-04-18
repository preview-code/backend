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

    public static PrNumber fromString(String string) {
        PrNumber number = new PrNumber();
        number.number = Integer.parseInt(string);
        return number;
    }

    @Override
    public String toString(){
        return Integer.toString(number);
     }
}
