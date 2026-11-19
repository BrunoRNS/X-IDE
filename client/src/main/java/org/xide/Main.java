package org.xide;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.swing.JOptionPane;
import org.xide.client.sockets.WebSocketClient;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;
import org.xide.ui.ConnectFrame;
import org.xide.ui.LoadingFrame;
import org.xide.ui.LoginFrame;

public final class Main {

	private static final String DEFAULT_ROUTE = "/develop/ping";

	private static final String DEFAULT_MESSAGE = "";

	public static void main(String[] args) throws Exception {
		if (VirtualMachineDetector.isVirtualMachine()) {
			System.err.println("This client is not permitted to run in a virtual machine.");
			System.exit(1);
		}

		String endpoint = null;
		String route = null;
		String message = null;

		for (int i = 0; i < args.length; i++) {
			switch (args[i]) {
				case "--endpoint" -> {
					if (endpoint != null) {
						System.err.println("--endpoint may only be specified once");
						System.exit(2);
					}
					endpoint = value(args, ++i, "--endpoint");
				}
				case "--message" -> message = value(args, ++i, "--message");
				case "--user", "--password" -> {
					System.err.println(args[i] + ": not yet implemented");
					System.exit(2);
				}
				default -> {
					if (args[i].startsWith("--")) {
						System.err.println("Unknown option: " + args[i]);
						System.exit(2);
					}
					else if (args[i].startsWith("ws://") || args[i].startsWith("wss://")) {
						System.err.println("Endpoint must be passed with --endpoint");
						System.exit(2);
					}
					else if (route == null) {
						route = args[i];
					}
					else {
						System.err.println("Unexpected argument: " + args[i]);
						System.exit(2);
					}
				}
			}
		}
		URI uri;
		if (endpoint == null) {
			ConnectFrame loginFrame = new ConnectFrame();
			endpoint = loginFrame.awaitEndpoint();
		}
		uri = URI.create(endpoint);
		String effectiveRoute = route != null ? route : DEFAULT_ROUTE;
		String payload = message != null ? message : DEFAULT_MESSAGE;

		LoadingFrame loadingFrame = new LoadingFrame();
		WebSocketClient client;
		try {
			client = WebSocketClient.getInstance(uri);
		}
		catch (Exception e) {
			loadingFrame.dispose();
			JOptionPane.showMessageDialog(null, "Could not connect to the server: " + e.getMessage(),
					"Connection failed", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
			return;
		}
		loadingFrame.dispose();

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			try {
				client.close();
			}
			catch (IOException e) {
				System.err.println("Failed to close WebSocket client: " + e.getMessage());
			}
		}, "websocket-client-shutdown"));
		new LoginFrame();
		client.sendRequest(effectiveRoute, payload).get();
		SocketMessage response = client.receive(effectiveRoute, 10, TimeUnit.SECONDS);
		if (response == null) {
			System.err.println("No response for " + effectiveRoute + " within 10 seconds");
			System.exit(1);
		}
		System.out.println(SocketMessageCodec.encode(response));
		if (args.length == 0) {
			System.out.println("Client connected. Press Ctrl+C to stop.");
			new CountDownLatch(1).await();
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
