package previewcode.backend.DTO;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import previewcode.backend.DTO.serializers.JsonToWrappedType;
import previewcode.backend.DTO.serializers.WrappedTypeToJson;

/**
 * DTO representing a group of hunks as stored in the database.
 */
@JsonDeserialize(converter = HunkChecksum.FromJson.class)
@JsonSerialize(converter = HunkChecksum.ToJson.class)
public class HunkChecksum {
    static class FromJson extends JsonToWrappedType<String, HunkChecksum> {}
    static class ToJson extends WrappedTypeToJson<HunkChecksum, String> {}

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
