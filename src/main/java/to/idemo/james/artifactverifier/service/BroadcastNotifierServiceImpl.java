package to.idemo.james.artifactverifier.service;

import org.springframework.stereotype.Service;
import to.idemo.james.artifactverifier.notification.Notifier;

import java.util.Collection;

/**
 * An implementation of the NotifierService that sends out events over all Notifier instances that are active on the
 * Spring graph.
 */
@Service
public class BroadcastNotifierServiceImpl implements NotifierService {
    private final Collection<Notifier> notifiers;

    public BroadcastNotifierServiceImpl(Collection<Notifier> notifiers) {
        this.notifiers = notifiers;
    }

    @Override
    public void warn(String artifactName, String artifactLocation, String attributedUser, String reason) {
        for (Notifier notifier : notifiers) {
            notifier.warn(artifactName, artifactLocation, attributedUser, reason);
        }
    }

    @Override
    public void alert(String artifactName, String artifactType, String artifactLocation, String attributedUser, String reason) {
        for (Notifier notifier : notifiers) {
            notifier.alert(artifactName, artifactType, artifactLocation, attributedUser, reason);
        }
    }
}
