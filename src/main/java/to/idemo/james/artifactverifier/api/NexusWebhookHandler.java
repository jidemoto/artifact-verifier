package to.idemo.james.artifactverifier.api;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import to.idemo.james.artifactverifier.configuration.VerifierProperties;
import to.idemo.james.artifactverifier.domain.NexusAsset;
import to.idemo.james.artifactverifier.domain.NexusComponent;
import to.idemo.james.artifactverifier.exception.ArtifactValidationFailureException;
import to.idemo.james.artifactverifier.service.NotifierService;
import to.idemo.james.artifactverifier.service.VerifierService;
import to.idemo.james.artifactverifier.service.nexus.NexusClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/nexuswebhook")
@Timed
public class NexusWebhookHandler {
    private static final Logger logger = LoggerFactory.getLogger(NexusWebhookHandler.class);

    private final VerifierService verifierService;
    private final NexusClient repoClient;
    private final Set<String> internalProjects;
    private final NotifierService notifierService;
    private final Timer verificationSuccessTimer;
    private final Timer verificationFailureTimer;

    public NexusWebhookHandler(VerifierService verifierService,
                               NexusClient repoClient,
                               VerifierProperties verifierProperties,
                               NotifierService notifierService,
                               MeterRegistry registry) {
        this.verifierService = verifierService;
        this.repoClient = repoClient;
        this.internalProjects = verifierProperties.getInternalProjects();
        this.notifierService = notifierService;
        this.verificationSuccessTimer = registry.timer("verification_time", "outcome", "success");
        this.verificationFailureTimer = registry.timer("verification_time", "outcome", "failure");

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
        Optional<String> sha256Hash = repoClient.getSha256Hash(component);
        if(sha256Hash.isPresent()) {
            try {
                verifierService.verifyArtifact(sha256Hash.get());
            } catch (ArtifactValidationFailureException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Hidden
    @PostMapping(headers = "X-Nexus-Webhook-Id=rm:repository:asset")
    public void handleAssetNotification(@RequestBody NexusAsset asset) {
        logger.info("Received asset: {}", asset);
        if(!asset.getAction().equals("CREATED")) {
            //We're only going to handle created assets
            return;
        }

        Instant eventTime = Instant.parse(asset.getTimestamp());

        String name = asset.getAsset().getName();
        String[] assetSegments = name.split("/");
        String dependencyName = name.startsWith("@")
                ? assetSegments[0] + "/" + assetSegments[1]
                : assetSegments[0];
        if(internalProjects.contains(dependencyName)) {
            Optional<String> sha256Hash = repoClient.getSha256Hash(asset);
            if(sha256Hash.isPresent()) {
                try {
                    verifierService.verifyArtifact(sha256Hash.get());
                    logger.info("Asset {} passed internal strategy verification", dependencyName);
                    verificationSuccessTimer.record(Duration.between(eventTime, Instant.now()));
                } catch (ArtifactValidationFailureException e) {
                    logger.warn("Alerting on notifiers");
                    notifierService.alert(dependencyName, asset.getAsset().getFormat(), asset.getAsset().getAssetId(), asset.getInitiator(), e.getMessage());
                    verificationFailureTimer.record(Duration.between(eventTime, Instant.now()));
                }
            }
        } else {
            logger.info("Passing on processing {} -- external artifact", dependencyName);
        }
    }

    @Hidden
    @PostMapping
    public void fallbackHandler(@RequestHeader("X-Nexus-Webhook-Id") String webhookId,
                                @RequestBody Map<String, Object> payload) {
        logger.info("Received webhook we're not interested in with type '{}': {}", webhookId, payload);
    }
}
