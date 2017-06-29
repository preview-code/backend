package previewcode.backend.api.v2;

import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.control.Option;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.Test;
import previewcode.backend.services.interpreters.RequestContextActionInterpreter;

import javax.ws.rs.NotAuthorizedException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.*;
import static previewcode.backend.services.actions.RequestContextActions.*;

public class RequestContextActionInterpreterTest {

    private final RequestContextActionInterpreter interpreter = new RequestContextActionInterpreter();

    @Test
    void getRequestBody_readsContent_withInjectedRequest() throws URISyntaxException {
        interpreter.request = MockHttpRequest.post("irrelevant").content("Hello Content".getBytes());
        String body = interpreter.unsafeEvaluate(getRequestBody);
        assertThat(body).isEqualTo("Hello Content");
    }

    @Test
    void getRequestBody_readsContent_withoutConsumingTheStream() throws URISyntaxException {
        interpreter.request = MockHttpRequest.post("irrelevant").content("Hello Content".getBytes());
        String body = interpreter.unsafeEvaluate(getRequestBody.then(getRequestBody));
        assertThat(body).isEqualTo("Hello Content");
    }

    @Test
    void getHeader_readsHeader_fromInjectedRequest() throws URISyntaxException {
        interpreter.request = MockHttpRequest.post("irrelevant").header("abc", "def");
        String header = interpreter.unsafeEvaluate(getHeader("abc"));
        assertThat(header).isEqualTo("def");
    }

    @Test
    void getHeader_returnsFirstValueOnly() throws URISyntaxException {
        interpreter.request = MockHttpRequest.post("irrelevant")
                .header("abc", "def")
                .header("abc", "ghi");
        String header = interpreter.unsafeEvaluate(getHeader("abc"));
        assertThat(header).isEqualTo("def");
    }

    @Test
    void getHeader_throws_whenHeaderIsNotPresent() throws URISyntaxException {
        interpreter.request = MockHttpRequest.post("irrelevant").header("abc", "def");
        assertThatExceptionOfType(NotAuthorizedException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(getHeader("xyz")));
    }

    @Test
    void getUserAgent_returnsUserAgent() throws URISyntaxException {
        interpreter.request = MockHttpRequest.post("irrelevant").header("User-Agent", "qwerty");
        String userAgent = interpreter.unsafeEvaluate(getUserAgent);
        assertThat(userAgent).isEqualTo("qwerty");
    }

    @Test
    void getQueryParam_returnsTheParam() throws URISyntaxException {
        interpreter.request = MockHttpRequest.post("irrelevant.com/?name=txsmith");
        Option<String> name = interpreter.unsafeEvaluate(getQueryParam("name"));
        assertThat(name).isEqualTo(Option.of("txsmith"));
    }

    @Test
    void getQueryParam_decodesTheParam() throws URISyntaxException {
        interpreter.request = MockHttpRequest.post("irrelevant.com/?name=tx%20smith");
        Option<String> name = interpreter.unsafeEvaluate(getQueryParam("name"));
        assertThat(name).isEqualTo(Option.of("tx smith"));
    }

    @Test
    void getQueryParam_returnsFirstParam() throws URISyntaxException {
        interpreter.request = MockHttpRequest.post("irrelevant.com/?name=txsmith&name=eanker");
        Option<String> name = interpreter.unsafeEvaluate(getQueryParam("name"));
        assertThat(name).isEqualTo(Option.of("txsmith"));
    }

    @Test
    void getQueryParam_returnsNoneWhenNotPresent() throws URISyntaxException {
        interpreter.request = MockHttpRequest.post("irrelevant.com/?name=txsmith");
        Option<String> salary = interpreter.unsafeEvaluate(getQueryParam("salary"));
        assertThat(salary).isEqualTo(Option.none());
    }

    @Test
    void getJsonBody_parsesBodyToJson() throws URISyntaxException {
        interpreter.request = MockHttpRequest.post("irrelevant").content("{ \"name\": \"txsmith\" }".getBytes());
        JsonNode jsonNode = interpreter.unsafeEvaluate(getJsonBody);
        assertThat(jsonNode.get("name").asText()).isEqualTo("txsmith");
    }
}