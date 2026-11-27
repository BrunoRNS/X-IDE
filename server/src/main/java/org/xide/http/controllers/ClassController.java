package org.xide.http.controllers;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import org.xide.http.service.ClassService;

public class ClassController {

	private static final Logger LOGGER = Logger.getLogger(ClassController.class.getName());

	private static final ConcurrentHashMap<Long, HostedClass> SERVERS = new ConcurrentHashMap<>();

	private record HostedClass(long id, String ip, int port, int files, Javalin server) {

		Map<String, Object> toJson() {
			return Map.of("id", id, "ip", ip, "port", port, "url", url(), "files", files);
		}

		String url() {
			String host = ip.contains(":") && !ip.startsWith("[") ? "[" + ip + "]" : ip;
			return "http://" + host + ":" + port;
		}

	}

	public static void page(Context ctx) {
		ctx.render("templates/class.html", Map.of());
	}

	public static void index(Context ctx) {
		Long id = pathId(ctx);
		if (id == null) {
			return;
		}

		List<UploadedFile> files = ctx.uploadedFiles("files");
		if (files == null || files.isEmpty()) {
			ctx.status(400).json(Map.of("error", "Choose a folder with at least one file."));
			return;
		}

		try {
			Javalin server = ClassService.createServer(files, ctx.formParams("paths"), id);
			HostedClass room = new HostedClass(id, resolveHost(ctx), server.port(), files.size(), server);
			HostedClass previous = SERVERS.put(id, room);

			if (previous != null) {
				previous.server().stop();
			}

			ctx.json(room.toJson());
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Could not host classroom " + id, e);
			ctx.status(500).json(Map.of("error", "Could not host this folder."));
		}
	}

	public static void show(Context ctx) {
		Long id = pathId(ctx);
		if (id == null) {
			return;
		}

		HostedClass room = SERVERS.get(id);
		if (room == null) {
			ctx.status(404).json(Map.of("error", "Classroom " + id + " is not hosted."));
			return;
		}

		ctx.json(room.toJson());
	}

	public static void list(Context ctx) {
		List<Map<String, Object>> rooms = SERVERS.values()
			.stream()
			.sorted(Comparator.comparingLong(HostedClass::id))
			.map(HostedClass::toJson)
			.toList();
		ctx.json(rooms);
	}

	static void stopAll() {
		for (HostedClass room : SERVERS.values()) {
			try {
				room.server().stop();
			}
			catch (RuntimeException e) {
				LOGGER.log(Level.WARNING, "Could not stop classroom " + room.id(), e);
			}
		}
		SERVERS.clear();
	}

	private static Long pathId(Context ctx) {
		try {
			long id = Long.parseLong(ctx.pathParam("id"));
			if (id < 1 || id > Integer.MAX_VALUE) {
				throw new NumberFormatException();
			}
			return id;
		}
		catch (NumberFormatException e) {
			ctx.status(400).json(Map.of("error", "Classroom ID must be a positive number."));
			return null;
		}
	}

	/**
	 * Students must reach the hosted port from their own devices, so prefer the address
	 * the teacher used to open the app. When that is localhost, fall back to the
	 * configured TLS host or the machine's LAN address.
	 */
	private static String resolveHost(Context ctx) {
		String host = ctx.req().getServerName();
		if (host == null || host.isBlank()) {
			host = ctx.host();
		}
		if (host == null || host.isBlank()) {
			return "127.0.0.1";
		}
		host = stripPort(host);

		if (isLoopback(host)) {
			String configured = System.getenv("TLS_HOST");
			if (configured != null && !configured.isBlank() && !"localhost".equalsIgnoreCase(configured)) {
				return configured;
			}

			String lan = lanAddress();
			if (lan != null) {
				return lan;
			}
		}

		return host;
	}

	private static String stripPort(String host) {
		if (host.startsWith("[")) {
			int end = host.indexOf(']');
			return end > 0 ? host.substring(1, end) : host;
		}

		int colon = host.indexOf(':');
		if (colon > 0 && colon == host.lastIndexOf(':')) {
			return host.substring(0, colon);
		}

		return host;
	}

	private static boolean isLoopback(String host) {
		return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "::1".equals(host)
				|| "0:0:0:0:0:0:0:1".equals(host);
	}

	private static String lanAddress() {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface network = interfaces.nextElement();
				String name = network.getName();

				if (!network.isUp() || network.isLoopback() || network.isVirtual() || name.startsWith("docker")
						|| name.startsWith("br-") || name.startsWith("veth")) {
					continue;
				}

				for (InterfaceAddress address : network.getInterfaceAddresses()) {
					InetAddress ip = address.getAddress();
					if (ip instanceof Inet4Address && ip.isSiteLocalAddress()) {
						return ip.getHostAddress();
					}
				}
			}
		}
		catch (Exception e) {
			LOGGER.log(Level.FINE, "Could not detect a LAN address", e);
		}
		return null;
	}

}
