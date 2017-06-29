package previewcode.backend.api.exceptionmapper;

public class HttpApiException extends RuntimeException {

    public final Integer statusCode;

    public HttpApiException(String message, Integer statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
