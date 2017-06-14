package previewcode.backend.services.actions;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.control.Option;

import static previewcode.backend.services.actiondsl.ActionDSL.*;

/**
 * Actions for interacting with the current request context.
 */
public class RequestContextActions {

    /**
     * Get the user-agent string of the current request.
     */
    public static Action<String> getUserAgent = new GetUserAgent();

    /**
     * Get the body of the current request.
     */
    public static Action<String> getRequestBody = new GetRequestBody();

    /**
     * Get the body of the current request, respresented as JsonNode.
     */
    public static Action<JsonNode> getJsonBody = new GetJsonRequestBody();

    /**
     * Get a specific request header for the current request,
     * Interpreters assume the header exists and throws a RuntimeException if it does not.
     */
    public static Action<String> getHeader(String header) {
        return new GetHeader(header);
    }

    /**
     * Get a specific query parameter of the current request,
     * returns {@link io.vavr.control.Option.None} if the query parameter was not present.
     */
    public static Action<Option<String>> getQueryParam(String param) {
        return new GetQueryParam(param);
    }




    public static class GetUserAgent extends Action<String> {}

    public static class GetRequestBody extends Action<String> {}

    public static class GetHeader extends Action<String> {
        public final String header;

        public GetHeader(String header) {
            this.header = header;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GetHeader getHeader = (GetHeader) o;

            return header.equals(getHeader.header);
        }

        @Override
        public int hashCode() {
            return header.hashCode();
        }
    }

    public static class GetQueryParam extends Action<Option<String>> {
        public final String param;

        public GetQueryParam(String param) {
            this.param = param;
        }
    }

    public static class GetJsonRequestBody extends Action<JsonNode> { }
}
