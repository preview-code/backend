package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * The data for the newly made pull request
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class PRbody extends TitleDescription {

    /**
     * The head branch of the newly made pull request
     */
    @JsonProperty("head")
    public final String head;

    /**
     * The base branch of the newly made pull request
     */
    @JsonProperty("base")
    public final String base;

    /**
     * The ordering of the changes of the pull request
     */
    @JsonProperty("ordering")
    public final List<OrderingGroup> ordering;

    /**
     * If the description should include metadata.
     */
    @JsonProperty("metadata")
    public final Boolean metadata;

    @JsonCreator
    public PRbody(
            @JsonProperty("title") String title,
            @JsonProperty("description") String description,
            @JsonProperty("head") String head,
            @JsonProperty("base") String base,
            @JsonProperty("ordering") List<OrderingGroup> ordering,
            @JsonProperty("metadata") Boolean metadata) {
        super(title, description);
        this.head = head;
        this.base = base;
        this.ordering = ordering;
        this.metadata = metadata;
    }
}
