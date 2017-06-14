package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;


@JsonIgnoreProperties(ignoreUnknown=true)
public class InstallationID {

    @JsonProperty("id")
    public final String id;

    @JsonCreator
    public InstallationID(@JsonProperty("id") String id) {
        this.id = id;
    }

    public static InstallationID fromJson(JsonNode json) {
        return new InstallationID(json.get("installation").get("id").asText());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InstallationID that = (InstallationID) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "InstallationID{" +
                "id='" + id + '\'' +
                '}';
    }
}
