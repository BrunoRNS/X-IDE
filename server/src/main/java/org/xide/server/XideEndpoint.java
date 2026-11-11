package org.xide.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import org.xide.common.ProtocolMessage;
import java.util.concurrent.*;

@ServerEndpoint(value="/ws", subprotocols={"xide"}, configurator=AuthConfigurator.class)
public class XideEndpoint {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final MessageService SERVICE = new MessageService();
    private static final ExecutorService DB_WORKERS = Executors.newFixedThreadPool(4);
    private static volatile MessageRepository repository;
    static void setRepository(MessageRepository value) { repository=value; }

    @OnOpen public void onOpen(Session session, EndpointConfig config) {
        session.setMaxTextMessageBufferSize(65536);
        if (!AuthConfigurator.AUTHENTICATED_SUBPROTOCOL.equals(session.getNegotiatedSubprotocol())) {
            try { session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Authentication required")); }
            catch (Exception ignored) {}
        }
    }

    @OnMessage public void onMessage(String text, Session session) {
        if (!session.isOpen()) return;
        try {
            var request=JSON.readValue(text, ProtocolMessage.class);
            var response=SERVICE.handle(request);
            var repo=repository;
            if (repo != null) DB_WORKERS.submit(() -> {
                try { repo.save(request.requestId(),request.payload()); }
                catch (Exception e) { System.err.println("MySQL write failed: "+e.getMessage()); }
            });
            session.getAsyncRemote().sendText(JSON.writeValueAsString(response));
        } catch (Exception e) {
            try { session.getAsyncRemote().sendText(JSON.writeValueAsString(new ProtocolMessage("ERROR","invalid-request",e.getMessage()))); }
            catch (Exception ignored) { try { session.close(); } catch (Exception ignoredAgain) {} }
        }
    }
}
