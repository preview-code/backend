package previewcode.backend.api.exceptionmapper;

import javax.ws.rs.core.Response;

public class NoTokenExceptionMapper extends AbstractExceptionMapper<NoTokenException> {

    @Override
    public Response.Status getStatusCode(NoTokenException exception) {
        return Response.Status.UNAUTHORIZED;
    }

    @Override
    protected String getExposedMessage(NoTokenException exception) {
        return exception.getMessage();
    }
}
