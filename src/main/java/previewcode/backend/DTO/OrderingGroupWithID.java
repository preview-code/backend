package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import previewcode.backend.database.PullRequestGroup;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown=true)
public class OrderingGroupWithID extends OrderingGroup {

    /**
     * The id of the group
     */
    @JsonProperty("id")
    public final String id;


    public OrderingGroupWithID(PullRequestGroup dbGroup, List<String> hunkIds) {
        super(dbGroup.title, dbGroup.description, hunkIds);
        this.id = dbGroup.id.id.toString();
    }
}
