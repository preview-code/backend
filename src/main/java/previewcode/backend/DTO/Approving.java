package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Approving {
    APPROVED("approved"),
    DISAPPROVED("disapproved"),
    NONE("none");

    private String approved;

    Approving(String approved) {
        this.approved = approved;
    }

    @JsonCreator
    public static Approving fromString(String approved) {
        return approved == null
                ? null
                : Approving.valueOf(approved.toUpperCase());
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