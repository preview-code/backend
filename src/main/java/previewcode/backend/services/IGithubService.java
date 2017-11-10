package previewcode.backend.services;

import io.atlassian.fugue.Unit;
import org.kohsuke.github.GHMyself;
import previewcode.backend.DTO.*;
import previewcode.backend.api.exceptionmapper.NoTokenException;
import previewcode.backend.services.actiondsl.ActionDSL;

import java.io.IOException;

import static previewcode.backend.services.actiondsl.ActionDSL.with;
import static previewcode.backend.services.actions.GitHubActions.*;
import static previewcode.backend.services.actions.RequestContextActions.*;

public interface IGithubService {
    GHMyself getLoggedInUser() throws IOException;

    PrNumber createPullRequest(String owner, String name, PRbody body);

    GitHubPullRequest fetchPullRequest(PullRequestIdentifier identifier) throws IOException;

    void setPRStatus(GitHubPullRequest pullRequest, ApproveStatus status) throws IOException;

    public static class V2 {

        private static final String GITHUB_WEBHOOK_SECRET_HEADER = "X-Hub-Signature";
        private static final String TOKEN_PARAMETER = "access_token";

        public ActionDSL.Action<Unit> authenticate() {
            return getUserAgent.then(isWebHookUserAgent).then(isWebHook -> {
                if (isWebHook) {
                    return with(getRequestBody)
                            .and(getHeader(GITHUB_WEBHOOK_SECRET_HEADER))
                            .then(verifyWebHookSecret)
                            .then(getJsonBody)
                            .map(InstallationID::fromJson)
                            .then(authenticateInstallation);
                } else {
                    return getQueryParam(TOKEN_PARAMETER)
                            .map(o -> o.getOrElseThrow(NoTokenException::new))
                            .map(GitHubUserToken::fromString)
                            .then(getUser)
                            .toUnit();
                }
            });
        }


    }
}
