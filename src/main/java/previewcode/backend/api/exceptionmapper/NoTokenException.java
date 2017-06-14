package previewcode.backend.api.exceptionmapper;

public class NoTokenException extends RuntimeException {

    public NoTokenException() {
        super("API call requires an authentication token");
    }
}
