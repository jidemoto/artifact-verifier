package to.idemo.james.artifactverifier.notification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
@Conditional(SlackNotifierImpl.SlackNotifierConditional.class)
public class SlackNotifierImpl implements Notifier {
    private static final Logger logger = LoggerFactory.getLogger(SlackNotifierImpl.class);

    private final RestTemplate restTemplate;
    private final String webhookUrl;

    public SlackNotifierImpl(RestTemplate restTemplate, SlackNotificationProperties properties) {
        logger.info("Slack notifier initialized");
        this.restTemplate = restTemplate;
        this.webhookUrl = properties.getWebhookUrl();
    }

    @Override
    public void warn(String artifactName, String artifactType, String artifactLocation, String attributedUser, String reason) {
        //No warnings right now.  No-op
    }

    @Override
    public void alert(String artifactName, String artifactType, String artifactUniqueId, String attributedUser, String reason) {
        String message = artifactName + " failed artifact validation!";
        List<SlackField> fields = new ArrayList<>();
        fields.add(new SlackField("Artifact", artifactName, true));
        fields.add(new SlackField("Type", artifactType, true));
        fields.add(new SlackField("Artifact ID", artifactUniqueId, false));
        fields.add(new SlackField("Initiator", attributedUser, true));
        fields.add(new SlackField("Failure Reason", reason, false));
        MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
        headerMap.add(HttpHeaders.CONTENT_TYPE, "application/json");
        HttpEntity<SlackMessage> entity = new HttpEntity<>(new SlackMessage(message, fields), headerMap);
        ResponseEntity<String> response = restTemplate.exchange(webhookUrl, HttpMethod.POST, entity, String.class);
        if(!response.getStatusCode().is2xxSuccessful()) {
            HttpStatus statusCode = response.getStatusCode();
            logger.error("Webhook send failure.  Got code {}: {}", statusCode.value(), statusCode.getReasonPhrase());
        }
        logger.info("Response from slack was {}", response.getBody());
    }

    @Data
    @AllArgsConstructor
    static class SlackMessage {
        private String text;
        private List<SlackField> fields;
    }

    @Data
    @AllArgsConstructor
    static class SlackField {
        private String title;
        private String value;
        @JsonProperty("short")
        private boolean isShort;
    }


    public static class SlackNotifierConditional implements Condition {
        @Override
        public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
            // We don't have a great way to evaluate the conditional directly (we're relying on Spring's relaxed
            // property resolution through ConfigurationProperties-annotated classes to support normalization of
            // environment variables).  We'll bind props to
            return Binder.get(context.getEnvironment())
                    .bind("notifications.slack", SlackNotificationProperties.class)
                    .map(props -> {
                        String requiredProperty = props.getWebhookUrl();
                        return requiredProperty != null && !requiredProperty.isEmpty();
                    }).orElseGet(() -> false);
        }
    }
}
