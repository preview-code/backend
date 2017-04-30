package previewcode.backend.api.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import java.util.Date;

@Path("v2/test")
public class TestAPI {

    static class Response {
        @JsonProperty("version")
        public String apiVersion = "v2";
        @JsonProperty("time")
        public String serverTime = new Date().toString();

        @JsonCreator
        public Response(@JsonProperty("version") String version, @JsonProperty("time") String serverTime) {
            this.apiVersion = version;
            this.serverTime = serverTime;
        }

        public Response() { }
    }


    @GET
    @Produces("application/json")
    public Response get() {
        return new Response();
    }
}
