package previewcode.backend.services.http;

import okhttp3.Request;

import java.io.IOException;

/**
 * Executes HTTP Requests.
 * Used in tests to stub API calls.
 */
@FunctionalInterface
public interface IHttpRequestExecutor {
    String execute(Request request) throws RuntimeException;
}
