package previewcode.backend.services.interpreters;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Option;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.spi.HttpRequest;
import previewcode.backend.services.actiondsl.Interpreter;

import javax.ws.rs.NotAuthorizedException;
import java.io.IOException;
import java.io.InputStream;

import static previewcode.backend.services.actions.RequestContextActions.*;

public class RequestContextActionInterpreter extends Interpreter {

    private final HttpRequest request;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String requestBody;
    private JsonNode jsonBody;


    public RequestContextActionInterpreter(HttpRequest request) {
        super();
        this.request = request;

        on(GetHeader.class).apply(this::getHeader);
        on(GetUserAgent.class).apply(__ -> request.getHttpHeaders().getHeaderString("User-Agent"));
        on(GetQueryParam.class).apply(this::getQueryParam);
        on(GetRequestBody.class).apply(__ -> getRequestBody());
        on(GetJsonRequestBody.class).apply(__ -> getJsonBody());
    }

    private String getHeader(GetHeader action) {
        String header = request.getHttpHeaders().getRequestHeaders().getFirst(action.header);
        if (header == null) {
            throw new NotAuthorizedException("Expected header: " + action.header + " to be present");
        } else {
            return header;
        }
    }

    private JsonNode getJsonBody() throws IOException {
        if (jsonBody != null) return jsonBody;
        this.jsonBody = MAPPER.readTree(getRequestBody());
        return this.jsonBody;
    }

    private Option<String> getQueryParam(GetQueryParam action) {
        return Option.of(request.getUri().getQueryParameters().getFirst(action.param));
    }

    private String getRequestBody() throws IOException {
        if (requestBody != null) return requestBody;

        try (InputStream inputStream = request.getInputStream()) {
            requestBody = IOUtils.toString(inputStream, "UTF-8");
            request.setInputStream(IOUtils.toInputStream(requestBody, "UTF-8"));
            return requestBody;
        }
    }
}
