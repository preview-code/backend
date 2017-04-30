package previewcode.backend.database;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing a group of hunks as stored in the database.
 */
public class HunkID {

    @JsonProperty("hunkID")
    public final String hunkID;


    @JsonCreator
    public HunkID(@JsonProperty("hunkID") String hunkID) {
        this.hunkID = hunkID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HunkID hunkID1 = (HunkID) o;

        return hunkID.equals(hunkID1.hunkID);
    }

    @Override
    public int hashCode() {
        return hunkID.hashCode();
    }

    @Override
    public String toString() {
        return "HunkID{" +
                "hunkID='" + hunkID + '\'' +
                '}';
    }
}
