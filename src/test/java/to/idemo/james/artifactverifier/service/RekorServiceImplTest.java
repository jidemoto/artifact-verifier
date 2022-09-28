package to.idemo.james.artifactverifier.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import to.idemo.james.artifactverifier.domain.Rekord;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class RekorServiceImplTest {

    @Test
    void getUuids() {
        RekorServiceImpl rekorService = new RekorServiceImpl(new RestTemplate());
        Collection<String> uuidsForHash = rekorService.getUuids("c7e37479bddbe14827a95e2313fba86c493b3a69f34e40850fa0be49ee7f4164");
        System.out.println(uuidsForHash);
        assertTrue(uuidsForHash.contains("362f8ecba72f43266627ee0787ec73d5bbe849c95f983d26a0ab4939595f01f76d4e0f46d522405d"));
    }

    @Test
    void getRekord() {
        RekorServiceImpl rekorService = new RekorServiceImpl(new RestTemplate());
        Rekord rekord = rekorService.getRekord("362f8ecba72f43266627ee0787ec73d5bbe849c95f983d26a0ab4939595f01f76d4e0f46d522405d");
        assertEquals(3993159L, rekord.getLogIndex());
    }
}