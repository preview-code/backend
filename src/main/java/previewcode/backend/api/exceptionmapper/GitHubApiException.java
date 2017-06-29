package previewcode.backend.api.exceptionmapper;

public class GitHubApiException extends HttpApiException {

    public GitHubApiException(String message, Integer statusCode) {
        super("Call to the GitHub API failed with message: " + message, statusCode);
    }
}
