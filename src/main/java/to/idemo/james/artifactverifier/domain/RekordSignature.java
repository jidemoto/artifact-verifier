package to.idemo.james.artifactverifier.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RekordSignature {
    private String content;
    private RekordPublicKey publicKey;
}
