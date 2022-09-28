package to.idemo.james.artifactverifier.exception;

import java.util.Map;

public class ArtifactValidationFailureException extends Exception {
    private final Map<String, Exception> exceptionMap;

    public ArtifactValidationFailureException(String message, Map<String, Exception> exceptionMap) {
        super(message);
        this.exceptionMap = exceptionMap;
    }

    public Map<String, Exception> getExceptionMap() {
        return exceptionMap;
    }
}
