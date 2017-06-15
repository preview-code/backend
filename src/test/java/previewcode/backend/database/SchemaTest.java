package previewcode.backend.database;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import previewcode.backend.database.model.Sequences;
import previewcode.backend.test.helpers.DatabaseTests;

import static org.assertj.core.api.Assertions.*;
import static previewcode.backend.database.model.Sequences.*;
import static previewcode.backend.database.model.Tables.*;

/**
 * Test that the schema model is generated using jOOQ,
 * and that the local database contains only the schema without any rows.
 */
@DatabaseTests
public class SchemaTest {

    @Test
    public void pullRequestTable_isEmpty(DSLContext db) {
        int rows = db.select(PULL_REQUEST.ID, PULL_REQUEST.OWNER, PULL_REQUEST.NAME, PULL_REQUEST.NUMBER)
                    .from(PULL_REQUEST).execute();
        assertThat(rows).isZero();
    }

    @Test
    public void groupsTable_isEmpty(DSLContext db) {
        int rows = db.select(GROUPS.ID, GROUPS.TITLE, GROUPS.DESCRIPTION)
                    .from(GROUPS).execute();
        assertThat(rows).isZero();
    }

    @Test
    public void hunksTable_isEmpty(DSLContext db) {
        int rows = db.select(HUNK.ID, HUNK.GROUP_ID)
                    .from(HUNK).execute();
        assertThat(rows).isZero();
    }

    @Test
    public void approvalsTable_isEmpty(DSLContext db) {
        int rows = db.select(APPROVAL.HUNK_ID, APPROVAL.APPROVER, APPROVAL.STATUS)
                .from(APPROVAL).execute();

        assertThat(rows).isZero();
    }

    @Test
    public void hasSequence_pullRequestId(DSLContext db) {
        db.nextval(SEQ_PK_PULL_REQUEST);
        assertThat(db.currval(SEQ_PK_PULL_REQUEST)).isEqualTo(1L);
    }

    @Test
    public void hasSequence_groupId(DSLContext db) {
        db.nextval(Sequences.SEQ_PK_GROUPS);
        assertThat(db.currval(SEQ_PK_GROUPS)).isEqualTo(1L);
    }

}
