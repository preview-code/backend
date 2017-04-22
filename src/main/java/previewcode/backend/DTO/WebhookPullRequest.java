package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WebhookPullRequest {
    public final String body;
    public final String url;
    public final String title;
    public final Integer number;
    public final PullRequestLinks links;


    @JsonCreator
    public WebhookPullRequest(
            @JsonProperty("title") String title,
            @JsonProperty("body") String body,
            @JsonProperty("url") String url,
            @JsonProperty("number") Integer number,
            @JsonProperty("_links") PullRequestLinks links) {
        this.title = title;
        this.body = body;
        this.url = url;
        this.number = number;
        this.links = links;
    }
}
