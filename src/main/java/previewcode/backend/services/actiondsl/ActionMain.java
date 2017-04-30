package previewcode.backend.services.actiondsl;

import io.atlassian.fugue.Unit;
import previewcode.backend.DTO.*;

import static previewcode.backend.services.actiondsl.ActionDSL.*;
import static previewcode.backend.services.actiondsl.ActionDSL.Action;
import static previewcode.backend.services.actions.GitHubActions.*;
import static previewcode.backend.services.actions.LogActions.*;



class GitHubInterpreter extends Interpreter {

    final GitHubPullRequest pull =
            new GitHubPullRequest("pull title", "these changes are awesome", "https://lol.com/", 123,
                    new PullRequestLinks("http://self", "http://html", "http://issue", "http://comments",
                            "http://statuses", "http://review_comments", "http://review_comment", "http://commits"));

    final GitHubStatus status = new GitHubStatus("pending", "Changes need ordering",
            "preview-code/ordering", "http://status-ordering");


    public GitHubInterpreter() {
        super();

        on(GitHubFetchPullRequest.class).apply(fetchPull -> {
            System.out.println("Pull!");
            return pull;
        });

        on(GitHubGetStatus.class).apply(getAction -> {
            System.out.println("Getting status");
            return status;
        });

        on(GitHubSetStatus.class).apply(toUnit(setAction ->
            System.out.println("Setting status")
        ));

        on(GitHubPostComment.class).apply(toUnit(postAction ->
            System.out.println("Posting comment")
        ));
    }
}

class LogActionInterpreter extends Interpreter {

    public LogActionInterpreter() {
        super();

        on(LogInfo.class).apply(toUnit(logInfo ->
            System.out.println("INFO: " + logInfo.message)
        ));
    }
}

class ActionMain {

    public static void main(String... args) {

        Action<Unit> gitHubAction =
            new GitHubFetchPullRequest("preview-code", "backend", 42) // Get the PR from GitHub
                .then(pullRequest -> new GitHubGetStatus(pullRequest) // Get the status of this PR
                    .then(maybeStatus ->
                            OrderingStatus.fromGitHubStatus(maybeStatus)
                            .map(OrderingStatus::complete)            // If the status exists, set it to 'complete'
                            .map(status ->                            // Then post a comment and the new status
                                    new GitHubPostComment(pullRequest, "This PR is now completed!")
                                    .then(done -> new GitHubSetStatus(pullRequest, status))
                            )
                            .orElse(returnU)));                         // If the status is not there, just do nothing


        Action<Unit> composedAction =
            new GitHubFetchPullRequest("preview-code", "backend", 42)
                .then(p -> new LogInfo("Fetched PR"));

        new Interpreter(new GitHubInterpreter())
            .run(composedAction);
    }

}