package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;

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
    public final List<HunkChecksum> hunkChecksums;

    /**
     * The body of the group
     */
    @JsonProperty("info")
    public final TitleDescription info;

    public final Boolean defaultGroup;


    @JsonCreator
    public OrderingGroup(
            @JsonProperty("diff") List<HunkChecksum> hunkChecksums,
            @JsonProperty("info") TitleDescription info) {
        this.hunkChecksums = hunkChecksums;
        String title = info.title;
        if(title == null){
            title = "";
        }
        this.info = new TitleDescription(title, info.description);
        this.defaultGroup = false;
    }

    public OrderingGroup(String title, String description, List<HunkChecksum> hunks) {
        this.hunkChecksums = hunks;
        if(title == null){
            title = "";
        }
        this.info = new TitleDescription(title, description);
        this.defaultGroup = false;
    }

    public OrderingGroup(String title, String description, List<HunkChecksum> hunks, boolean defaultGroup) {
        this.hunkChecksums = hunks;
        if(title == null){
            title = "";
        }
        this.info = new TitleDescription(title, description);
        this.defaultGroup = defaultGroup;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrderingGroup that = (OrderingGroup) o;

        if (!hunkChecksums.equals(that.hunkChecksums)) return false;
        if (!info.equals(that.info)) return false;
        return defaultGroup.equals(that.defaultGroup);
    }

    @Override
    public int hashCode() {
        int result = hunkChecksums.hashCode();
        result = 31 * result + info.hashCode();
        result = 31 * result + defaultGroup.hashCode();
        return result;
    }
}
