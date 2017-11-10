package previewcode.backend.api.exceptionmapper;

public class NotAuthorizedException extends RuntimeException {
    public NotAuthorizedException(String s) {
        super(s);
    }
}
