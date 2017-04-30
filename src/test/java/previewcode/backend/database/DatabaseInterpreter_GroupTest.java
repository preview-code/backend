package previewcode.backend.database;

import org.jooq.DSLContext;
import org.jooq.Record3;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import previewcode.backend.database.model.tables.records.GroupsRecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.fail;
import static previewcode.backend.database.model.Tables.GROUPS;
import static previewcode.backend.database.model.Tables.PULL_REQUEST;
import static previewcode.backend.services.actions.DatabaseActions.*;


public class DatabaseInterpreter_GroupTest extends DatabaseInterpreterTest {

    private static final String groupTitle = "Title";
    private static final String groupDescription = "Description";

    private static final PullRequestID dbPullId = new PullRequestID(42L);

    //    private List<PullRequestGroup> groups = List.of(
//            new PullRequestGroup(new GroupID(42L), "Group A", "Description A"),
//            new PullRequestGroup(new GroupID(24L), "Group B", "Description B")
//    );
//
//    private List<OrderingGroup> groupsWithoutHunks = groups.map(group ->
//            new OrderingGroupWithID(group, Lists.newLinkedList())
//    );
//
//    private List<HunkID> hunkIDs = List.of(
//            new HunkID("abcd"), new HunkID("efgh"), new HunkID("ijkl"));
//
//    private List<OrderingGroup> groupsWithHunks = groups.map(group ->
//            new OrderingGroupWithID(group, hunkIDs.map(id -> id.hunkID).toJavaList())
//    );

    @BeforeEach
    @Override
    public void setup(DSLContext db) {
        super.setup(db);

        db.insertInto(PULL_REQUEST, PULL_REQUEST.ID, PULL_REQUEST.OWNER, PULL_REQUEST.NAME, PULL_REQUEST.NUMBER)
                .values(dbPullId.id, owner, name, number)
                .execute();
    }

    @Test
    public void newGroup_insertsGroup(DSLContext db) throws Exception {
        GroupID groupID = eval(newGroup(dbPullId, groupTitle, groupDescription));
        assertThat(groupID.id).isPositive();

        Integer groupCount = db.selectCount().from(GROUPS).fetchOne().value1();
        assertThat(groupCount).isEqualTo(1);
    }

    @Test
    public void newGroup_returnsNewId(DSLContext db) throws Exception {
        db.insertInto(GROUPS, GROUPS.PULL_REQUEST_ID, GROUPS.TITLE, GROUPS.DESCRIPTION)
                .values(dbPullId.id, "A", "B")
                .values(dbPullId.id, "C", "D")
                .values(dbPullId.id, "E", "F")
                .execute();


        GroupID groupID = eval(newGroup(dbPullId, groupTitle, groupDescription));

        GroupID insertedID = new GroupID(db.select(GROUPS.ID).from(GROUPS).where(
                GROUPS.TITLE.eq(groupTitle).and(GROUPS.DESCRIPTION.eq(groupDescription))
        ).fetchOne().value1());

        assertThat(groupID).isEqualTo(insertedID);
    }

    @Test
    public void newGroup_canInsertDuplicates(DSLContext db) throws Exception {
        NewGroup create = newGroup(dbPullId, groupTitle, groupDescription);

        GroupID groupID = eval(create.then(create));

        Integer groupCount = db.selectCount().from(GROUPS).fetchOne().value1();
        assertThat(groupCount).isEqualTo(2);
    }

    @Test
    public void newGroup_insertsCorrectData(DSLContext db) throws Exception {
        NewGroup create = newGroup(dbPullId, groupTitle, groupDescription);
        eval(create);

        GroupsRecord groupsRecord = db.selectFrom(GROUPS).fetchOne();

        assertThat(groupsRecord.getPullRequestId()).isEqualTo(create.pullRequestId.id);
        assertThat(groupsRecord.getTitle()).isEqualTo(create.title);
        assertThat(groupsRecord.getDescription()).isEqualTo(create.description);
    }

    @Test
    public void newGroup_pullRequestMustExist() {
        PullRequestID wrongID = new PullRequestID(0L);
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> eval(newGroup(wrongID, "A", "B")));
    }


    @Test
    public void fetchGroups_returnsAllGroups() {
        fail();
    }

    @Test
    public void fetchGroups_pullRequestMustExist() {
        fail();
    }

    @Test
    public void fetchGroups_fetchesCorrectGroupData() {
        fail();
    }
}
