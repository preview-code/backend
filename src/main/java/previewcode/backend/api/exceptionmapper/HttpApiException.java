package previewcode.backend.api.exceptionmapper;

import okhttp3.HttpUrl;

public class HttpApiException extends RuntimeException {

    public final Integer statusCode;
    public final HttpUrl url;


    public HttpApiException(String message, Integer statusCode, HttpUrl url) {
        super(message);
        this.statusCode = statusCode;
        this.url = url;
    }
}
