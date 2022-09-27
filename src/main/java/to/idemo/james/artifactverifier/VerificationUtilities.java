package to.idemo.james.artifactverifier;

import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.*;
import java.util.Base64;
import java.util.Collection;
import java.util.List;

public class VerificationUtilities {
    private static final Logger logger = LoggerFactory.getLogger(VerificationUtilities.class);

    public static X509Certificate getCertificateFromClasspath(String classpathLocation) {
        try (InputStream inputStream = new ClassPathResource(classpathLocation).getInputStream()) {
            return getX509Certificate(inputStream);
        } catch (IOException | CertificateException e) {
            throw new RuntimeException("Unable to retrieve certificate", e);
        }
    }

    public static X509Certificate getX509Certificate(InputStream inputStream) throws CertificateException {
        return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(inputStream);
    }

    public static String extractOidcProvider(X509Certificate x509Certificate) {
        byte[] extensionValue = x509Certificate.getExtensionValue("1.3.6.1.4.1.57264.1.1");
        try (ASN1InputStream asn1InputStream = new ASN1InputStream(extensionValue)) {
            ASN1Primitive oidcPrimative = asn1InputStream.readObject();
            // Docs say this will be a DER-encoded octet stream: https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/security/cert/X509Extension.html#getExtensionValue(java.lang.String)
            if (oidcPrimative instanceof DEROctetString) {
                return new String(((DEROctetString) oidcPrimative).getOctets());
            } else {
                throw new IllegalStateException("Spec says that a DER-encoded octet string should be present, but found something else");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String extractSan(X509Certificate x509Certificate) {
        Collection<List<?>> subjectAlternativeNames;
        try {
            subjectAlternativeNames = x509Certificate.getSubjectAlternativeNames();
        } catch (CertificateParsingException e) {
            throw new RuntimeException(e);
        }
        List<?> firstSan = subjectAlternativeNames.iterator().next();
        // Skipping entry 0 because it contains the type.  These should always be strings here for rekor
        Object name = firstSan.get(1);
        return (String) name;
    }

    public static void verifySigningCertificate(String base64EncodedCertificate) {
        CertPath signingCertPath;
        ClassPathResource classPathResource = new ClassPathResource("fulcio.bundle");
        logger.info("Bundle file is readable: {}", classPathResource.isReadable());
        try (InputStream stream = classPathResource.getInputStream();
             BufferedInputStream bufferedInputStream = new BufferedInputStream(stream)) {
            signingCertPath = CertificateFactory.getInstance("X.509")
                    .generateCertPath(bufferedInputStream, "PKCS7");
        } catch (IOException | CertificateException e) {
            throw new RuntimeException(e);
        }
        byte[] certBytes = Base64.getDecoder().decode(base64EncodedCertificate);
        try (ByteArrayInputStream certificateStream = new ByteArrayInputStream(certBytes)) {
            Certificate certificate = CertificateFactory.getInstance("X.509").generateCertificate(certificateStream);
            certificate.verify(signingCertPath.getCertificates().iterator().next().getPublicKey());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (SignatureException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        }


    }
}
