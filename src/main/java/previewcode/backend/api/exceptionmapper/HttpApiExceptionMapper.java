package previewcode.backend.api.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class HttpApiExceptionMapper extends
        AbstractExceptionMapper<HttpApiException> {

    @Override
    public Response.Status getStatusCode(HttpApiException exception) {
        return Response.Status.fromStatusCode(exception.statusCode);
    }

    @Override
    protected String getExposedMessage(HttpApiException exception) {
        return exception.getMessage();
    }
}
