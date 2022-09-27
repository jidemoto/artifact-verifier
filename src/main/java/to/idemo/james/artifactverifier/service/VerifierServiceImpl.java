package to.idemo.james.artifactverifier.service;

import to.idemo.james.artifactverifier.exception.RekorEntryNotFoundException;

public class VerifierServiceImpl implements VerifierService {


    @Override
    public void verifyArtifact(byte[] sha256Hash) {
        throw new RekorEntryNotFoundException();
    }
}
