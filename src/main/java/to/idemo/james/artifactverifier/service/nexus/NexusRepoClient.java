package to.idemo.james.artifactverifier.service.nexus;

/**
 * Interface for Nexus repo clients (specific format handlers e.g. npm)
 *
 * Exceptions will be thrown internally if more than one client exists per format
 */
public interface NexusRepoClient extends RepositoryClient {
    String handlesFormat();
}
