package to.idemo.james.artifactverifier.service.nexus;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import to.idemo.james.artifactverifier.domain.NexusAsset;
import to.idemo.james.artifactverifier.domain.NexusComponent;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class Nexus3RepositoryClientImpl implements NexusClient {
    private static final Logger logger = LoggerFactory.getLogger(Nexus3RepositoryClientImpl.class);

    private final Map<String, NexusRepoClient> repoTypeMapping;

    public Nexus3RepositoryClientImpl(Collection<NexusRepoClient> repoClients) {
        repoTypeMapping = repoClients.stream()
                .map(repo -> Pair.of(repo.handlesFormat(), repo))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
    }

    @Override
    public Optional<String> getSha256Hash(NexusComponent component) {
        String format = component.getComponent().getFormat();
        NexusRepoClient nexusRepoClient = repoTypeMapping.get(format);
        if (nexusRepoClient == null) {
            logger.info("Skipping processing component {} because there's no handler for the format {}: {}",
                    component.getComponent().getComponentId(), format, component);
            return Optional.empty();
        }
        return nexusRepoClient.getSha256Hash(component);
    }

    @Override
    public Optional<String> getSha256Hash(NexusAsset asset) {
        String format = asset.getAsset().getFormat();
        NexusRepoClient nexusRepoClient = repoTypeMapping.get(format);
        if (nexusRepoClient == null) {
            logger.info("Skipping processing asset {} because there's no handler for the format {}: {}",
                    asset.getAsset().getAssetId(), format, asset);
            return Optional.empty();
        }
        return nexusRepoClient.getSha256Hash(asset);
    }
}
