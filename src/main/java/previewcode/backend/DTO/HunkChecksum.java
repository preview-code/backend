package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import previewcode.backend.DTO.deserialize.WrappedTypeConverter;

/**
 * DTO representing a group of hunks as stored in the database.
 */
@JsonDeserialize(converter = HunkChecksum.Converter.class)
public class HunkChecksum {
    static class Converter extends WrappedTypeConverter<String, HunkChecksum> {}

    @JsonProperty("hunkID")
    public final String checksum;

    public HunkChecksum(String checksum) {
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
