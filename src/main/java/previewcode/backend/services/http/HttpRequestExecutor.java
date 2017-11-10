package previewcode.backend.services.http;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import previewcode.backend.api.exceptionmapper.HttpApiException;
import previewcode.backend.services.GithubService;

import java.io.IOException;
import java.nio.file.Files;

public class HttpRequestExecutor implements IHttpRequestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(GithubService.class);

    private final Cache cache = new Cache(
            Files.createTempDirectory("preview-code-gh-cache").toFile(),
            10 * 1024 * 1024);

    private final OkHttpClient client =
            new OkHttpClient.Builder().cache(cache).build();

    public HttpRequestExecutor() throws IOException { }

    public String execute(Request request) {
        logger.debug("[OKHTTP3] Executing request: " + request);
        logger.debug("With Authorization hashcode: " + request.header("Authorization").hashCode());
        try (Response response = client.newCall(request).execute()) {
            String body = response.body().string();
            if (response.isSuccessful()) {
                return body;
            } else {
                throw new HttpApiException(body, response.code(), request.url());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
