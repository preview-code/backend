package previewcode.backend.services.actions;

import io.atlassian.fugue.Unit;
import previewcode.backend.DTO.GitHubPullRequest;
import previewcode.backend.DTO.GitHubStatus;
import previewcode.backend.DTO.PullRequestIdentifier;

import static previewcode.backend.services.actiondsl.ActionDSL.Action;

public class GitHubActions {

    public static class GitHubGetStatus extends Action<GitHubStatus> {
        public final GitHubPullRequest pullRequest;

        public GitHubGetStatus(GitHubPullRequest pullRequest) {
            this.pullRequest = pullRequest;
        }
    }

    public static class GitHubSetStatus extends Action<Unit> {
        public final GitHubPullRequest pullRequest;
        public final GitHubStatus status;

        public GitHubSetStatus(GitHubPullRequest pullRequest, GitHubStatus status) {
            this.pullRequest = pullRequest;
            this.status = status;
        }
    }

    public static class GitHubFetchPullRequest extends Action<GitHubPullRequest> {
        public final PullRequestIdentifier pull;

        GitHubFetchPullRequest(PullRequestIdentifier pull) {
            this.pull = pull;
        }

        public GitHubFetchPullRequest(String owner, String name, Integer number) {
            this(new PullRequestIdentifier(owner, name, number));
        }
    }

    public static class GitHubPostComment extends Action<Unit> {
        public final String postUrl;
        public final String comment;

        public GitHubPostComment(GitHubPullRequest pull, String comment) {
            this.postUrl = pull.links.comments;
            this.comment = comment;
        }
    }

}
