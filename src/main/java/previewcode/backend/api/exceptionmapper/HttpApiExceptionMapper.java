package previewcode.backend.api.exceptionmapper;

import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class HttpApiExceptionMapper extends
        AbstractExceptionMapper<HttpApiException> {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(HttpApiExceptionMapper.class);

    @Override
    public Response.Status getStatusCode(HttpApiException exception) {
        return Response.Status.fromStatusCode(exception.statusCode);
    }

    @Override
    protected String getExposedMessage(HttpApiException exception) {
        logger.error("Error while calling an external API: " + exception.url);
        return exception.getMessage();
    }
}
