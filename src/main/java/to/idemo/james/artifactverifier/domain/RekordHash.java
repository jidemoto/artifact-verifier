package to.idemo.james.artifactverifier.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RekordHash {
    private String algorithm;
    private String value;
}
