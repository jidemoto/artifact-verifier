package to.idemo.james.artifactverifier.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import to.idemo.james.artifactverifier.domain.NexusComponent;
import to.idemo.james.artifactverifier.util.RequestUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RestController
@RequestMapping("/admin")
public class AdminHandler {
    private static final Logger logger = LoggerFactory.getLogger(AdminHandler.class);

    private static final String REPO_NAME = "npm-proxy";

    private final String username;
    private final String password;
    private final String nexusUrl;
    private final RestTemplate restTemplate;

    public AdminHandler(RestTemplate restTemplate,
                             @Value("${NEXUS_USERNAME:jidemoto}") String username,
                             @Value("${NEXUS_PASSWORD:jidemoto}") String password,
                             @Value("${NEXUS_URL:http://nexus:8081}") String nexusUrl) {
        this.username = username;
        this.password = password;
        this.nexusUrl = nexusUrl;
        this.restTemplate = restTemplate;
    }

    @DeleteMapping("/clear")
    public void clearSensitiveAssets(@RequestParam(required = false, defaultValue = "npm-proxy") String repo) {
        for (String assetId : findAssetIds(repo)) {
            deleteAsset(assetId);
        }
    }

    private Collection<String> findAssetIds(String repository) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", RequestUtils.generateBasicAuthHeader(username, password));
        ResponseEntity<SearchResponse> response = restTemplate.exchange(
                nexusUrl + "/service/rest/v1/search/assets?repository=" + URLEncoder.encode(repository, StandardCharsets.UTF_8) + " &format=npm&name=william-rowan-hamilton",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                SearchResponse.class
        );
        return Objects.requireNonNull(response.getBody()).getItems()
                .stream()
                .map(metadata -> (String) metadata.get("id"))
                .collect(Collectors.toSet());
    }

    private void deleteAsset(String assetId) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", RequestUtils.generateBasicAuthHeader(username, password));
        ResponseEntity<Object> deleteResponse = restTemplate.exchange(
                nexusUrl + "/service/rest/v1/assets/" + assetId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Object.class
        );
        logger.info("Deleted Asset ID {}; status code {}", assetId, deleteResponse.getStatusCode());
    }


    private void deleteComponent(String componentId) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", RequestUtils.generateBasicAuthHeader(username, password));
        ResponseEntity<Object> response = restTemplate.exchange(
                nexusUrl + "/service/rest/v1/components/" + componentId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Object.class
        );
        if(!response.getStatusCode().is2xxSuccessful()) {
            logger.error("Failed to delete");
        }
    }

    private List<String> getComponents() {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Authorization", RequestUtils.generateBasicAuthHeader(username, password));
        String currentToken = "";
        List<String> componentIds = new ArrayList<>();
        while(currentToken != null) {
            ResponseEntity<ListRepoResponse> response = restTemplate.exchange(
                    nexusUrl + "/service/rest/v1/components?repository={repo}&continuationToken={continuationToken}",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    ListRepoResponse.class,
                    REPO_NAME,
                    currentToken
            );
            ListRepoResponse repoResponseBody = Objects.requireNonNull(response.getBody());
            repoResponseBody
                    .getItems()
                    .stream()
                    .map(component -> component.getComponent().getComponentId())
                    .forEach(componentIds::add);
            currentToken = repoResponseBody.getContinuationToken();
        }

        return componentIds;
    }

    public static class ListRepoResponse {
        private List<NexusComponent> items;
        private String continuationToken;

        public ListRepoResponse() {
        }

        public ListRepoResponse(List<NexusComponent> items, String continuationToken) {
            this.items = items;
            this.continuationToken = continuationToken;
        }

        public List<NexusComponent> getItems() {
            return items;
        }

        public String getContinuationToken() {
            return continuationToken;
        }
    }

    public static class SearchResponse {
        private List<Map<String, Object>> items;

        public List<Map<String, Object>> getItems() {
            return items;
        }
    }
}
