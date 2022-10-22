package to.idemo.james.artifactverifier.service.nexus;

/**
 * Marker interface to target a higher-level class for dependency injection.  Implementations of this class will
 * delegate retrieval of assets and components to the specific repo type and client implementation
 */
public interface NexusClient extends RepositoryClient {

}
