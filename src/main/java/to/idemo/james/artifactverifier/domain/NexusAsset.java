package to.idemo.james.artifactverifier.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NexusAsset {
    private String id;
    private String assetId;
    private String format;
    private String name;
}
