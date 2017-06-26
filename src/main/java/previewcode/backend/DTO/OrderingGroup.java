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
    public final List<HunkChecksum> hunkChecksums;

    /**
     * The body of the group
     */
    @JsonProperty("info")
    public final TitleDescription info;


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
    }

    public OrderingGroup(String title, String description, List<HunkChecksum> hunks) {
        this.hunkChecksums = hunks;
        if(title == null){
            title = "";
        }
        this.info = new TitleDescription(title, description);
    }
}
