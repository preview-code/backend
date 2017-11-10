package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class InstallationToken {

    @JsonProperty("token")
    public final String token;

    @JsonCreator
    public InstallationToken(@JsonProperty("token") String token) {
        this.token = token;
    }
}
