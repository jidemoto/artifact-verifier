package to.idemo.james.artifactverifier.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import to.idemo.james.artifactverifier.util.Base64Deserializer;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rekord {
    @JsonDeserialize(using = Base64Deserializer.class)
    private RekordBody body;
    private long integratedTime;
    @JsonProperty("logID")
    private String logId;
    private long logIndex;
    private String signedEntryTimestamp;
    private Verification verification;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Verification {
        private InclusionProof inclusionProof;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class InclusionProof {
        private List<String> hashes;
        private long logIndex;
        private String rootHash;
        private long treeSize;
    }
}
