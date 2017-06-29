package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Objects;

public enum ApproveStatus {
    APPROVED("approved"),
    DISAPPROVED("disapproved"),
    NONE("none");

    private String approved;

    ApproveStatus(String approved) {
        this.approved = approved;
    }

    @JsonCreator
    public static ApproveStatus fromString(String approved) {
        Objects.requireNonNull(approved);
        return ApproveStatus.valueOf(approved.toUpperCase());
    }

    @JsonValue
    public String getApproved() {
        return approved.toLowerCase();
    }

    @Override
    public String toString() {
        return approved;
    }
}