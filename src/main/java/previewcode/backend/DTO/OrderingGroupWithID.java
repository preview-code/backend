package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import previewcode.backend.database.PullRequestGroup;

public class OrderingGroupWithID extends OrderingGroup {

    @JsonProperty("groupID")
    public final Long groupID;

    public OrderingGroupWithID(List<HunkChecksum> hunkChecksums, TitleDescription info) {
        super(hunkChecksums, info);
        this.groupID = 0L;
    }

    public OrderingGroupWithID(String title, String description, List<HunkChecksum> hunks) {
        super(title, description, hunks);
        this.groupID = 0L;
    }

    public OrderingGroupWithID(PullRequestGroup dbGroup, List<HunkChecksum> hunkIds) {
        super(dbGroup, hunkIds);
        this.groupID = 0L;
    }

    public OrderingGroupWithID(String title, String description, List<HunkChecksum> hunks, Boolean defaultGroup) {
        super(title, description, hunks, defaultGroup);
        this.groupID = 0L;
    }

    public OrderingGroupWithID(Long groupID, String title, String description, List<HunkChecksum> hunks, Boolean defaultGroup) {
        super(title, description, hunks, defaultGroup);
        this.groupID = groupID;
    }

}
