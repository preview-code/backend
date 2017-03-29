package previewcode.backend.api.exceptionmapper;

import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * An ExceptionMapper which can catch exceptions and report to the frontend.
 *
 * @param <T>
 *            The exception to report.
 */
public abstract class AbstractExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {

    @Override
    public Response toResponse(final Throwable exception) {
        final UUID id = UUID.randomUUID();
//        log.error(exception.getMessage() + " (" + id + ")", exception);
        return createResponse(exception, id);
    }

    protected Response createResponse(final Throwable exception, final UUID id) {
        final ExceptionResponse exceptionResponse = createResponse(exception);
        exceptionResponse.setUuid(id.toString());

        return Response.status(getStatusCode())
                .type(MediaType.APPLICATION_JSON_TYPE)
                .entity(exceptionResponse)
                .build();
    }

    protected ExceptionResponse createResponse(final Throwable exception) {
        final ExceptionResponse exceptionResponse = new ExceptionResponse();
        exceptionResponse.setMessage(exception.getMessage());
        return exceptionResponse;
    }

    public abstract Status getStatusCode();

    /**
     * The response to the frontend.
     */
    public static class ExceptionResponse {

        /**
         * The unique id for the exception.
         *
         * @param uuid
         *            The new unique id of this response.
         * @return The unique id of this response.
         */
        private String uuid;

        /**
         * The exception message.
         *
         * @param message
         *            The message to inform the user.
         * @return The message of this response.
         */
        private String message;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }

}