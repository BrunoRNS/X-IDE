package org.xide.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.*;
import org.xide.common.ProtocolMessage;
import javax.net.ssl.*;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.*;

public final class ClientConnection implements AutoCloseable {
    private WebSocketContainer container;
    private final BlockingQueue<String> messages=new ArrayBlockingQueue<>(16);
    private Session session;
    private static final ObjectMapper JSON=new ObjectMapper();

    public void connect(URI uri, String username, char[] password, Path trustCertificate) throws Exception {
        if (username == null || username.isBlank() || password == null || password.length == 0)
            throw new IllegalArgumentException("--user and --password are required");
        if (!"wss".equalsIgnoreCase(uri.getScheme()))
            throw new IllegalArgumentException("Only wss:// endpoints are accepted; credentials must never be sent over plain WebSocket.");
        configureTls13(trustCertificate);
        container=ContainerProvider.getWebSocketContainer();
        String token=Base64.getUrlEncoder().withoutPadding().encodeToString((username+":"+new String(password)).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        ClientEndpointConfig config=ClientEndpointConfig.Builder.create()
                .preferredSubprotocols(List.of("xide", "xide-auth."+token)).build();
        Endpoint endpoint=new Endpoint() {
            @Override public void onOpen(Session s,EndpointConfig endpointConfig) { s.addMessageHandler(String.class,messages::offer); }
        };
        session=container.connectToServer(endpoint,config,uri);
        if (!"xide".equals(session.getNegotiatedSubprotocol())) {
            try { session.close(); } catch (Exception ignored) {}
            throw new SecurityException("The server did not authenticate this client during the WebSocket handshake.");
        }
    }

    private static void configureTls13(Path certificatePath) throws Exception {
        TrustManager[] trustManagers = null;
        if (certificatePath != null) {
            CertificateFactory factory=CertificateFactory.getInstance("X.509");
            KeyStore store=KeyStore.getInstance(KeyStore.getDefaultType()); store.load(null,null);
            try(InputStream in=Files.newInputStream(certificatePath)) { store.setCertificateEntry("xide-local-test-cert",factory.generateCertificate(in)); }
            TrustManagerFactory tmf=TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()); tmf.init(store);
            trustManagers=tmf.getTrustManagers();
        }
        SSLContext context=SSLContext.getInstance("TLSv1.3");
        context.init(null,trustManagers,null);
        SSLContext.setDefault(context);
    }

    public ProtocolMessage ping(String id,String payload,long timeoutSeconds) throws Exception {
        if(session==null||!session.isOpen()) throw new IllegalStateException("Not connected");
        session.getBasicRemote().sendText(JSON.writeValueAsString(ProtocolMessage.ping(id,payload)));
        String text=messages.poll(timeoutSeconds,TimeUnit.SECONDS);
        if(text==null) throw new IllegalStateException("Timed out waiting for server");
        return JSON.readValue(text,ProtocolMessage.class);
    }
    @Override public void close() throws Exception { if(session!=null&&session.isOpen()) session.close(); }
}
