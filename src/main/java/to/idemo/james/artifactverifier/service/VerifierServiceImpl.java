package to.idemo.james.artifactverifier.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import to.idemo.james.artifactverifier.VerificationUtilities;
import to.idemo.james.artifactverifier.domain.Rekord;
import to.idemo.james.artifactverifier.domain.RekordSignature;
import to.idemo.james.artifactverifier.exception.ArtifactValidationFailureException;
import to.idemo.james.artifactverifier.exception.EmailRuleFailureException;
import to.idemo.james.artifactverifier.exception.ProviderRuleFailureException;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;

@Service
public class VerifierServiceImpl implements VerifierService {

    private final RekorService rekorService;

    private final String requiredEmailDomain;
    private final String requiredOidcProvider;

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

            if(validated) {
                return;
            }
        }

        throw new ArtifactValidationFailureException("Failed to validate artifact with hash " + sha256Hash, failures);
    }

    private void verifyArtifactAgainstEntry(String sha256Hash,  Rekord rekord) throws CertificateException, EmailRuleFailureException, ProviderRuleFailureException {
        RekordSignature rekordSignature = rekord.getBody().getSpec().getSignature();
        String signature = rekordSignature.getContent();
        String certificateString = rekordSignature.getPublicKey().getContent();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(certificateString));
        X509Certificate x509Certificate = VerificationUtilities.getX509Certificate(byteArrayInputStream);

        //verify the certificate is valid for fulcio root / intermediate

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
}
