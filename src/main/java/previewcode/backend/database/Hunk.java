package previewcode.backend.database;

import io.vavr.collection.List;
import previewcode.backend.DTO.HunkChecksum;
import previewcode.backend.database.model.tables.records.HunkRecord;
import previewcode.backend.services.actiondsl.ActionDSL.Action;

import static previewcode.backend.services.actions.DatabaseActions.fetchApprovals;

public class Hunk {
    public final HunkID id;
    public final GroupID groupID;
    public final HunkChecksum checksum;
    public final Action<List<HunkApproval>> fetchApprovals;

    public Hunk(HunkID id, GroupID groupID, HunkChecksum checksum) {
        this.id = id;
        this.groupID = groupID;
        this.checksum = checksum;
        this.fetchApprovals = fetchApprovals(id);
    }

    public static Hunk fromRecord(HunkRecord record) {
        return new Hunk(new HunkID(record.getId()), new GroupID(record.getGroupId()), new HunkChecksum(record.getChecksum()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hunk hunk = (Hunk) o;

        return id.equals(hunk.id) && groupID.equals(hunk.groupID) && checksum.equals(hunk.checksum);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + groupID.hashCode();
        result = 31 * result + checksum.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Hunk{" +
                "id=" + id +
                ", groupID=" + groupID +
                ", checksum=" + checksum +
                '}';
    }
}
