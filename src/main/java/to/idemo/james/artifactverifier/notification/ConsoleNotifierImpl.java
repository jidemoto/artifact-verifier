package to.idemo.james.artifactverifier.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ConsoleNotifierImpl implements Notifier {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleNotifierImpl.class);
    @Override
    public void warn(String artifactName, String artifactLocation, String attributedUser, String reason) {
        logger.warn("{} uploaded by {} at {} failed validation because {}", artifactName, attributedUser, artifactLocation, reason);
    }

    @Override
    public void alert(String artifactName, String artifactType, String artifactLocation, String attributedUser, String reason) {
        logger.error("{} uploaded by {} at {} failed validation because {}", artifactName, attributedUser, artifactLocation, reason);
    }
}
