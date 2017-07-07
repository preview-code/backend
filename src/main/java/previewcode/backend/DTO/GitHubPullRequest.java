package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class GitHubPullRequest {
    public final String body;
    public final String url;
    public final String title;
    public final Integer number;
    public final PullRequestLinks links;
    public final Base base;

    private static final String PREVIEW_URL = "https://preview-code.com/";

    @JsonCreator
    public GitHubPullRequest(
            @JsonProperty("title") String title,
            @JsonProperty("body") String body,
            @JsonProperty("url") String url,
            @JsonProperty("number") Integer number,
            @JsonProperty("base") Base base,
            @JsonProperty("_links") PullRequestLinks links
    ) {
        this.title = title;
        this.body = body;
        this.url = url;
        this.number = number;
        this.links = links;
        this.base = base;
    }

    public String previewCodeUrl() {
        return PREVIEW_URL + base.repo.fullName + "/pulls/" + this.number;
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Base {
        public final GitHubRepository repo;

        @JsonCreator
        public Base(@JsonProperty("repo") GitHubRepository repo) {
            this.repo = repo;
        }
    }
}
