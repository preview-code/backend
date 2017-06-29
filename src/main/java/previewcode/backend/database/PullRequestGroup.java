package previewcode.backend.database;

import io.vavr.collection.List;
import previewcode.backend.database.model.tables.records.GroupsRecord;

import static previewcode.backend.services.actiondsl.ActionDSL.*;
import static previewcode.backend.services.actions.DatabaseActions.*;

/**
 * DTO representing a group of hunks as stored in the database.
 */
public class PullRequestGroup {

    /**
     * The id of the group
     */
    public final GroupID id;

    /**
     * The title of the object
     */
    public final String title;

    /**
     * The description of the object
     */
    public final String description;

    /**
     * If the group is the default group or not
     */
    public final Boolean defaultGroup;

    /**
     * Evaluating this action should result in the list of
     * hunk-ids of all hunks in this group.
     */
    public final Action<List<Hunk>> fetchHunks;

    public PullRequestGroup(GroupID id, String title, String description, Boolean defaultGroup) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.fetchHunks = fetchHunks(id);
        this.defaultGroup = defaultGroup;
    }

    public static PullRequestGroup fromRecord(GroupsRecord record) {
        return new PullRequestGroup(new GroupID(record.getId()), record.getTitle(), record.getDescription(), record.getDefaultGroup());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PullRequestGroup that = (PullRequestGroup) o;

        return id.equals(that.id) && title.equals(that.title) && description.equals(that.description);
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PullRequestGroup{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
