package to.idemo.james.artifactverifier.service;

public interface NotifierService {
    void warn(String artifactName, String artifactLocation, String attributedUser, String reason);
    void alert(String artifactName, String artifactType, String artifactLocation, String attributedUser, String reason);
}
