package to.idemo.james.artifactverifier;

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
