package previewcode.backend.database;

import io.atlassian.fugue.Unit;
import io.vavr.Tuple2;
import io.vavr.collection.List;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import previewcode.backend.DTO.HunkChecksum;
import previewcode.backend.services.actiondsl.ActionDSL;
import previewcode.backend.services.actiondsl.ActionDSL.Action;

import static org.assertj.core.api.Assertions.*;
import static previewcode.backend.database.model.Tables.GROUPS;
import static previewcode.backend.database.model.Tables.HUNK;
import static previewcode.backend.database.model.Tables.PULL_REQUEST;
import static previewcode.backend.services.actions.DatabaseActions.*;

public class DatabaseInterpreter_HunksTest extends DatabaseInterpreterTest {

    private static final String hunkChecksum = "ABCDEF";

    private static final GroupID group_A_id = new GroupID(0L);
    private static final GroupID group_B_id = new GroupID(1L);

    @BeforeEach
    @Override
    public void setup(DSLContext db) {
        super.setup(db);
        db.insertInto(PULL_REQUEST, PULL_REQUEST.ID, PULL_REQUEST.OWNER, PULL_REQUEST.NAME, PULL_REQUEST.NUMBER)
                .values(dbPullId.id, owner, name, number)
                .execute();

        db.insertInto(GROUPS)
                .columns(GROUPS.ID, GROUPS.PULL_REQUEST_ID, GROUPS.TITLE, GROUPS.DESCRIPTION)
                .values(group_A_id.id, dbPullId.id, "A", "B")
                .values(group_B_id.id, dbPullId.id, "C", "D")
                .execute();
    }

    @Test
    public void assignHunk_groupMustExist() throws Exception {
        GroupID invalidID = new GroupID(-1L);
        assertThatExceptionOfType(DatabaseException.class)
                .isThrownBy(() -> eval(assignToGroup(invalidID, hunkChecksum)));
    }

    @Test
    public void assignHunk_cannotAssignTwice_toSameGroup() {
        AssignHunkToGroup assign = assignToGroup(group_A_id, hunkChecksum);

        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> eval(assign.then(assign)));
    }

    @Test
    public void assignHunk_insertsIntoHunkTable(DSLContext db) throws Exception {
        eval(assignToGroup(group_A_id, hunkChecksum));

        assertThat(
                db.selectCount().from(HUNK).fetchOneInto(Integer.class)
        ).isOne();
    }

    @Test
    public void assignHunk_cannotAssignTwice_toDifferentGroup(DSLContext db) throws Exception {
        Action<?> assignDouble = assignToGroup(group_A_id, hunkChecksum).then(assignToGroup(group_B_id, hunkChecksum));
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() ->
                        eval(assignDouble)
        );
    }

    @Test
    public void assignHunk_insertsCorrectData(DSLContext db) throws Exception {
        eval(assignToGroup(group_A_id, hunkChecksum));

        Tuple2 record = db.select(HUNK.GROUP_ID, HUNK.CHECKSUM).from(HUNK).fetchOneInto(Tuple2.class);
        assertThat(record).isEqualTo(new Tuple2<>(group_A_id.id, hunkChecksum));
    }

    @Test
    void fetchHunks_forUnknownGroup_returnsEmpty() throws Exception {
        List<?> hunks = eval(fetchHunks(new GroupID(-1L)));
        assertThat(hunks).isEmpty();
    }

    @Test
    void fetchHunks_returnsAllHunks(DSLContext db) throws Exception {
        db.insertInto(HUNK)
                .columns(HUNK.CHECKSUM, HUNK.GROUP_ID, HUNK.PULL_REQUEST_ID)
                .values("X", group_A_id.id, dbPullId.id)
                .values("Y", group_A_id.id, dbPullId.id)
                .values("Z", group_B_id.id, dbPullId.id)
                .execute();

        List<HunkChecksum> hunkIDS = eval(fetchHunks(group_A_id)).map(h -> h.checksum);
        assertThat(hunkIDS).containsOnly(new HunkChecksum("X"), new HunkChecksum("Y"));
    }

}
