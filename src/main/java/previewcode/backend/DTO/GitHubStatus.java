package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO representing a Status from the GitHub API.
 *
 * More info on:
 *  https://developer.github.com/v3/repos/statuses
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class GitHubStatus {
    @JsonProperty("state")
    public final String state;

    @JsonProperty("description")
    public final String description;

    @JsonProperty("context")
    public final String context;

    @JsonProperty("target_url")
    public final String url;

    @JsonCreator
    public GitHubStatus(
            @JsonProperty("state") String state,
            @JsonProperty("description") String description,
            @JsonProperty("context") String context,
            @JsonProperty("target_url") String url) {
        this.state = state;
        this.description = description;
        this.context = context;
        this.url = url;
    }
}