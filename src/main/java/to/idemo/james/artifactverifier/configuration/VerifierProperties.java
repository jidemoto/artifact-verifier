package to.idemo.james.artifactverifier.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@ConfigurationProperties(prefix = "verifier")
public class VerifierProperties {
    private Set<String> allowedEmailDomains;
    private Set<String> allowedProviders;
    private Set<String> internalProjects;

    public VerifierProperties() {
    }

    public VerifierProperties(Set<String> allowedEmailDomains, Set<String> allowedProviders, Set<String> internalProjects) {
        this.allowedEmailDomains = allowedEmailDomains;
        this.allowedProviders = allowedProviders;
        this.internalProjects = internalProjects;
    }

    public Set<String> getAllowedEmailDomains() {
        return allowedEmailDomains;
    }

    public Set<String> getAllowedProviders() {
        return allowedProviders;
    }

    public Set<String> getInternalProjects() {
        return internalProjects;
    }

    public void setAllowedEmailDomains(Set<String> allowedEmailDomains) {
        this.allowedEmailDomains = allowedEmailDomains;
    }

    public void setAllowedProviders(Set<String> allowedProviders) {
        this.allowedProviders = allowedProviders;
    }

    public void setInternalProjects(Set<String> internalProjects) {
        this.internalProjects = internalProjects;
    }
}
