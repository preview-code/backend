package previewcode.backend.api.exceptionmapper;

import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class GitHubApiExceptionMapper extends
    AbstractExceptionMapper<GitHubApiException> {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(GitHubApiExceptionMapper.class);

    @Override
    public Response.Status getStatusCode(GitHubApiException exception) {
        return Response.Status.fromStatusCode(exception.statusCode);
    }

    @Override
    protected String getExposedMessage(GitHubApiException exception) {
        logger.error("Github API error for url: " + exception.url);
        return "Error while calling GitHub API: " + exception.getMessage();
    }
}
