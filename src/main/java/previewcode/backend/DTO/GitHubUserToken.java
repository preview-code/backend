package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class GitHubUserToken {

    @JsonProperty("token")
    public final String token;

    @JsonCreator
    public GitHubUserToken(@JsonProperty("token") String token) {
        this.token = token;
    }

    public static GitHubUserToken fromString(String token) {
        return new GitHubUserToken(token);
    }
}
