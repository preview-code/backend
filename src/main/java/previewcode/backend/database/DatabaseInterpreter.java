package previewcode.backend.database;

import io.atlassian.fugue.Unit;
import io.vavr.collection.List;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.postgresql.util.PSQLException;
import previewcode.backend.services.actiondsl.Interpreter;
import previewcode.backend.services.actions.DatabaseActions;

import javax.inject.Inject;

import static previewcode.backend.database.model.Tables.*;
import static previewcode.backend.services.actiondsl.ActionDSL.toUnit;
import static previewcode.backend.services.actiondsl.ActionDSL.unit;
import static previewcode.backend.services.actions.DatabaseActions.*;

public class DatabaseInterpreter extends Interpreter {

    private final DSLContext db;
    private static final String UNIQUE_CONSTRAINT_VIOLATION = "23505";

    @Inject
    public DatabaseInterpreter(DSLContext db) {
        this.db = db;
        on(FetchPull.class).apply(this::fetchPullRequest);
        on(InsertPullIfNotExists.class).apply(this::insertPull);
        on(NewGroup.class).apply(this::insertNewGroup);
        on(FetchGroupsForPull.class).apply(this::fetchGroups);
        on(AssignHunkToGroup.class).apply(toUnit(this::assignHunk));
        on(DeleteGroup.class).apply(toUnit(this::deleteGroup));
        on(ApproveHunk.class).apply(toUnit(this::approveHunk));
    }

    protected void deleteGroup(DeleteGroup deleteGroup) {
        db.deleteFrom(GROUPS)
                .where(GROUPS.ID.eq(deleteGroup.groupID.id))
                .execute();
    }

    protected void assignHunk(AssignHunkToGroup action) {
        db.insertInto(HUNK)
                .columns(HUNK.GROUP_ID, HUNK.ID)
                .values(action.groupID.id, action.hunkIdentifier)
                .execute();
    }

    protected void approveHunk(ApproveHunk action) {
      db.insertInto(APPROVAL)
              .columns(APPROVAL.PULL_REQUEST_ID, APPROVAL.HUNK_ID, APPROVAL.APPROVER, APPROVAL.STATUS)
              .values(action.pullRequestID.id, action.hunkId, action.githubUser, action.status.getApproved())
              .onDuplicateKeyUpdate()
              .set(APPROVAL.STATUS, action.status.getApproved())
              .execute();
    }


    protected PullRequestID insertPull(InsertPullIfNotExists action) {
        try {
            return new PullRequestID(
                    db.insertInto(PULL_REQUEST, PULL_REQUEST.OWNER, PULL_REQUEST.NAME, PULL_REQUEST.NUMBER)
                            .values(action.owner, action.name, action.number)
                            .returning(PULL_REQUEST.ID)
                            .fetchOne().getId()
            );
        } catch (DataAccessException e) {
            if (e.getCause() instanceof PSQLException &&
                    ((PSQLException) e.getCause()).getSQLState().equals(UNIQUE_CONSTRAINT_VIOLATION)) {
                return this.fetchPullRequest(fetchPull(action.owner, action.name, action.number));
            } else {
                throw e;
            }
        }
    }

    protected PullRequestID fetchPullRequest(FetchPull action) {
        Record1<Long> pullIdRecord = db.select(PULL_REQUEST.ID)
                .from(PULL_REQUEST)
                .where(  PULL_REQUEST.OWNER.eq(action.owner)
                    .and(PULL_REQUEST.NAME.eq(action.name))
                    .and(PULL_REQUEST.NUMBER.eq(action.number)))
                .fetchAny();

        if (pullIdRecord != null) {
            return new PullRequestID(pullIdRecord.value1());
        } else {
            throw new DatabaseException("Could not find pull request with identifier: " +
                    action.owner + "/" + action.name + "/" + action.number);
        }
    }

    protected GroupID insertNewGroup(NewGroup newGroup) {
        return new GroupID(
                db.insertInto(GROUPS, GROUPS.PULL_REQUEST_ID, GROUPS.TITLE, GROUPS.DESCRIPTION)
                .values(newGroup.pullRequestId.id, newGroup.title, newGroup.description)
                .returning(GROUPS.ID).fetchOne().getId()
        );
    }

    protected List<PullRequestGroup> fetchGroups(FetchGroupsForPull action) {
        return List.ofAll(db.selectFrom(GROUPS)
                .where(GROUPS.PULL_REQUEST_ID.eq(action.pullRequestID.id))
                .fetch(PullRequestGroup::fromRecord));
    }
}
