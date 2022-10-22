package to.idemo.james.artifactverifier.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NexusAsset implements NexusEvent {
    private String timestamp;
    private String nodeId;
    private String initiator;
    private String repositoryName;
    private String action;
    private Asset asset;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Asset {
        private String id;
        private String assetId;
        private String format;
        private String name;
    }
}

