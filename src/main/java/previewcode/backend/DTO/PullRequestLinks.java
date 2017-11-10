package previewcode.backend.DTO;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PullRequestLinks {
    public final String self;
    public final String html;
    public final String issue;
    public final String comments;
    public final String commits;
    public final String reviewComments;
    public final String reviewComment;
    public final String statuses;

    @JsonCreator
    public PullRequestLinks(
            @JsonProperty("self") HRef self,
            @JsonProperty("html") HRef html,
            @JsonProperty("issue") HRef issue,
            @JsonProperty("comments") HRef comments,
            @JsonProperty("statuses") HRef statuses,
            @JsonProperty("review_comments") HRef reviewComments,
            @JsonProperty("review_comment") HRef reviewComment,
            @JsonProperty("commits") HRef commits) {

        this.self = self.href;
        this.html = html.href;
        this.issue = issue.href;
        this.comments = comments.href;
        this.commits = commits.href;
        this.reviewComments = reviewComments.href;
        this.reviewComment = reviewComment.href;
        this.statuses = statuses.href;
    }

    public PullRequestLinks(String self, String html, String issue,
                            String comments, String statuses, String reviewComments,
                            String reviewComment, String commits) {
        this.self = self;
        this.html = html;
        this.issue = issue;
        this.comments = comments;
        this.commits = commits;
        this.reviewComments = reviewComments;
        this.reviewComment = reviewComment;
        this.statuses = statuses;
    }
}

class HRef {
    public String href;
}
