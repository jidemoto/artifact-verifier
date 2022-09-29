package to.idemo.james.artifactverifier.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import to.idemo.james.artifactverifier.domain.NexusAsset;
import to.idemo.james.artifactverifier.domain.NexusComponent;
import to.idemo.james.artifactverifier.domain.NexusEventWrapper;
import to.idemo.james.artifactverifier.service.VerifierService;

import java.util.Map;

@RestController
@RequestMapping("/nexuswebhook")
public class NexusWebhookHandler {
    private static final Logger logger = LoggerFactory.getLogger(NexusWebhookHandler.class);

    private final VerifierService verifierService;

    public NexusWebhookHandler(VerifierService verifierService) {
        this.verifierService = verifierService;
    }

    @PostMapping(headers = "X-Nexus-Webhook-Id=rm:repository:component")
    public void handleComponentNotification(@RequestBody NexusEventWrapper<NexusComponent> component) {
        logger.info("Received component: {}", component);

    }

    @PostMapping(headers = "X-Nexus-Webhook-Id=rm:repository:asset")
    public void handleAssetNotification(@RequestBody NexusEventWrapper<NexusAsset> component) {
        logger.info("Received Asset: {}", component);
    }

    @PostMapping
    public void fallbackHandler(@RequestHeader("X-Nexus-Webhook-Id") String webhookId,
                                @RequestBody Map<String, Object> payload) {
        logger.info("Received webhook we're not interested in with type '{}': {}", webhookId, payload);
    }
}
