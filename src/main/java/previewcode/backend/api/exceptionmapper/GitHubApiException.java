package previewcode.backend.api.exceptionmapper;

public class GitHubApiException extends RuntimeException {

    public final Integer statusCode;

    public GitHubApiException(String message, Integer statusCode) {
        super("Call to the GitHub API failed with message: " + message);
        this.statusCode = statusCode;
    }
}
