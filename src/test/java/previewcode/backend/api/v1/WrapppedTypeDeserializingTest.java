package previewcode.backend.api.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import previewcode.backend.DTO.HunkChecksum;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class WrapppedTypeDeserializingTest {

    @Test
    void deserialize_hunkChecksum() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        HunkChecksum readValue = mapper.readValue("\"abcd\"", HunkChecksum.class);
        assertThat(readValue.checksum).isEqualTo("abcd");
    }

    @Test
    void serialize_hunkChecksum() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        HunkChecksum h = new HunkChecksum("abcd");

        assertThat(mapper.writeValueAsString(h)).isEqualTo("\"abcd\"");
    }
}
