package previewcode.backend.services.actions;

import io.atlassian.fugue.Unit;
import io.vavr.Function2;
import previewcode.backend.DTO.*;

import java.util.function.Function;

import static previewcode.backend.services.actiondsl.ActionDSL.Action;

/**
 * Actions for interacting with the GitHub API
 */
public class GitHubActions {

    public static Action<Unit> authenticateInstallation(InstallationID id) {
        return new AuthenticateInstallation(id);
    }

    public static Function<InstallationID, Action<Unit>> authenticateInstallation =
            GitHubActions::authenticateInstallation;



    public static VerifyWebhookSharedSecret verifyWebHookSecret(String requestBody, String sha1) {
        return new VerifyWebhookSharedSecret(requestBody, sha1);
    }

    public static Function2<String, String, Action<Unit>> verifyWebHookSecret =
            GitHubActions::verifyWebHookSecret;



    public static IsWebHookUserAgent isWebHookUserAgent(String userAgent) {
        return new IsWebHookUserAgent(userAgent);
    }

    public static Function<String, IsWebHookUserAgent> isWebHookUserAgent =
            GitHubActions::isWebHookUserAgent;



    public static GetUser getUser(GitHubUserToken token) {
        return new GetUser(token);
    }

    public static Function<GitHubUserToken, GetUser> getUser = GitHubActions::getUser;


    public static class AuthenticateInstallation extends Action<Unit> {
        public final InstallationID installationID;

        public AuthenticateInstallation(InstallationID installationID) {
            this.installationID = installationID;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            AuthenticateInstallation that = (AuthenticateInstallation) o;

            return installationID.equals(that.installationID);
        }

        @Override
        public int hashCode() {
            return installationID.hashCode();
        }

        @Override
        public String toString() {
            return "AuthenticateInstallation{" +
                    "installationID=" + installationID +
                    '}';
        }
    }

    public static class VerifyWebhookSharedSecret extends Action<Unit> {
        public final String requestBody;
        public final String sha1;

        public VerifyWebhookSharedSecret(String requestBody, String sha1) {
            this.requestBody = requestBody;
            this.sha1 = sha1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            VerifyWebhookSharedSecret that = (VerifyWebhookSharedSecret) o;

            if (!requestBody.equals(that.requestBody)) return false;
            return sha1.equals(that.sha1);
        }

        @Override
        public int hashCode() {
            int result = requestBody.hashCode();
            result = 31 * result + sha1.hashCode();
            return result;
        }
    }

    public static class IsWebHookUserAgent extends Action<Boolean> {
        public final String userAgent;

        public IsWebHookUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            IsWebHookUserAgent that = (IsWebHookUserAgent) o;

            return userAgent.equals(that.userAgent);
        }

        @Override
        public int hashCode() {
            return userAgent.hashCode();
        }
    }

    public static class GetUser extends Action<GitHubUser> {
        public final GitHubUserToken token;

        public GetUser(GitHubUserToken token) {
            this.token = token;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GetUser getUser = (GetUser) o;

            return token.equals(getUser.token);
        }

        @Override
        public int hashCode() {
            return token.hashCode();
        }
    }


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
