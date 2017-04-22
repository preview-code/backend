package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class GitHubRepository {

    public final String fullName;

    @JsonCreator
    public GitHubRepository(@JsonProperty("full_name") String name) {
        this.fullName = name;
    }
}
