package previewcode.backend.database;

import io.vavr.collection.List;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import previewcode.backend.DTO.ApproveStatus;
import previewcode.backend.database.model.tables.records.ApprovalRecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static previewcode.backend.database.model.Tables.*;
import static previewcode.backend.services.actions.DatabaseActions.*;


public class DatabaseInterpreter_ApprovalTest extends DatabaseInterpreterTest {

    private static final String hunkChecksum = "jkl";
    private static final String githubUser = "user";
    private static final ApproveStatus statusApproved = ApproveStatus.APPROVED;

    private static final PullRequestID dbPullId = new PullRequestID(42L);

    @BeforeEach
    @Override
    public void setup(DSLContext db) {
        super.setup(db);

        db.insertInto(PULL_REQUEST, PULL_REQUEST.ID, PULL_REQUEST.OWNER, PULL_REQUEST.NAME, PULL_REQUEST.NUMBER)
                .values(dbPullId.id, owner, name, number)
                .execute();

        db.insertInto(GROUPS, GROUPS.TITLE, GROUPS.DESCRIPTION, GROUPS.PULL_REQUEST_ID)
                .values("Group A", "AA", dbPullId.id).execute();

        db.insertInto(HUNK, HUNK.GROUP_ID, HUNK.CHECKSUM, HUNK.PULL_REQUEST_ID)
                .values(1L, "abc", dbPullId.id)
                .values(1L, "def", dbPullId.id)
                .values(1L, "ghi", dbPullId.id)
                .values(1L, "jkl", dbPullId.id)
                .values(1L, "mno", dbPullId.id)
                .execute();
    }

    @Test
    public void approveHunk_insertApproval(DSLContext db){
        eval(setApprove(dbPullId, hunkChecksum, githubUser, statusApproved));

        Integer approveCount = db.selectCount().from(APPROVAL).fetchOne().value1();
        assertThat(approveCount).isEqualTo(1);
    }

    @Test
    public void approveHunk_insertsCorrectData(DSLContext db){
        ApproveHunk approveHunk = setApprove(dbPullId, hunkChecksum, githubUser, statusApproved);
        eval(approveHunk);

        ApprovalRecord approvalRecord = db.selectFrom(APPROVAL).fetchOne();

        assertThat(approvalRecord.getApprover()).isEqualTo(approveHunk.githubUser);
        assertThat(approvalRecord.getStatus()).isEqualTo(approveHunk.status.getApproved());
    }

    @Test
    public void approveHunk_requiresPullRequest(DSLContext db){
        ApproveHunk create = setApprove(new PullRequestID(424242424242L), hunkChecksum, githubUser, statusApproved);
        assertThatExceptionOfType(DatabaseException.class).isThrownBy(() -> eval(create));
    }

    @Test
    public void approveHunk_onDuplicate_updatesStatus(DSLContext db){
        ApproveHunk first = setApprove(dbPullId, hunkChecksum, githubUser, statusApproved);
        ApproveHunk second = setApprove(dbPullId, hunkChecksum, githubUser, ApproveStatus.DISAPPROVED);
        eval(first.then(second));

        Integer approveCount = db.selectCount().from(APPROVAL).fetchOne().value1();
        assertThat(approveCount).isEqualTo(1);

        List<ApproveStatus> fetchedStatuses = List.ofAll(db.selectFrom(APPROVAL).fetch(APPROVAL.STATUS))
                .map(ApproveStatus::fromString);

        assertThat(fetchedStatuses).hasOnlyOneElementSatisfying(
                s -> assertThat(s).isEqualTo(second.status)
        );
    }

    @Test
    void fetchApprovals_requiresHunkID(DSLContext db){
        FetchHunkApprovals action = fetchApprovals(new HunkID(-1L));
        List<HunkApproval> result = eval(action);
        assertThat(result).isEmpty();
    }

    @Test
    void fetchApprovals_returnsAllApprovals(DSLContext db){
        db.insertInto(APPROVAL, APPROVAL.HUNK_ID, APPROVAL.APPROVER, APPROVAL.STATUS)
                .values(1L, "txsmith", ApproveStatus.APPROVED.getApproved())
                .values(1L, "eanker", ApproveStatus.DISAPPROVED.getApproved())
                .values(2L, "txsmith", ApproveStatus.DISAPPROVED.getApproved())
                .execute();

        FetchHunkApprovals action = fetchApprovals(new HunkID(1L));
        List<HunkApproval> result = eval(action);
        assertThat(result).containsOnly(
                new HunkApproval(ApproveStatus.APPROVED, "txsmith"),
                new HunkApproval(ApproveStatus.DISAPPROVED, "eanker")
        );
    }

}