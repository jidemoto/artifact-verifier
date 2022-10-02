package to.idemo.james.artifactverifier.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NexusComponent implements NexusEvent {
    private String timestamp;
    private String nodeId;
    private String initiator;
    private String repositoryName;
    private String action;
    private Component component;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Component {
        private String id;
        private String componentId;
        private String format;
        private String name;
        private String group;
        private String version;
    }
}
