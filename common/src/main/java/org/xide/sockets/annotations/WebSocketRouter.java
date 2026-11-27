package org.xide.sockets.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.websocket.Session;
import org.xide.protocol.SocketMessage;
import org.xide.protocol.SocketMessageCodec;

public class WebSocketRouter {

	private static final ObjectMapper MAPPER = new ObjectMapper();

	private final Map<String, RouteHandler> routes = new ConcurrentHashMap<>();

	public void register(Object controller) {
		Objects.requireNonNull(controller, "controller");
		if (!controller.getClass().isAnnotationPresent(WebSocketController.class)) {
			throw new IllegalArgumentException("WebSocket controller must be annotated with @WebSocketController");
		}
		for (Method method : controller.getClass().getDeclaredMethods()) {
			WebSocketRoute route = method.getAnnotation(WebSocketRoute.class);
			if (route == null) {
				continue;
			}
			validateRoute(controller, method, route.value());
			RouteHandler previous = routes.putIfAbsent(route.value(),
					new RouteHandler(controller, method, argumentResolvers(method), route.protectedRoute()));
			if (previous != null) {
				throw new IllegalStateException("Duplicate WebSocket route: " + route.value());
			}
		}
	}

	/**
	 * Dispatches a request and wraps a non-null handler result in its response payload.
	 * Void and null results produce no response.
	 */
	public Optional<SocketMessage> dispatch(SocketMessage request) {
		return dispatch(request, null);
	}

	public Optional<SocketMessage> dispatch(SocketMessage request, Session session) {
		Objects.requireNonNull(request, "request");
		RouteHandler handler = routes.get(request.route());
		if (handler == null) {
			throw new IllegalArgumentException("Unknown WebSocket route: " + request.route());
		}
		try {
			ArgumentResolver[] resolvers = handler.resolvers();
			Object[] arguments = new Object[resolvers.length];
			for (int i = 0; i < arguments.length; i++) {
				arguments[i] = resolvers[i].resolve(request, session);
			}
			Object result = handler.method().invoke(handler.controller(), arguments);
			if (result == null) {
				return Optional.empty();
			}
			return Optional.of(SocketMessage.response(request.route(), SocketMessageCodec.toJsonNode(result)));
		}
		catch (InvocationTargetException e) {
			throw new IllegalStateException("WebSocket route failed: " + request.route(), e.getCause());
		}
		catch (IllegalAccessException e) {
			throw new IllegalStateException("Cannot invoke WebSocket route: " + request.route(), e);
		}
	}

	public boolean hasRoute(String route) {
		return routes.containsKey(route);
	}

	/** Reports whether the route only accepts logged in sessions. */
	public boolean isProtected(String route) {
		RouteHandler handler = routes.get(route);
		return handler != null && handler.protectedRoute();
	}

	private static void validateRoute(Object controller, Method method, String path) {
		if (path == null || !path.startsWith("/") || path.startsWith("//")) {
			throw new IllegalArgumentException("WebSocket route must be an absolute path: " + path);
		}
		if (!Modifier.isPublic(method.getModifiers())) {
			throw new IllegalArgumentException(
					"Route " + path + " on " + controller.getClass().getName() + " must be public");
		}
		for (Parameter parameter : method.getParameters()) {
			if (parameter.getType() == SocketMessage.class || parameter.getType() == Session.class
					|| parameter.isAnnotationPresent(SocketParam.class)) {
				continue;
			}
			throw new IllegalArgumentException(
					"Route " + path + " on " + controller.getClass().getName() + " parameter '" + parameter.getName()
							+ "' must be SocketMessage" + " or annotated with @SocketParam");
		}
	}

	private static ArgumentResolver[] argumentResolvers(Method method) {
		Parameter[] parameters = method.getParameters();
		ArgumentResolver[] resolvers = new ArgumentResolver[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			Parameter parameter = parameters[i];
			if (parameter.getType() == SocketMessage.class) {
				resolvers[i] = (request, session) -> request;
				continue;
			}
			if (parameter.getType() == Session.class) {
				resolvers[i] = (request, session) -> session;
				continue;
			}
			resolvers[i] = new PayloadResolver(parameter.getAnnotation(SocketParam.class).value(),
					MAPPER.getTypeFactory().constructType(parameter.getParameterizedType()));
		}
		return resolvers;
	}

	/** Resolves a handler argument from a named field of the request payload. */
	private record PayloadResolver(String name, JavaType type) implements ArgumentResolver {
		@Override
		public Object resolve(SocketMessage request, Session session) {
			JsonNode payload = request.payload();
			JsonNode value = payload == null ? null : payload.get(name);
			if (value == null || value.isMissingNode()) {
				throw new IllegalArgumentException(
						"Missing parameter '" + name + "' for WebSocket route: " + request.route());
			}
			if (value.isNull() && type.getRawClass().isPrimitive()) {
				throw new IllegalArgumentException("Parameter '" + name + "' for WebSocket route: " + request.route()
						+ " is primitive and cannot be null");
			}
			return MAPPER.convertValue(value, type);
		}
	}

	private interface ArgumentResolver {

		Object resolve(SocketMessage request, Session session);

	}

	private record RouteHandler(Object controller, Method method, ArgumentResolver[] resolvers,
			boolean protectedRoute) {
	}

}
