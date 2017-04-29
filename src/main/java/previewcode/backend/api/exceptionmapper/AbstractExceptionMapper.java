package previewcode.backend.api.exceptionmapper;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.annotation.JSONP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.UUID;

/**
 * An ExceptionMapper which can catch exceptions and report to the frontend.
 *
 * @param <T>
 *            The exception to report.
 */
public abstract class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

    private static final Logger logger = LoggerFactory.getLogger(AbstractExceptionMapper.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Response toResponse(final T exception) {
        logger.error("Unhandled exception in API call:", exception);
        try {
            return createResponse(exception);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert object to JSON", e);
        }
    }

    protected Response createResponse(final T exception) throws JsonProcessingException {
        final ExceptionResponse exceptionResponse = new ExceptionResponse(
                this.getUuid(exception).toString(),
                exception.getMessage()
        );

        return Response.status(this.getStatusCode(exception))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(mapper.writeValueAsString(exceptionResponse))
                .build();
    }

    protected UUID getUuid(T exception) {
        return UUID.randomUUID();
    }

    public abstract Status getStatusCode(final T exception);

}

/**
 * The response to the frontend.
 */
class ExceptionResponse {

    /**
     * The unique id for the exception.
     *
     * @param uuid
     *            The new unique id of this response.
     * @return The unique id of this response.
     */
    @JsonProperty("uuid")
    public final String uuid;

    /**
     * The exception message.
     *
     * @param message
     *            The message to inform the user.
     * @return The message of this response.
     */
    @JsonProperty("message")
    public final String message;



    public ExceptionResponse(String uuid, String message) {
        this.uuid = uuid;
        this.message = message;
    }
}