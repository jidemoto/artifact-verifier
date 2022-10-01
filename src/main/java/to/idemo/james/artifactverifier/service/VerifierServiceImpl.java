package to.idemo.james.artifactverifier.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import to.idemo.james.artifactverifier.VerificationUtilities;
import to.idemo.james.artifactverifier.domain.Rekord;
import to.idemo.james.artifactverifier.domain.RekordSignature;
import to.idemo.james.artifactverifier.exception.ArtifactValidationFailureException;
import to.idemo.james.artifactverifier.exception.EmailRuleFailureException;
import to.idemo.james.artifactverifier.exception.ProviderRuleFailureException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
public class VerifierServiceImpl implements VerifierService {
    private static final Logger logger = LoggerFactory.getLogger(VerifierServiceImpl.class);

    private final RekorService rekorService;

    private final String requiredEmailDomain;
    private final String requiredOidcProvider;

    // These will be lazily initialized when needed -- we'll want to break these out into another service so that they
    // can be retrieved separately
    private volatile Certificate rootCertificate;
    private volatile Certificate intermediateCertificate;

    public VerifierServiceImpl(RekorService rekorService,
                               @Value("${validation.emailDomain}") String requiredEmailDomain,
                               @Value("${validation.provider}") String requiredOidcProvider) {
        this.rekorService = rekorService;
        this.requiredEmailDomain = requiredEmailDomain;
        this.requiredOidcProvider = requiredOidcProvider;
    }

    @Override
    public void verifyArtifact(String sha256Hash) throws ArtifactValidationFailureException {
        Collection<String> uuids = rekorService.getUuids(sha256Hash);
        if (uuids.size() == 0) {
            throw new ArtifactValidationFailureException("No rekor entries found for hash", Collections.emptyMap());
        }
        Map<String, Exception> failures = new HashMap<>(uuids.size());
        boolean validated = false;
        for (String uuid : uuids) {
            Rekord rekord = rekorService.getRekord(uuid);
            try {
                verifyArtifactAgainstEntry(sha256Hash, rekord);
                validated = true;
            } catch (Exception e) {
                failures.put(uuid, e);
            }

            if (validated) {
                return;
            }
        }

        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, Exception> entry : failures.entrySet()) {
                logger.debug("Validation failures for sha {}", sha256Hash, entry.getValue());
            }
        }
        throw new ArtifactValidationFailureException("Failed to validate artifact with hash " + sha256Hash, failures);
    }

    private void verifyArtifactAgainstEntry(String sha256Hash, Rekord rekord)
            throws CertificateException, EmailRuleFailureException, ProviderRuleFailureException,
            NoSuchAlgorithmException, SignatureException, InvalidKeyException, NoSuchProviderException {
        RekordSignature rekordSignature = rekord.getBody().getSpec().getSignature();
        String signature = rekordSignature.getContent();
        String certificateString = rekordSignature.getPublicKey().getContent();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(certificateString));
        X509Certificate x509Certificate = VerificationUtilities.getX509Certificate(byteArrayInputStream);

        //verify the certificate is valid for fulcio root / intermediate
        getIntermediateCertificate().verify(getRootCertificate().getPublicKey());
        x509Certificate.verify(getIntermediateCertificate().getPublicKey());

        String san = VerificationUtilities.extractSan(x509Certificate);
        if (!san.endsWith("@" + requiredEmailDomain)) {
            throw new EmailRuleFailureException();
        }

        String provider = VerificationUtilities.extractOidcProvider(x509Certificate);
        if (!provider.equals(requiredOidcProvider)) {
            throw new ProviderRuleFailureException();
        }

        Base64.Decoder decoder = Base64.getDecoder();
        HexFormat hexFormat = HexFormat.of();
        VerificationUtilities.verifyArtifact(x509Certificate, decoder.decode(signature), hexFormat.parseHex(sha256Hash));
    }

    private Certificate getRootCertificate() {
        if (rootCertificate == null) {
            synchronized (this) {
                if (rootCertificate == null) {
                    rootCertificate = loadCert("root.cert");
                }
            }
        }
        return rootCertificate;
    }

    private Certificate getIntermediateCertificate() {
        if (intermediateCertificate == null) {
            synchronized (this) {
                if (intermediateCertificate == null) {
                    intermediateCertificate = loadCert("intermediate.cert");
                }
            }
        }
        return intermediateCertificate;
    }

    private Certificate loadCert(String classpathLocation) {
        try (InputStream certStream = new ClassPathResource(classpathLocation).getInputStream()) {
            return VerificationUtilities.getX509Certificate(certStream);
        } catch (IOException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}
