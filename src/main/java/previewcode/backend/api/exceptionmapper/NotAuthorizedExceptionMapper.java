package previewcode.backend.api.exceptionmapper;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class NotAuthorizedExceptionMapper extends
        AbstractExceptionMapper<NotAuthorizedException> {

    @Override
    protected String getExposedMessage(NotAuthorizedException exception) {
        return exception.getMessage();
    }

    @Override
    protected Response.Status getStatusCode(NotAuthorizedException exception) {
        return Response.Status.UNAUTHORIZED;
    }
}
