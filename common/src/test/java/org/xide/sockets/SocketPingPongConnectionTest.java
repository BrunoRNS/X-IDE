package org.xide.sockets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.server.Server;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;
import org.xide.sockets.annotations.WebSocketRouter;

/**
 * Connection tests for the shared socket protocol: a real WebSocket server and a real
 * WebSocket client exchanging frames over the wire, with the ping/pong handshake driven
 * by the common classes ({@link SocketMessage}, {@link SocketMessageCodec},
 * {@link WebSocketRouter} and the {@link WebSocketController} routes).
 *
 * <p>
 * The endpoints below mirror the production ones (server: {@code
 * org.xide.sockets.endpoints.WebSocketEndpoint}, client: {@code
 * org.xide.client.sockets.WebSocketEndpoint}), but live in the test sources because
 * common cannot depend on the client and server modules (Maven forbids cyclic module
 * dependencies).
 *
 * <p>
 * Requests are dispatched through the router and responses are identified independently
 * of their payload, so a response containing {@code pong} is never dispatched as another
 * request.
 */
public class SocketPingPongConnectionTest {

	private static final long TIMEOUT_SECONDS = 10;

	private static Server server;

	private static URI serverUri;

	@BeforeClass
	public static void startServer() throws DeploymentException {
		// Port 0 = ephemeral port, so the tests never fight over a fixed port.
		server = new Server("127.0.0.1", 0, "/ws", null, TestServerEndpoint.class);
		server.start();
		serverUri = URI.create("ws://127.0.0.1:" + server.getPort() + "/ws/socket");
	}

	@AfterClass
	public static void stopServer() {
		if (server != null) {
			server.stop();
		}
	}

	@Before
	public void resetHub() {
		Hub.reset();
	}

	@Test(timeout = 30_000)
	public void serverRespondsWithPongWhenClientSendsPing() throws Exception {
		ResponseSink receivedByClient = new ResponseSink();
		Session session = ClientManager.createClient()
			.connectToServer(new TestClientEndpoint(receivedByClient), serverUri);
		try {
			send(session, SocketMessage.text("/develop/ping", "ping"));

			assertEquals(SocketMessage.textResponse("/develop/ping", "pong"), receivedByClient.awaitPong());
		}
		finally {
			session.close();
		}
	}

	@Test(timeout = 30_000)
	public void clientRespondsWithPongWhenServerSendsPing() throws Exception {
		Session session = ClientManager.createClient()
			.connectToServer(new TestClientEndpoint(new ResponseSink()), serverUri);
		try {
			assertTrue("Server did not accept the connection in time",
					Hub.serverReady.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

			send(Hub.clientSessionOnServer, SocketMessage.text("/develop/ping", "ping"));

			assertEquals(SocketMessage.textResponse("/develop/ping", "pong"), Hub.receivedByServer.awaitPong());
		}
		finally {
			session.close();
		}
	}

	/**
	 * Server side of the connection: decodes the frame, dispatches pings through the
	 * router and sends the response back, while capturing pongs sent by the client.
	 */
	@ServerEndpoint("/socket")
	public static final class TestServerEndpoint {

		private final WebSocketRouter router = routerWithPingController();

		@OnOpen
		public void onOpen(Session session) {
			Hub.clientSessionOnServer = session;
			Hub.serverReady.countDown();
		}

		@OnMessage
		public void onMessage(Session session, String json) {
			handleFrame(session, json, router, Hub.receivedByServer);
		}

		@OnError
		public void onError(Session session, Throwable error) {
			Hub.receivedByServer.receiveError(error);
		}

	}

	/**
	 * Client side of the connection: decodes the frame, dispatches pings through the
	 * router and sends the response back, while capturing pongs sent by the server.
	 */
	@ClientEndpoint
	public static final class TestClientEndpoint {

		private final WebSocketRouter router = routerWithPingController();

		private final ResponseSink receivedFromServer;

		private volatile Session session;

		TestClientEndpoint(ResponseSink receivedFromServer) {
			this.receivedFromServer = receivedFromServer;
		}

		@OnOpen
		public void onOpen(Session session) {
			this.session = session;
		}

		@OnMessage
		public void onMessage(String json) {
			handleFrame(session, json, router, receivedFromServer);
		}

		@OnError
		public void onError(Session session, Throwable error) {
			receivedFromServer.receiveError(error);
		}

	}

	private static void handleFrame(Session session, String json, WebSocketRouter router, ResponseSink sink) {
		try {
			SocketMessage message = SocketMessageCodec.decode(json);
			if (message.response()) {
				sink.receive(message);
			}
			else {
				Optional<SocketMessage> response = router.dispatch(message);
				if (response.isPresent()) {
					session.getAsyncRemote().sendText(SocketMessageCodec.encode(response.get()));
				}
			}
		}
		catch (Exception e) {
			sink.receiveError(e);
		}
	}

	/**
	 * Mirrors the real deployment, where each side registers its own ping controller
	 * (server: DevelopSocketController, client: PingController).
	 */
	private static WebSocketRouter routerWithPingController() {
		WebSocketRouter router = new WebSocketRouter();
		router.register(new PingController());
		return router;
	}

	private static void send(Session session, SocketMessage message) throws Exception {
		session.getAsyncRemote().sendText(SocketMessageCodec.encode(message)).get();
	}

	/** Same contract as the production controllers: ping in, pong out. */
	@WebSocketController
	public static final class PingController {

		@WebSocketRoute("/develop/ping")
		public String ping(SocketMessage request) {
			return "pong";
		}

	}

	/** Shared state between the container-created server endpoint and the tests. */
	private static final class Hub {

		static volatile Session clientSessionOnServer;
		static CountDownLatch serverReady;
		static ResponseSink receivedByServer;

		static void reset() {
			clientSessionOnServer = null;
			serverReady = new CountDownLatch(1);
			receivedByServer = new ResponseSink();
		}

	}

	/** Captures the pong (or the failure) of one direction of the handshake. */
	private static final class ResponseSink {

		private final CountDownLatch pong = new CountDownLatch(1);

		private final AtomicReference<SocketMessage> message = new AtomicReference<>();

		private final AtomicReference<Throwable> error = new AtomicReference<>();

		void receive(SocketMessage message) {
			this.message.set(message);
			pong.countDown();
		}

		void receiveError(Throwable error) {
			this.error.compareAndSet(null, error);
			pong.countDown();
		}

		SocketMessage awaitPong() throws InterruptedException {
			if (!pong.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
				fail("Timed out after " + TIMEOUT_SECONDS + "s waiting for a pong");
			}
			SocketMessage received = message.get();
			if (received == null) {
				fail("Received no pong" + describeError());
			}
			return received;
		}

		private String describeError() {
			Throwable failure = error.get();
			return failure == null ? "" : ": " + failure;
		}

	}

}
