package previewcode.backend.services;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.node.IntNode;
import previewcode.backend.DTO.HunkChecksum;

import java.io.IOException;

public class HunkChecksumDeserializer extends StdDeserializer<HunkChecksum> {

    public HunkChecksumDeserializer() {
        this(null);
    }

    public HunkChecksumDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public HunkChecksum deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        String checksum = node.asText();

        return new HunkChecksum(checksum);
    }
}