package previewcode.backend.database;

import io.vavr.collection.List;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import previewcode.backend.DTO.ApproveStatus;
import previewcode.backend.database.model.tables.records.ApprovalRecord;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static previewcode.backend.database.model.Tables.APPROVAL;
import static previewcode.backend.database.model.Tables.PULL_REQUEST;
import static previewcode.backend.services.actions.DatabaseActions.*;


public class DatabaseInterpreter_ApprovalTest extends DatabaseInterpreterTest {

    private static final String hunkID = "hunkID";
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
    }

    @Test
    public void approveHunk_insertApproval(DSLContext db) throws Exception {
        eval(setApprove(dbPullId, hunkID, githubUser, statusApproved));

        Integer approveCount = db.selectCount().from(APPROVAL).fetchOne().value1();
        assertThat(approveCount).isEqualTo(1);
    }

    @Test
    public void approveHunk_insertsCorrectData(DSLContext db) throws Exception {
        ApproveHunk create = setApprove(dbPullId, hunkID, githubUser, statusApproved);
        eval(create);

        ApprovalRecord approvalRecord = db.selectFrom(APPROVAL).fetchOne();

        assertThat(approvalRecord.getPullRequestId()).isEqualTo(create.pullRequestID.id);
        assertThat(approvalRecord.getHunkId()).isEqualTo(create.hunkId);
        assertThat(approvalRecord.getApprover()).isEqualTo(create.githubUser);
        assertThat(approvalRecord.getStatus()).isEqualTo(create.status.getApproved());
    }

    @Test
    public void approveHunk_requiresPullRequest(DSLContext db) throws Exception {
        ApproveHunk create = setApprove(new PullRequestID(424242424242L), hunkID, githubUser, statusApproved);
        assertThatExceptionOfType(DataAccessException.class).isThrownBy(() -> eval(create));
    }

    @Test
    public void approveHunk_onDuplicate_updatesStatus(DSLContext db) throws Exception {
        ApproveHunk first = setApprove(dbPullId, hunkID, githubUser, statusApproved);
        ApproveHunk second = setApprove(dbPullId, hunkID, githubUser, ApproveStatus.DISAPPROVED);
        eval(first.then(second));

        Integer approveCount = db.selectCount().from(APPROVAL).fetchOne().value1();
        assertThat(approveCount).isEqualTo(1);

        List<ApproveStatus> fetchedStatuses = List.ofAll(db.selectFrom(APPROVAL).fetch(APPROVAL.STATUS))
                .map(ApproveStatus::fromString);

        assertThat(fetchedStatuses).hasOnlyOneElementSatisfying(
                s -> assertThat(s).isEqualTo(second.status)
        );
    }
}