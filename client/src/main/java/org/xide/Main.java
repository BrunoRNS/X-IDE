package org.xide;

import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.swing.JOptionPane;

import jakarta.websocket.DeploymentException;
import org.xide.client.sockets.WebSocketClient;
import org.xide.frame.holders.ConnectFrameHolder;
import org.xide.frame.holders.LoadingFrameHolder;
import org.xide.frame.holders.LoginFrameHolder;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;

public final class Main {

	private static final String VERSION = "1.0.0";

	private static final String DEFAULT_ROUTE = "/develop/ping";

	private static final String DEFAULT_MESSAGE = "";

	private static String endpoint = null;

	private static String route = null;

	private static String message = null;

	public static void main(String[] args) throws Exception {
		ensureNotVM();
		parseArguments(args);

		// Handle --version early exit
		for (String arg : args) {
			if ("--version".equals(arg)) {
				System.out.println("X-IDE Client " + VERSION);
				return;
			}
		}

		String effectiveRoute = route != null ? route : DEFAULT_ROUTE;
		String payload = message != null ? message : DEFAULT_MESSAGE;

		WebSocketClient client = connect(resolveEndpoint());
		registerShutdownHook(client);
		LoginFrameHolder.startFrame();
		sendAndPrint(client, effectiveRoute, payload);
		awaitShutdown(args);
	}

	private static URI resolveEndpoint() throws Exception {
		if (endpoint == null) {
			ConnectFrameHolder.startFrame();
			endpoint = ConnectFrameHolder.awaitEndpoint();
		}
		return URI.create(endpoint);
	}

	private static WebSocketClient connect(URI uri) {
		LoadingFrameHolder.startFrame();
		try {
			WebSocketClient client = WebSocketClient.getInstance(uri);
			LoadingFrameHolder.disposeFrame();
			return client;
		}
		catch (DeploymentException | IOException | GeneralSecurityException e) {
			LoadingFrameHolder.disposeFrame();
			JOptionPane.showMessageDialog(null, "Could not connect to the server: " + e.getMessage(),
					"Connection failed", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
			return null;
		}
	}

	private static void registerShutdownHook(WebSocketClient client) {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				client.close();
			}
			catch (IOException e) {
				System.err.println("Failed to close WebSocket client: " + e.getMessage());
			}
		}, "websocket-client-shutdown"));
	}

	private static void sendAndPrint(WebSocketClient client, String route, String payload) throws Exception {
		client.sendRequest(route, payload).get();
		SocketMessage response = client.receive(route, 10, TimeUnit.SECONDS);
		if (response == null) {
			System.err.println("No response for " + route + " within 10 seconds");
			System.exit(1);
		}
		System.out.println(SocketMessageCodec.encode(response));
	}

	private static void awaitShutdown(String[] args) throws InterruptedException {
		if (args.length == 0) {
			System.out.println("Client connected. Press Ctrl+C to stop.");
			new CountDownLatch(1).await();
		}
	}

	private static void parseArguments(String[] args) {
		for (int i = 0; i < args.length; i++) {
			Argument argument = Argument.from(args[i]);
			if (argument == null) {
				handlePositional(args[i]);
				continue;
			}
			switch (argument) {
				case ENDPOINT -> {
					if (endpoint != null) {
						fail("--endpoint may only be specified once");
					}
					endpoint = value(args, ++i, argument.flag());
				}
				case MESSAGE -> message = value(args, ++i, argument.flag());
				case USER, PASSWORD -> fail(argument.flag() + ": not yet implemented");
			}
		}
	}

	private static void handlePositional(String arg) {
		if (arg.startsWith("--")) {
			fail("Unknown option: " + arg);
		}
		else if (arg.startsWith("ws://") || arg.startsWith("wss://")) {
			fail("Endpoint must be passed with --endpoint");
		}
		else if (route == null) {
			route = arg;
		}
		else {
			fail("Unexpected argument: " + arg);
		}
	}

	private static void fail(String message) {
		System.err.println(message);
		System.exit(2);
	}

	private static void ensureNotVM() {
		if (VirtualMachineDetector.isVirtualMachine()) {
			System.err.println("This client is not permitted to run in a virtual machine.");
			System.exit(1);
		}
	}

	private static String value(String[] args, int index, String flag) {
		if (index >= args.length) {
			System.err.println(flag + " requires a value");
			System.exit(2);
		}
		return args[index];
	}

}
