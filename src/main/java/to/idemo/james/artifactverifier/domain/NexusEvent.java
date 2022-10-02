package to.idemo.james.artifactverifier.domain;


public interface NexusEvent {

    String getTimestamp();

    String getNodeId();

    String getInitiator();

    String getRepositoryName();

    String getAction();
}
