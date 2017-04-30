package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
/**
 * The ordering of the pull request
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class OrderingGroup {

    /**
     * The list of diffs in the pul request
     */
    @JsonProperty("diff")
    public final List<String> diff;

    /**
     * The body of the group
     */
    @JsonProperty("info")
    public final TitleDescription info;


    @JsonCreator
    public OrderingGroup(
            @JsonProperty("diff") List<String> diff,
            @JsonProperty("info") TitleDescription info) {
        this.diff = diff;
        this.info = info;
    }

    public OrderingGroup(String title, String description, List<String> hunks) {
        this.diff = hunks;
        this.info = new TitleDescription(title, description);
    }
}
