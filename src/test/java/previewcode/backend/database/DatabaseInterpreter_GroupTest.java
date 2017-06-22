package previewcode.backend.database;

import io.vavr.collection.List;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import previewcode.backend.database.model.tables.records.GroupsRecord;
import previewcode.backend.services.actiondsl.Interpreter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertNull;
import static previewcode.backend.database.model.Tables.GROUPS;
import static previewcode.backend.database.model.Tables.HUNK;
import static previewcode.backend.database.model.Tables.PULL_REQUEST;
import static previewcode.backend.services.actiondsl.ActionDSL.*;
import static previewcode.backend.services.actions.DatabaseActions.*;


public class DatabaseInterpreter_GroupTest extends DatabaseInterpreterTest {

    private static final String groupTitle = "Title";
    private static final String groupDescription = "Description";
    private static final Boolean defaultGroup = null;

    @BeforeEach
    @Override
    public void setup(DSLContext db) {
        super.setup(db);

        db.insertInto(PULL_REQUEST, PULL_REQUEST.ID, PULL_REQUEST.OWNER, PULL_REQUEST.NAME, PULL_REQUEST.NUMBER)
                .values(dbPullId.id, owner, name, number)
                .execute();
    }

    @Test
    public void newGroup_insertsGroup(DSLContext db) {
        GroupID groupID = eval(newGroup(dbPullId, groupTitle, groupDescription, null));
        assertThat(groupID.id).isPositive();

        Integer groupCount = db.selectCount().from(GROUPS).fetchOne().value1();
        assertThat(groupCount).isEqualTo(1);
    }

    @Test
    public void newGroup_returnsNewId(DSLContext db){
        db.insertInto(GROUPS)
                .columns(GROUPS.PULL_REQUEST_ID, GROUPS.TITLE, GROUPS.DESCRIPTION)
                .values(dbPullId.id, "A", "B")
                .values(dbPullId.id, "C", "D")
                .values(dbPullId.id, "E", "F")
                .execute();


        GroupID groupID = eval(newGroup(dbPullId, groupTitle, groupDescription, defaultGroup));

        GroupID insertedID = new GroupID(db.select(GROUPS.ID).from(GROUPS).where(
                GROUPS.TITLE.eq(groupTitle).and(GROUPS.DESCRIPTION.eq(groupDescription))
        ).fetchOne().value1());

        assertThat(groupID).isEqualTo(insertedID);
    }

    @Test
<<<<<<< 84cd7a0944beeac91449e2112a18ddba6b30cff9
    public void newGroup_canInsertDuplicates(DSLContext db){
        NewGroup create = newGroup(dbPullId, groupTitle, groupDescription);
=======
    public void newGroup_canInsertDuplicates(DSLContext db) throws Exception {
        NewGroup create = newGroup(dbPullId, groupTitle, groupDescription, defaultGroup);
>>>>>>> On webhook opened all hunks are added to the database
        eval(create.then(create));

        Integer groupCount = db.selectCount().from(GROUPS).fetchOne().value1();
        assertThat(groupCount).isEqualTo(2);
    }

    @Test
<<<<<<< 84cd7a0944beeac91449e2112a18ddba6b30cff9
    public void newGroup_insertsCorrectData(DSLContext db){
        NewGroup create = newGroup(dbPullId, groupTitle, groupDescription);
=======
    public void newGroup_insertsCorrectData(DSLContext db) throws Exception {
        NewGroup create = newGroup(dbPullId, groupTitle, groupDescription, defaultGroup);
>>>>>>> On webhook opened all hunks are added to the database
        eval(create);

        GroupsRecord groupsRecord = db.selectFrom(GROUPS).fetchOne();

        assertThat(groupsRecord.getPullRequestId()).isEqualTo(create.pullRequestId.id);
        assertThat(groupsRecord.getTitle()).isEqualTo(create.title);
        assertThat(groupsRecord.getDescription()).isEqualTo(create.description);
        assertThat(groupsRecord.getDefaultGroup()).isEqualTo(defaultGroup);
    }

    @Test
    public void newGroup_insertDefault(DSLContext db) throws Exception {
        NewGroup create = newGroup(dbPullId, groupTitle, groupDescription, true);
        eval(create);

        GroupsRecord groupsRecord = db.selectFrom(GROUPS).fetchOne();

        assertThat(groupsRecord.getDefaultGroup().booleanValue());
        assertThat(groupsRecord.getDefaultGroup()).isEqualTo(true);

    }


    @Test
    public void newGroup_pullRequestMustExist() {
        PullRequestID wrongID = new PullRequestID(0L);
        assertThatExceptionOfType(DataAccessException.class)
                .isThrownBy(() -> eval(newGroup(wrongID, "A", "B", defaultGroup)));
    }


    @Test
    public void fetchGroups_returnsAllGroups(DSLContext db){
        db.insertInto(PULL_REQUEST, PULL_REQUEST.ID, PULL_REQUEST.OWNER, PULL_REQUEST.NAME, PULL_REQUEST.NUMBER)
                .values(dbPullId.id+1, "xyz", "pqr", number)
                .execute();

        db.insertInto(GROUPS)
                .columns(GROUPS.PULL_REQUEST_ID, GROUPS.TITLE, GROUPS.DESCRIPTION)
                .values(dbPullId.id, "A", "B")
                .values(dbPullId.id, "C", "D")
                .values(dbPullId.id, "E", "F")
                .values(dbPullId.id+1, "X", "Y")
                .execute();

        List<String> fetchedTitles = eval(fetchGroups(dbPullId)).map(g -> g.title);
        assertThat(fetchedTitles).containsOnly("A", "C", "E");
    }

    @Test
    public void fetchGroups_invalidPull_returnsNoResults(){
        PullRequestID invalidID = new PullRequestID(-1L);
        List<PullRequestGroup> groups = eval(fetchGroups(invalidID));
        assertThat(groups).isEmpty();
    }

    @Test
    public void fetchGroups_fetchesCorrectGroupData(DSLContext db){
        db.insertInto(GROUPS)
                .columns(GROUPS.PULL_REQUEST_ID, GROUPS.TITLE, GROUPS.DESCRIPTION)
                .values(dbPullId.id, "A", "B")
                .execute();

        PullRequestGroup group = eval(fetchGroups(dbPullId)).get(0);
        assertThat(group.title).isEqualTo("A");
        assertThat(group.description).isEqualTo("B");
    }

    @Test
    public void fetchGroups_hasHunkFetchingAction(DSLContext db){
        db.insertInto(GROUPS)
                .columns(GROUPS.ID, GROUPS.PULL_REQUEST_ID, GROUPS.TITLE, GROUPS.DESCRIPTION)
                .values(1234L, dbPullId.id, "A", "B")
                .execute();

        Action<?> hunkFetchAction = eval(fetchGroups(dbPullId)).get(0).fetchHunks;

        Interpreter i = interpret()
                .on(FetchHunksForGroup.class).stop(
                        action -> assertThat(action.groupID).isEqualTo(new GroupID(1234L)));

        assertThatExceptionOfType(Interpreter.StoppedException.class)
                .isThrownBy(() -> i.unsafeEvaluate(hunkFetchAction));
    }

    @Test
    void deleteGroup_groupID_mustExist(DSLContext db){
        db.insertInto(GROUPS)
                .columns(GROUPS.ID, GROUPS.PULL_REQUEST_ID, GROUPS.TITLE, GROUPS.DESCRIPTION)
                .values(1234L, dbPullId.id, "A", "B")
                .execute();

        eval(delete(new GroupID(984351L)));

        Integer groupCount = db.selectCount().from(GROUPS).fetchOne().value1();
        assertThat(groupCount).isOne();
    }

    @Test
    void deleteGroup_cascades_deletesHunks(DSLContext db){
        db.insertInto(GROUPS)
                .columns(GROUPS.ID, GROUPS.PULL_REQUEST_ID, GROUPS.TITLE, GROUPS.DESCRIPTION)
                .values(1234L, dbPullId.id, "A", "B")
                .execute();

        db.insertInto(HUNK)
                .columns(HUNK.CHECKSUM, HUNK.GROUP_ID, HUNK.PULL_REQUEST_ID)
                .values("abc", 1234L, dbPullId.id)
                .execute();

        eval(delete(new GroupID(1234L)));

        Integer hunkCount = db.selectCount().from(HUNK).fetchOne().value1();
        assertThat(hunkCount).isZero();
    }

    @Test
    void deleteGroup_deletesGroup(DSLContext db){
        db.insertInto(GROUPS)
                .columns(GROUPS.ID, GROUPS.PULL_REQUEST_ID, GROUPS.TITLE, GROUPS.DESCRIPTION)
                .values(1234L, dbPullId.id, "A", "B")
                .execute();

        eval(delete(new GroupID(1234L)));

        Integer groupCount = db.selectCount().from(GROUPS).fetchOne().value1();
        assertThat(groupCount).isZero();
    }
}
