package to.idemo.james.artifactverifier.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NexusEventWrapper<T> {
    private String timestamp;
    private String nodeId;
    private String initiator;
    private String repositoryName;
    private String action; //Enumeration is probably more appropriate
    private T component;
}
