package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class GitHubUser {
    public final Integer id;
    public final String login;

    @JsonCreator
    public GitHubUser(@JsonProperty("id") Integer id, @JsonProperty("login") String login) {
        this.id = id;
        this.login = login;
    }
}
