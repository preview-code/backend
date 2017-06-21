package previewcode.backend.database;

import previewcode.backend.DTO.ApproveStatus;
import previewcode.backend.database.model.tables.records.ApprovalRecord;

public class HunkApproval {
    public final ApproveStatus approveStatus;
    public final String approver;

    public HunkApproval(ApproveStatus approveStatus, String approver) {
        this.approveStatus = approveStatus;
        this.approver = approver;
    }

    public static HunkApproval fromRecord(ApprovalRecord record) {
        return new HunkApproval(ApproveStatus.fromString(record.getStatus()), record.getApprover());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HunkApproval that = (HunkApproval) o;

        return approveStatus == that.approveStatus && approver.equals(that.approver);
    }

    @Override
    public int hashCode() {
        int result = approveStatus.hashCode();
        result = 31 * result + approver.hashCode();
        return result;
    }
}
