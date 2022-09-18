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
        for (List<?> subjectAlternativeName : subjectAlternativeNames) {
            Object type = subjectAlternativeName.get(0);
            assertEquals(1, type); //rfc822Name according to https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/security/cert/X509Certificate.html#getSubjectAlternativeNames()
            Object name = subjectAlternativeName.get(1);
            assertEquals("james.idemoto@gmail.com", name);
        }

        //Next up: getting the OIDC provider that authenticated the user.  A little harder because we have to pull from another extension
        Set<String> criticalExtensionOIDs = x509Certificate.getNonCriticalExtensionOIDs();
        assertTrue(criticalExtensionOIDs.contains("1.3.6.1.4.1.57264.1.1"));
        byte[] extensionValue = x509Certificate.getExtensionValue("1.3.6.1.4.1.57264.1.1");
        try (ASN1InputStream asn1InputStream = new ASN1InputStream(extensionValue)) {
            ASN1Primitive oidcPrimative = asn1InputStream.readObject();
            // Docs say this will be a DER-encoded octet stream: https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/security/cert/X509Extension.html#getExtensionValue(java.lang.String)
            if (oidcPrimative instanceof DEROctetString) {
                assertEquals("https://github.com/login/oauth", new String(((DEROctetString) oidcPrimative).getOctets()));
            } else {
                fail("Expected a DER Octet String from extension");
            }
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
        X509Certificate x509Certificate = VerificationUtilities.getX509Certificate(byteArrayInputStream);
        return x509Certificate;
    }
}