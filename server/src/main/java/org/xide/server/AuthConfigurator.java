package org.xide.server;

import jakarta.websocket.server.ServerEndpointConfig;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/** Authenticates a per-connection credential token while negotiating the WebSocket subprotocol. */
public final class AuthConfigurator extends ServerEndpointConfig.Configurator {
    static final String AUTHENTICATED_SUBPROTOCOL = "xide";
    private static final String TOKEN_PREFIX = "xide-auth.";
    private static volatile JdbcUserRepository users;
    static void setUserRepository(JdbcUserRepository repository) { users = repository; }

    @Override public String getNegotiatedSubprotocol(List<String> supported, List<String> requested) {
        if (!supported.contains(AUTHENTICATED_SUBPROTOCOL)) return "";
        for (String candidate : requested) {
            if (!candidate.startsWith(TOKEN_PREFIX)) continue;
            if (authenticateToken(candidate.substring(TOKEN_PREFIX.length())))
                return AUTHENTICATED_SUBPROTOCOL;
        }
        // No negotiated protocol means the endpoint closes the unauthenticated session.
        return "";
    }

    private boolean authenticateToken(String encodedCredentials) {
        char[] password = null;
        try {
            String credentials = new String(Base64.getUrlDecoder().decode(encodedCredentials), StandardCharsets.UTF_8);
            int separator = credentials.indexOf(':');
            if (separator <= 0) return false;
            String username = credentials.substring(0, separator);
            password = credentials.substring(separator + 1).toCharArray();
            JdbcUserRepository repository = users;
            return repository != null && repository.authenticate(username, password);
        } catch (RuntimeException e) {
            return false;
        } finally {
            if (password != null) java.util.Arrays.fill(password, '\0');
        }
    }
}
