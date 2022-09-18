package to.idemo.james.artifactverifier.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RekordPublicKey {
    /**
     * Contains the certificate chain used to create the signature (Base64 encoded)
     */
    private String content;
}
