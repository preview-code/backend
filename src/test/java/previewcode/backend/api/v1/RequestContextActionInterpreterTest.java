package previewcode.backend.api.v1;

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

    @Test
    void getRequestBody_readsContent_withInjectedRequest() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("irrelevant").content("Hello Content".getBytes());
        RequestContextActionInterpreter interpreter = new RequestContextActionInterpreter(request);
        String body = interpreter.unsafeEvaluate(getRequestBody);
        assertThat(body).isEqualTo("Hello Content");
    }

    @Test
    void getRequestBody_readsContent_withoutConsumingTheStream() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("irrelevant").content("Hello Content".getBytes());
        RequestContextActionInterpreter interpreter = new RequestContextActionInterpreter(request);
        String body = interpreter.unsafeEvaluate(getRequestBody.then(getRequestBody));
        assertThat(body).isEqualTo("Hello Content");
    }

    @Test
    void getHeader_readsHeader_fromInjectedRequest() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("irrelevant").header("abc", "def");
        RequestContextActionInterpreter interpreter = new RequestContextActionInterpreter(request);
        String header = interpreter.unsafeEvaluate(getHeader("abc"));
        assertThat(header).isEqualTo("def");
    }

    @Test
    void getHeader_returnsFirstValueOnly() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("irrelevant")
                .header("abc", "def")
                .header("abc", "ghi");
        RequestContextActionInterpreter interpreter = new RequestContextActionInterpreter(request);
        String header = interpreter.unsafeEvaluate(getHeader("abc"));
        assertThat(header).isEqualTo("def");
    }

    @Test
    void getHeader_throws_whenHeaderIsNotPresent() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("irrelevant").header("abc", "def");
        RequestContextActionInterpreter interpreter = new RequestContextActionInterpreter(request);
        assertThatExceptionOfType(NotAuthorizedException.class)
                .isThrownBy(() -> interpreter.unsafeEvaluate(getHeader("xyz")));
    }

    @Test
    void getUserAgent_returnsUserAgent() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("irrelevant").header("User-Agent", "qwerty");
        RequestContextActionInterpreter interpreter = new RequestContextActionInterpreter(request);
        String userAgent = interpreter.unsafeEvaluate(getUserAgent);
        assertThat(userAgent).isEqualTo("qwerty");
    }

    @Test
    void getQueryParam_returnsTheParam() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("irrelevant.com/?name=txsmith");
        RequestContextActionInterpreter interpreter = new RequestContextActionInterpreter(request);
        Option<String> name = interpreter.unsafeEvaluate(getQueryParam("name"));
        assertThat(name).isEqualTo(Option.of("txsmith"));
    }

    @Test
    void getQueryParam_decodesTheParam() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("irrelevant.com/?name=tx%20smith");
        RequestContextActionInterpreter interpreter = new RequestContextActionInterpreter(request);
        Option<String> name = interpreter.unsafeEvaluate(getQueryParam("name"));
        assertThat(name).isEqualTo(Option.of("tx smith"));
    }

    @Test
    void getQueryParam_returnsFirstParam() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("irrelevant.com/?name=txsmith&name=eanker");
        RequestContextActionInterpreter interpreter = new RequestContextActionInterpreter(request);
        Option<String> name = interpreter.unsafeEvaluate(getQueryParam("name"));
        assertThat(name).isEqualTo(Option.of("txsmith"));
    }

    @Test
    void getQueryParam_returnsNoneWhenNotPresent() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("irrelevant.com/?name=txsmith");
        RequestContextActionInterpreter interpreter = new RequestContextActionInterpreter(request);
        Option<String> salary = interpreter.unsafeEvaluate(getQueryParam("salary"));
        assertThat(salary).isEqualTo(Option.none());
    }

    @Test
    void getJsonBody_parsesBodyToJson() throws URISyntaxException {
        MockHttpRequest request = MockHttpRequest.post("irrelevant").content("{ \"name\": \"txsmith\" }".getBytes());
        RequestContextActionInterpreter interpreter = new RequestContextActionInterpreter(request);
        JsonNode jsonNode = interpreter.unsafeEvaluate(getJsonBody);
        assertThat(jsonNode.get("name").asText()).isEqualTo("txsmith");
    }
}