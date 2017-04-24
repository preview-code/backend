package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The data of a newly made pull request
 *
 */
public class PrNumber {
    /**
     * The number of the newly made pull request
     */
    public final Integer number;

    @JsonCreator
    public PrNumber(@JsonProperty("number") Integer number) {
        this.number = number;
    }


    @Override
    public String toString() {
        return Integer.toString(number);
    }
}
