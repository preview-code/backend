package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Object that has a title and body
 *
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class TitleDescription {

    /**
     * The title of the object
     */
    @JsonProperty("title")
    public final String title;

    /**
     * The description of the object
     */
    @JsonProperty("description")
    public final String description;

    @JsonCreator
    public TitleDescription(
            @JsonProperty("title") String title,
            @JsonProperty("description") String description) {
        this.title = title;
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TitleDescription that = (TitleDescription) o;

        if (!title.equals(that.title)) return false;
        return description.equals(that.description);
    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }
}
