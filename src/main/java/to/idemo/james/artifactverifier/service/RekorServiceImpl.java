package to.idemo.james.artifactverifier.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import to.idemo.james.artifactverifier.domain.Rekord;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Service
public class RekorServiceImpl implements RekorService {
    private static final Logger logger = LoggerFactory.getLogger(RekorServiceImpl.class);
    private static final MultiValueMap<String, String> HEADERS = new LinkedMultiValueMap<>(
            Collections.singletonMap("ContentType", Collections.singletonList("application/json"))
    );
    private final RestTemplate restTemplate;
    private final String rekorHostname;

    public RekorServiceImpl(RestTemplate restTemplate,
                            @Value("${rekor.hostname:https://rekor.sigstore.dev}") String rekorHostname) {
        this.restTemplate = restTemplate;
        this.rekorHostname = rekorHostname;
    }

    @Override
    public Collection<String> getUuids(String shaHash) {
        //Something to look at in the future: accept a pair of the hash algorithm -> hash string
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(Collections.singletonMap("hash", "sha256:" + shaHash), HEADERS);
        ResponseEntity<Collection<String>> response = restTemplate.exchange(rekorHostname + "/api/v1/index/retrieve",
                HttpMethod.POST,
                entity,
                new ParameterizedTypeReference<>() {
                }
        );
        return response.getBody();
    }

    @Override
    public Rekord getRekord(String rekorUuid) throws IllegalArgumentException {
        String encodedId = URLEncoder.encode(rekorUuid, StandardCharsets.UTF_8);
        String url = rekorHostname + "/api/v1/log/entries/" + encodedId;
        logger.debug("Making a call to {}", url);

        ResponseEntity<Map<String, Rekord>> response = restTemplate.exchange(url,
                HttpMethod.GET,
                new HttpEntity<>(HEADERS),
                new ParameterizedTypeReference<>() {
                });

        if(response.getStatusCodeValue() != 200) {
            throw new IllegalArgumentException("UUID " + encodedId + "not found");
        }
        if(response.getBody() == null) {
            throw new IllegalStateException("Not body found on response");
        }

        return response.getBody().entrySet().iterator().next().getValue();
    }
}
