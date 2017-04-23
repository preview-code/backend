package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class GitHubRepository {

    public final String fullName;
    public final GitHubUser owner;
    public final String name;

    @JsonCreator
    public GitHubRepository(
            @JsonProperty("full_name") String fullName,
            @JsonProperty("owner") GitHubUser owner,
            @JsonProperty("name") String name) {
        this.fullName = fullName;
        this.owner = owner;
        this.name = name;
    }
}
