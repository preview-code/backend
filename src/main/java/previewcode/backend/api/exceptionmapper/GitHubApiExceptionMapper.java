package previewcode.backend.api.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class GitHubApiExceptionMapper extends
    AbstractExceptionMapper<GitHubApiException> {

    @Override
    public Response.Status getStatusCode(GitHubApiException exception) {
        return Response.Status.fromStatusCode(exception.statusCode);
    }

    @Override
    protected String getExposedMessage(GitHubApiException exception) {
        return "Error while calling GitHub API: " + exception.getMessage();
    }
}
