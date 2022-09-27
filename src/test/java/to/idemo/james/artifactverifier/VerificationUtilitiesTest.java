package to.idemo.james.artifactverifier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import to.idemo.james.artifactverifier.domain.Rekord;
import to.idemo.james.artifactverifier.domain.RekordBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class VerificationUtilitiesTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void testCanRetrieveAndVerifyCertificate() throws Exception {
        X509Certificate root = VerificationUtilities.getCertificateFromClasspath("root.cert");
        X509Certificate intermediate = VerificationUtilities.getCertificateFromClasspath("intermediate.cert");
        intermediate.verify(root.getPublicKey()); //Throws an exception if incorrect
    }

    @Test
    public void testVerifySigningCertAgainstRoot() throws Exception {
        X509Certificate intermediate = VerificationUtilities.getCertificateFromClasspath("intermediate.cert");
        X509Certificate testCertificate = getTestCertificate();
        testCertificate.verify(intermediate.getPublicKey());
    }

    @Test
    public void testCanExtractExtensionValues() throws Exception {
        X509Certificate x509Certificate = getTestCertificate();

        //First up: the easy one.  Getting a SAN should be easy
        Collection<List<?>> subjectAlternativeNames = x509Certificate.getSubjectAlternativeNames();
        assertNotNull(subjectAlternativeNames);
        assertEquals("research@idemo.to", VerificationUtilities.extractSan(x509Certificate));

        //Next up: getting the OIDC provider that authenticated the user.  A little harder because we have to pull from another extension
        Set<String> criticalExtensionOIDs = x509Certificate.getNonCriticalExtensionOIDs();
        assertTrue(criticalExtensionOIDs.contains("1.3.6.1.4.1.57264.1.1"));
        assertEquals("https://accounts.google.com", VerificationUtilities.extractOidcProvider(x509Certificate));
    }

    @Test
    public void testVerifySignature() throws Exception {
        X509Certificate x509Certificate = getTestCertificate();

        ClassPathResource classPathResource = new ClassPathResource("test-sigstore-entry.json");
        Map<String, Rekord> body = MAPPER.readValue(classPathResource.getInputStream(), new TypeReference<>() {
        });

        Rekord record = body.entrySet().iterator().next().getValue();
        String sigString = record.getBody().getSpec().getSignature().getContent();
        String artifactHash = record.getBody().getSpec().getData().getHash().getValue();

        assertEquals("c7e37479bddbe14827a95e2313fba86c493b3a69f34e40850fa0be49ee7f4164", artifactHash);
        assertEquals("MEQCIC1KffJ0FZ2D6de4tFiEbV2FQGytuipq87woAzFRcfm3AiAl6dFBYzpWNas5DuCeRs64csh/+sxPtPg3GUCGRKmlsw==", sigString);
        Base64.Decoder decoder = Base64.getDecoder();
        HexFormat hexFormat = HexFormat.of();
        VerificationUtilities.verifyArtifact(x509Certificate, decoder.decode(sigString), hexFormat.parseHex(artifactHash));

        //We'll pass a bad hash to verify that we fail when the artifacts don't match
        try {
            VerificationUtilities.verifyArtifact(x509Certificate, decoder.decode(sigString),
                    hexFormat.parseHex("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855"));
            fail("Should have failed check with a made up signature");
        } catch (RuntimeException e) {
            //pass
        }
    }

    private static X509Certificate getTestCertificate() throws IOException, CertificateException {
        ClassPathResource classPathResource = new ClassPathResource("test-sigstore-entry.json");
        Map<String, Rekord> body = MAPPER.readValue(classPathResource.getInputStream(), new TypeReference<>() {
        });

        Rekord record = body.entrySet().iterator().next().getValue();
        RekordBody rekordBody = record.getBody();
        String certificate = rekordBody.getSpec().getSignature().getPublicKey().getContent();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(certificate));
        return VerificationUtilities.getX509Certificate(byteArrayInputStream);
    }
}