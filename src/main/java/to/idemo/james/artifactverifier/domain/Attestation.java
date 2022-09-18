package to.idemo.james.artifactverifier.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attestation {
    private String attestationType;
    private String certificate;
    private String signedEntryTimestamp;
    private long integratedTime;
    @JsonProperty("logID")
    private String logId;
    private long logIndex;
}
