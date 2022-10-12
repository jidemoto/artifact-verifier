package to.idemo.james.artifactverifier.api;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import to.idemo.james.artifactverifier.domain.NexusAsset;
import to.idemo.james.artifactverifier.domain.NexusComponent;
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

    @Operation(
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "The Component or Asset depending on the Nexus Webhook Type",
                    required = true,
                    content = {
                            @Content(mediaType = "application/json",
                                    schema = @Schema(oneOf = {NexusComponent.class, NexusAsset.class, Object.class})),
                    }
            )
    )
    @PostMapping(headers = "X-Nexus-Webhook-Id=rm:repository:component")
    public void handleComponentNotification(@RequestBody NexusComponent component) {
        logger.info("Received component: {}", component);

    }

    @Hidden
    @PostMapping(headers = "X-Nexus-Webhook-Id=rm:repository:asset")
    public void handleAssetNotification(@RequestBody NexusAsset component) {
        logger.info("Received Asset: {}", component);
    }

    @Hidden
    @PostMapping
    public void fallbackHandler(@RequestHeader("X-Nexus-Webhook-Id") String webhookId,
                                @RequestBody Map<String, Object> payload) {
        logger.info("Received webhook we're not interested in with type '{}': {}", webhookId, payload);
    }
}
