package to.idemo.james.artifactverifier.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NexusComponent {
    private String id;
    private String componentId;
    private String format;
    private String name;
    private String group;
    private String version;
}
