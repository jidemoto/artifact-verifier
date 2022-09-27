package to.idemo.james.artifactverifier.service;

public interface VerifierService {
    void verifyArtifact(byte[] sha256Hash);
}
