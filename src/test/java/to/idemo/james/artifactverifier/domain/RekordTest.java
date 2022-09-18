package to.idemo.james.artifactverifier.domain;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RekordTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void canDeserializeResponseBody() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("test-sigstore-entry.json");
        Map<String, Rekord> body = MAPPER.readValue(classPathResource.getInputStream(), new TypeReference<>() {
        });

        Rekord record = body.entrySet().iterator().next().getValue();
        RekordBody rekordBody = record.getBody();
        assertEquals("sha256", rekordBody.getSpec().getData().getHash().getAlgorithm());
    }

}