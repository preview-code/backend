package previewcode.backend.api.exceptionmapper;

import javax.ws.rs.core.Response;

public class GitHubApiExceptionMapper extends
    AbstractExceptionMapper<GitHubApiException> {

    @Override
    public Response.Status getStatusCode(GitHubApiException exception) {
        return Response.Status.fromStatusCode(exception.statusCode);
    }
}
