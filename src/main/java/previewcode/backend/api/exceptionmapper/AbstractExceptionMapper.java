package previewcode.backend.api.exceptionmapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Objects;
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
        UUID uuid = this.newUUID(exception);

        logger.error("Unhandled exception in API call:");
        logger.error("  UUID: " + uuid);
        logger.error("  Exception: ", exception);
        try {
            return createResponse(uuid, exception);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Could not convert object to JSON", e);
        }
    }

    protected Response createResponse(final UUID uuid, final T exception) throws JsonProcessingException {
        final ExceptionResponse exceptionResponse = new ExceptionResponse(
                uuid.toString(),
                this.getExposedMessage(exception)
        );

        return Response.status(this.getStatusCode(exception))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(mapper.writeValueAsString(exceptionResponse))
                .build();
    }

    /**
     * Creates a response string to send back over the network.
     * Care should be taken to avoid sending sensitive data back to users.
     */
    protected String getExposedMessage(T exception) {
        return "An error occurred, try again later or contact an administrator.";
    }

    protected UUID newUUID(T exception) {
        return UUID.randomUUID();
    }

    protected Status getStatusCode(final T exception) {
        return Status.INTERNAL_SERVER_ERROR;
    }
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


    @JsonCreator
    public ExceptionResponse(@JsonProperty("uuid") String uuid, @JsonProperty("message") String message) {
        Objects.requireNonNull(uuid, message);
        this.uuid = uuid;
        this.message = message;
    }

    @Override
    public String toString() {
        return "ExceptionResponse{" +
                "message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExceptionResponse that = (ExceptionResponse) o;

        return message.equals(that.message);
    }

    @Override
    public int hashCode() {
        return message.hashCode();
    }
}