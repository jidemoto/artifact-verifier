package to.idemo.james.artifactverifier.notification;

public interface Notifier {
    void warn(String artifactName, String artifactLocation, String attributedUser, String reason);
    void alert(String artifactName, String artifactType, String artifactLocation, String attributedUser, String reason);
}
