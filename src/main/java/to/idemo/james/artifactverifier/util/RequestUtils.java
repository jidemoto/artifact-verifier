package to.idemo.james.artifactverifier.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class RequestUtils {
    //Inspired by https://www.baeldung.com/how-to-use-resttemplate-with-basic-authentication-in-spring
    public static String generateBasicAuthHeader(String username, String password) {
        String auth = username + ":" + password;
        byte[] encodedAuth = Base64.getEncoder().encode(
                auth.getBytes(StandardCharsets.US_ASCII));
        return "Basic " + new String(encodedAuth);
    }
}
