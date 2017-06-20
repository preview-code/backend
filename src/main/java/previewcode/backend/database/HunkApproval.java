package previewcode.backend.database;

import previewcode.backend.DTO.ApproveStatus;

public class HunkApproval {
    public final ApproveStatus approveStatus;
    public final String approver;

    public HunkApproval(ApproveStatus approveStatus, String approver) {
        this.approveStatus = approveStatus;
        this.approver = approver;
    }
}
