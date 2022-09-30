package to.idemo.james.artifactverifier.service;

import to.idemo.james.artifactverifier.domain.Rekord;

import java.util.Collection;

public interface RekorService {
    Collection<String> getUuids(String shaHash);

    Rekord getRekord(String rekorUuid) throws IllegalArgumentException;
}
