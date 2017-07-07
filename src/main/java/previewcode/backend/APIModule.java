package previewcode.backend;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.servlet.ServletModule;
import io.atlassian.fugue.Unit;
import io.vavr.jackson.datatype.VavrModule;
import org.jboss.resteasy.plugins.guice.ext.JaxrsModule;
import previewcode.backend.api.exceptionmapper.*;
import previewcode.backend.api.v2.ApprovalsAPI;
import previewcode.backend.api.v2.OrderingAPI;
import previewcode.backend.api.v2.TestAPI;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;
import java.io.IOException;


public class APIModule extends ServletModule {

    @SuppressWarnings("PointlessBinding")
    @Override
    public void configureServlets() {
        this.install(new JaxrsModule());

        // API Endpoints
        // v2
        this.bind(TestAPI.class);
        this.bind(OrderingAPI.class);
        this.bind(ApprovalsAPI.class);

        // Exception mappers
        this.bind(RootExceptionMapper.class);
        this.bind(IllegalArgumentExceptionMapper.class);
        this.bind(HttpApiExceptionMapper.class);
        this.bind(NoTokenExceptionMapper.class);
        this.bind(NotAuthorizedExceptionMapper.class);
        this.bind(JacksonObjectMapperProvider.class);
    }


    /**
     * Plumbing necessary to convert Unit values to an empty response body when sent back over the network
     */
    @Provider
    static class JacksonObjectMapperProvider implements ContextResolver<ObjectMapper> {

        final ObjectMapper defaultObjectMapper;

        public JacksonObjectMapperProvider() {
            defaultObjectMapper = createDefaultMapper();
        }

        @Override
        public ObjectMapper getContext(Class<?> type) {
            return defaultObjectMapper;
        }

        private static ObjectMapper createDefaultMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new UnitSerializerModule());
            mapper.registerModule(new VavrModule());
            return mapper;
        }

    }

    static class UnitSerializerModule extends SimpleModule {
        public UnitSerializerModule() {
            super();
            this.addSerializer(Unit.class, new JsonSerializer<Unit>() {
                @Override
                public void serialize(Unit value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                    gen.close();
                }
            });
        }
    }
}
