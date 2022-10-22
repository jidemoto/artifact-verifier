package to.idemo.james.artifactverifier.configuration;

import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcTagsContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
public class NexusTagContributorConfiguration {
    @Bean
    public WebMvcTagsContributor webMvcTagsContributor() {
        return new WebMvcTagsContributor() {
            @Override
            public Iterable<Tag> getTags(HttpServletRequest request, HttpServletResponse response, Object handler, Throwable exception) {
                String nexusWebhookId = request.getHeader("X-Nexus-Webhook-Id");
                Tags tags = Tags.empty();
                if (nexusWebhookId != null) {
                    tags = tags.and(Tag.of("nexusWebhookId", nexusWebhookId));
                }
                return tags;
            }

            @Override
            public Iterable<Tag> getLongRequestTags(HttpServletRequest request, Object handler) {
                return null;
            }
        };
    }
}
