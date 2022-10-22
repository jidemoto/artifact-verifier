package to.idemo.james.artifactverifier.service;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import to.idemo.james.artifactverifier.configuration.VerifierProperties;
import to.idemo.james.artifactverifier.exception.ArtifactValidationFailureException;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class VerifierServiceImplTest {
    private static final Logger logger = LoggerFactory.getLogger(VerifierServiceImplTest.class);

    @Test
    void verifyArtifact() {
        VerifierServiceImpl verifierService = new VerifierServiceImpl(new RekorServiceImpl(new RestTemplate(), "https://rekor.sigstore.dev"),
                new VerifierProperties(Collections.singleton("gatech.edu"), Collections.singleton("https://accounts.google.com"), Collections.emptySet()));

        try {
            verifierService.verifyArtifact("c7e37479bddbe14827a95e2313fba86c493b3a69f34e40850fa0be49ee7f4164");
            fail("Should have failed due to invalid email domain");
        } catch (ArtifactValidationFailureException e) {
            logger.info("Failures on validation: {}", e.getExceptionMap());
        }

    }
}