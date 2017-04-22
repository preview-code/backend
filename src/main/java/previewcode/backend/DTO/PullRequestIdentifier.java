package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PullRequestIdentifier {
    public final String owner;
    public final String name;
    public final Integer number;

    @JsonCreator
    public PullRequestIdentifier(
            @JsonProperty("owner") String owner,
            @JsonProperty("name") String name,
            @JsonProperty("number") Integer number) {
        this.owner = owner;
        this.name = name;
        this.number = number;
    }

    public String toGitHubURL() {
        return "https://api.github.com/repos" + owner + "/" + name + "/pulls/" + number;
    }
}
