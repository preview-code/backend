package previewcode.backend.api.exceptionmapper;

import okhttp3.HttpUrl;

public class GitHubApiException extends HttpApiException {

    public final HttpUrl url;

    public GitHubApiException(String message, Integer statusCode, HttpUrl url) {
        super("Call to the GitHub API failed with message: " + message, statusCode);
        this.url = url;
    }
}
