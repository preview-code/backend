package previewcode.backend.database;

import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import previewcode.backend.DTO.PullRequestIdentifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static previewcode.backend.database.model.Tables.*;
import static previewcode.backend.services.actions.DatabaseActions.*;

class DatabaseInterpreter_PullRequestTest extends DatabaseInterpreterTest {

    @Test
    public void fetchPull_selectsFromPullsTable(DSLContext db) throws Exception {
        db.insertInto(PULL_REQUEST, PULL_REQUEST.ID, PULL_REQUEST.OWNER, PULL_REQUEST.NAME, PULL_REQUEST.NUMBER)
                .values(41L, owner, name, number+1)
                .values(42L, owner, name, number)
                .values(43L, owner, name, number-1)
                .execute();

        PullRequestID pullRequestID = eval(fetchPull(pullIdentifier));
        assertThat(pullRequestID.id).isEqualTo(42L);
    }

    @Test
    public void fetchPull_throwsWhenPullIsNotFound() throws Exception {
        PullRequestIdentifier invalidIdentifier = new PullRequestIdentifier("x", "y", 0);

        assertThatExceptionOfType(DatabaseException.class).isThrownBy(
                () -> eval(fetchPull(invalidIdentifier)));
    }

    @Test
    public void insertPull_definitelyInserts(DSLContext db) throws Exception {
        PullRequestID pullRequestID = eval(insertPullIfNotExists(pullIdentifier));
        assertThat(pullRequestID.id).isPositive();

        Integer pullRequestCount = db.selectCount().from(PULL_REQUEST).fetchOne().value1();
        assertThat(pullRequestCount).isEqualTo(1);
    }

    @Test
    public void insertPull_returnsNewId(DSLContext db) throws Exception {
        db.insertInto(PULL_REQUEST, PULL_REQUEST.OWNER, PULL_REQUEST.NAME, PULL_REQUEST.NUMBER)
                .values(owner, name, number+1)
                .values(owner, name, number+2)
                .values(owner, name, number+3)
                .execute();

        PullRequestID pullRequestID = eval(insertPullIfNotExists(pullIdentifier));

        PullRequestID insertedId = new PullRequestID(
                db.select(PULL_REQUEST.ID)
                        .from(PULL_REQUEST)
                        .where(PULL_REQUEST.OWNER.eq(pullIdentifier.owner)
                                .and(PULL_REQUEST.NAME.eq(pullIdentifier.name))
                                .and(PULL_REQUEST.NUMBER.eq(pullIdentifier.number)))
                        .fetchOne().value1()
        );

        assertThat(pullRequestID).isEqualTo(insertedId);
    }

    @Test
    public void insertPull_duplicate_doesNotInsert(DSLContext db) throws Exception {
        PullRequestID existingId = new PullRequestID(
                db.insertInto(PULL_REQUEST, PULL_REQUEST.OWNER, PULL_REQUEST.NAME, PULL_REQUEST.NUMBER)
                .values(pullIdentifier.owner, pullIdentifier.name, pullIdentifier.number)
                .returning(PULL_REQUEST.ID).fetchOne().getId());

        Integer pullRequestCount = db.selectCount().from(PULL_REQUEST).fetchOne().value1();
        assertThat(pullRequestCount).isEqualTo(1);
    }

    @Test
    public void insertPull_duplicate_returnsOldId(DSLContext db) throws Exception {
        PullRequestID existingId = new PullRequestID(
                db.insertInto(PULL_REQUEST, PULL_REQUEST.OWNER, PULL_REQUEST.NAME, PULL_REQUEST.NUMBER)
                        .values(pullIdentifier.owner, pullIdentifier.name, pullIdentifier.number)
                        .returning(PULL_REQUEST.ID).fetchOne().getId());

        PullRequestID pullRequestID = eval(insertPullIfNotExists(pullIdentifier));

        assertThat(pullRequestID).isEqualTo(existingId);
    }
}