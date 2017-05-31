package previewcode.backend.database;

import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import previewcode.backend.DTO.ApproveStatus;
import previewcode.backend.database.model.tables.records.ApprovalRecord;
import previewcode.backend.database.model.tables.records.GroupsRecord;
import previewcode.backend.services.actions.DatabaseActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static previewcode.backend.database.model.Tables.APPROVAL;
import static previewcode.backend.database.model.Tables.GROUPS;
import static previewcode.backend.database.model.Tables.PULL_REQUEST;
import static previewcode.backend.services.actions.DatabaseActions.*;


public class DatabaseInterpreter_ApprovalTest extends DatabaseInterpreterTest {

    private static final String hunkID = "hunkID";
    private static final String githubUser = "user";
    private static final ApproveStatus approve = ApproveStatus.APPROVED;

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
        eval(setApprove(dbPullId, hunkID, githubUser, approve));

        Integer approveCount = db.selectCount().from(APPROVAL).fetchOne().value1();
        assertThat(approveCount).isEqualTo(1);
    }

    @Test
    public void approveHunk_insertsCorrectData(DSLContext db) throws Exception {
        DatabaseActions.ApproveHunk create = setApprove(dbPullId, hunkID, githubUser, approve);
        eval(create);

        ApprovalRecord approvalRecord = db.selectFrom(APPROVAL).fetchOne();

        assertThat(approvalRecord.getPullRequestId()).isEqualTo(create.pullRequestID.id);
        assertThat(approvalRecord.getHunkId()).isEqualTo(create.hunkId);
        assertThat(approvalRecord.getApprover()).isEqualTo(create.githubUser);
        assertThat(approvalRecord.getStatus()).isEqualTo(create.approve.getApproved());
    }

    @Test
    public void approveHunk_requiresPullRequest(DSLContext db) throws Exception {
        DatabaseActions.ApproveHunk create = setApprove(new PullRequestID(424242424242L), hunkID, githubUser, approve);

        assertThatExceptionOfType(DataAccessException.class).isThrownBy(() -> eval(create));

    }

    @Test
    public void approveHunk_uniqueConstraints(DSLContext db) throws Exception {
        DatabaseActions.ApproveHunk create = setApprove(dbPullId, hunkID, githubUser, approve);

        assertThatExceptionOfType(DataAccessException.class).isThrownBy(() -> eval(create.then(create)));

    }

}