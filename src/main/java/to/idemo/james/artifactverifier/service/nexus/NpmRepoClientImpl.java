package to.idemo.james.artifactverifier.service.nexus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;
import to.idemo.james.artifactverifier.domain.NexusAsset;
import to.idemo.james.artifactverifier.domain.NexusAssetMetadata;
import to.idemo.james.artifactverifier.domain.NexusComponent;
import to.idemo.james.artifactverifier.util.RequestUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Component
public class NpmRepoClientImpl implements NexusRepoClient {
    private static final Logger logger = LoggerFactory.getLogger(NpmRepoClientImpl.class);
    private static final String FORMAT = "npm";
    private final String username;
    private final String password;
    private final String nexusUrl;
    private final RestTemplate restTemplate;

    public NpmRepoClientImpl(RestTemplate restTemplate,
                             @Value("${NEXUS_USERNAME:jidemoto}") String username,
                             @Value("${NEXUS_PASSWORD:jidemoto}") String password,
                             @Value("${NEXUS_URL:http://nexus:8081}") String nexusUrl) {
        this.username = username;
        this.password = password;
        this.nexusUrl = nexusUrl;
        this.restTemplate = restTemplate;
    }

    @Override
    public String handlesFormat() {
        return FORMAT;
    }

    @Override
    public Optional<String> getSha256Hash(NexusComponent component) {
        return Optional.empty();
    }

    @Override
    public Optional<String> getSha256Hash(NexusAsset asset) {
        String assetId = asset.getAsset().getAssetId();
        return getAssetSha256Hash(assetId);
    }

    public Optional<String> getAssetSha256Hash(String assetId) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", RequestUtils.generateBasicAuthHeader(username, password));
        ResponseEntity<NexusAssetMetadata> response = restTemplate.exchange(
                nexusUrl + "/service/rest/v1/assets/" + assetId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                NexusAssetMetadata.class);
        if(!response.getStatusCode().is2xxSuccessful()) {
            logger.error("Asset with ID {} couldn't be retrieved.  Reason: {}",
                    assetId, response.getStatusCode().getReasonPhrase());
        }

        NexusAssetMetadata assetMetadata = response.getBody();
        if(assetMetadata == null) {
            throw new NullPointerException("Retrieved asset metadata for " + assetId + " was null");
        }
        if(assetMetadata.getContentType().equals("application/json")) {
            //We're going to ignore the json file assets because they appear to be component metadata
            return Optional.empty();
        }
        String downloadUrl = assetMetadata.getDownloadUrl();

        try {
            Path fileLocation = Files.createTempFile("Verifier-npm-repo-", ".tmp");
            try {
                String sha256Hash = restTemplate.execute(downloadUrl, HttpMethod.GET, req -> {
                            req.getHeaders().add("Authorization", RequestUtils.generateBasicAuthHeader(username, password));
                        },
                        res -> {
                            MessageDigest digest;
                            try {
                                digest = MessageDigest.getInstance("SHA-256");
                            } catch (NoSuchAlgorithmException e) {
                                throw new RuntimeException(e);
                            }
                            try (DigestInputStream inputStream = new DigestInputStream(res.getBody(), digest);
                                 OutputStream outputStream = Files.newOutputStream(fileLocation)) {
                                StreamUtils.copy(inputStream, outputStream);
                                return HexFormat.of().formatHex(digest.digest());
                            }
                        });

                return Optional.ofNullable(sha256Hash);
            } finally {
                Files.deleteIfExists(fileLocation);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
