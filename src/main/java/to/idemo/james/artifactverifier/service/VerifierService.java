package to.idemo.james.artifactverifier.service;

import to.idemo.james.artifactverifier.exception.ArtifactValidationFailureException;

public interface VerifierService {

    void verifyArtifact(String sha256Hash) throws ArtifactValidationFailureException;
}
