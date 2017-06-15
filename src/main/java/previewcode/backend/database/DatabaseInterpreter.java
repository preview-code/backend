package previewcode.backend.database;

import io.vavr.collection.List;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.postgresql.util.PSQLException;
import previewcode.backend.DTO.HunkChecksum;
import previewcode.backend.services.actiondsl.Interpreter;

import javax.inject.Inject;

import static previewcode.backend.database.model.Tables.*;
import static previewcode.backend.services.actiondsl.ActionDSL.toUnit;
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
        on(AssignHunkToGroup.class).apply(toUnit(this::assignHunk));
        on(FetchGroupsForPull.class).apply(this::fetchGroups);
        on(FetchHunksForGroup.class).apply(this::fetchHunks);
        // TODO: on(FetchHunkApprovals.class).apply(this::fetchApprovals);
        on(DeleteGroup.class).apply(toUnit(this::deleteGroup));
        on(ApproveHunk.class).apply(toUnit(this::approveHunk));
    }

    private List<HunkChecksum> fetchHunks(FetchHunksForGroup action) {
        return List.ofAll(db.selectFrom(HUNK)
                .where(HUNK.GROUP_ID.eq(action.groupID.id))
                .fetch(HUNK.CHECKSUM)).map(HunkChecksum::new);
    }

    protected void deleteGroup(DeleteGroup deleteGroup) {
        db.deleteFrom(GROUPS)
                .where(GROUPS.ID.eq(deleteGroup.groupID.id))
                .execute();
    }

    protected void assignHunk(AssignHunkToGroup action) {
        db.insertInto(HUNK)
                .columns(HUNK.GROUP_ID, HUNK.CHECKSUM)
                .values(action.groupID.id, action.hunkChecksum)
                .execute();
    }

    protected void approveHunk(ApproveHunk action) {

        int result = db.insertInto(APPROVAL)
                .columns(APPROVAL.HUNK_ID, APPROVAL.APPROVER, APPROVAL.STATUS)
                .select(
                        db.select(HUNK.ID, DSL.val(action.githubUser), DSL.val(action.status.getApproved()))
                                .from(GROUPS)
                                .join(HUNK).on(GROUPS.ID.eq(HUNK.GROUP_ID))
                                .where(GROUPS.PULL_REQUEST_ID.eq(action.pullRequestID.id)
                                        .and(HUNK.CHECKSUM.eq(action.hunkChecksum))
                                )
                ).onDuplicateKeyUpdate()
                .set(APPROVAL.STATUS, action.status.getApproved())
                .execute();

        if (result == 0) {
            throw new DatabaseException("Pull request or hunk checksum not found.");
        }
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
