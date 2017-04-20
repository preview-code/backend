package previewcode.backend.DTO;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WebhookPullRequest {

    @JsonProperty("body")
    public final String body;

    @JsonIgnore
    public final String url;

    @JsonIgnore
    public final String title;

    @JsonIgnore
    public final Integer number;

    @JsonCreator
    public WebhookPullRequest(
            @JsonProperty("title") String title,
            @JsonProperty("body") String body,
            @JsonProperty("url") String url,
            @JsonProperty("number") Integer number) {
        this.title = title;
        this.body = body;
        this.url = url;
        this.number = number;
    }

    public WebhookPullRequest addPreviewCodeSignature(WebhookRepo repo) {
        if (this.body.contains("[//]: # (PREVIEW_CODE_BEGIN_SIGNATURE)")) {
            return this;
        } else {
            return new WebhookPullRequest(this.title, this.body + this.getSignatureMarkdown(repo), this.url, this.number);
        }
    }

    private String getSignatureMarkdown(WebhookRepo repo) {
        return "\n" +
                "\n" +
                "[//]: # (PREVIEW_CODE_BEGIN_SIGNATURE)\n" +
                "\n" +
                "---\n" +
                "Review this pull request [on Preview Code](https://preview-code.com/projects/" + repo.fullName + "/pulls/" + this.number + "/overview).";
    }
}
