package previewcode.backend.database;

public class DatabaseException extends RuntimeException {
    public DatabaseException(String reason) {
        super(reason);
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
    }

    public DatabaseException(Throwable cause) {
        super(cause);
    }
}
