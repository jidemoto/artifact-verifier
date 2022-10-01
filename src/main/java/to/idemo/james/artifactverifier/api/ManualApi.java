package to.idemo.james.artifactverifier.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import to.idemo.james.artifactverifier.exception.ArtifactValidationFailureException;
import to.idemo.james.artifactverifier.service.RekorService;
import to.idemo.james.artifactverifier.service.VerifierService;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/manual")
public class ManualApi {

    private final VerifierService verifierService;
    private final RekorService rekorService;

    public ManualApi(VerifierService verifierService, RekorService rekorService) {
        this.verifierService = verifierService;
        this.rekorService = rekorService;
    }

    @PostMapping
    public ResponseEntity<VerificationResponse> verifyHash(@RequestParam String sha256) {
        try {
            verifierService.verifyArtifact(sha256);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } catch (ArtifactValidationFailureException e) {
            Map<String, Exception> exceptionMap = e.getExceptionMap();
            HttpStatus status = exceptionMap.size() == 0
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            VerificationResponse response = new VerificationResponse(rekorService.getRekorLocation(),
                    exceptionMap
                            .entrySet()
                            .stream()
                            .map(entry -> new EntryVerificationFailure(
                                    entry.getKey(),
                                    entry.getValue().getMessage()
                            ))
                            .collect(Collectors.toSet()));
            return new ResponseEntity<>(response, status);
        }
    }

    record VerificationResponse(String rekorLocation, Collection<EntryVerificationFailure> failures) {
    }

    record EntryVerificationFailure(String uuid, String reason) {
    }
}
