package to.idemo.james.artifactverifier.service.nexus;

import to.idemo.james.artifactverifier.domain.NexusAsset;
import to.idemo.james.artifactverifier.domain.NexusComponent;

import java.util.Optional;

public interface RepositoryClient {
    Optional<String> getSha256Hash(NexusComponent component);
    Optional<String> getSha256Hash(NexusAsset asset);
}
