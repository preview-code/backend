package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing a group of hunks as stored in the database.
 */
public class HunkChecksum {

    @JsonProperty("hunkID")
    public final String checksum;


    @JsonCreator
    public HunkChecksum(@JsonProperty("hunkID") String checksum) {
        this.checksum = checksum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HunkChecksum hunkChecksum1 = (HunkChecksum) o;

        return checksum.equals(hunkChecksum1.checksum);
    }

    @Override
    public int hashCode() {
        return checksum.hashCode();
    }

    @Override
    public String toString() {
        return "HunkChecksum{" + checksum + '}';
    }
}