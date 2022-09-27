package to.idemo.james.artifactverifier;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.x509.GeneralName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import to.idemo.james.artifactverifier.domain.Rekord;
import to.idemo.james.artifactverifier.domain.RekordBody;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.Certificate;
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
    public void canExtractExtensionValues() throws Exception {
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

    private static X509Certificate getTestCertificate() throws IOException, CertificateException {
        ClassPathResource classPathResource = new ClassPathResource("test-sigstore-entry.json");
        Map<String, Rekord> body = MAPPER.readValue(classPathResource.getInputStream(), new TypeReference<>() {
        });

        Rekord record = body.entrySet().iterator().next().getValue();
        RekordBody rekordBody = record.getBody();
        String certificate = rekordBody.getSpec().getSignature().getPublicKey().getContent();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(certificate));
        X509Certificate x509Certificate = VerificationUtilities.getX509Certificate(byteArrayInputStream);
        return x509Certificate;
    }
}