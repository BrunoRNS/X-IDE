package org.xide.client.routing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.xide.protocol.SocketMessage;
import org.xide.sockets.annotations.WebSocketController;
import org.xide.sockets.annotations.WebSocketRoute;

public final class ClientWebSocketRouter {
    private final Map<String, RouteHandler> routes = new ConcurrentHashMap<>();

    public synchronized void register(Object controller) {
        Objects.requireNonNull(controller, "controller");
        Class<?> controllerClass = controller.getClass();
        if (!controllerClass.isAnnotationPresent(WebSocketController.class)) {
            throw new IllegalArgumentException(
                "WebSocket controller must be annotated with @WebSocketController"
            );
        }

        Map<String, RouteHandler> controllerRoutes = new HashMap<>();
        for (Method method : controllerClass.getDeclaredMethods()) {
            WebSocketRoute route = method.getAnnotation(WebSocketRoute.class);
            if (route == null) {
                continue;
            }
            validateRoute(controllerClass, method, route.value());
            if (controllerRoutes.putIfAbsent(
                    route.value(),
                    new RouteHandler(controller, method)
            ) != null || routes.containsKey(route.value())) {
                throw new IllegalStateException("Duplicate WebSocket route: " + route.value());
            }
        }
        if (controllerRoutes.isEmpty()) {
            throw new IllegalArgumentException(
                "WebSocket controller has no @WebSocketRoute methods: "
                    + controllerClass.getName()
            );
        }
        routes.putAll(controllerRoutes);
    }

    public boolean dispatch(SocketMessage message) {
        Objects.requireNonNull(message, "message");
        RouteHandler handler = routes.get(message.route());
        if (handler == null) {
            return false;
        }

        try {
            handler.method().invoke(handler.controller(), message);
            return true;
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(
                "WebSocket route failed: " + message.route(),
                e.getCause()
            );
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(
                "Cannot invoke WebSocket route: " + message.route(),
                e
            );
        }
    }

    private static void validateRoute(Class<?> controllerClass, Method method, String path) {
        if (path == null || !path.startsWith("/") || path.startsWith("//")) {
            throw new IllegalArgumentException(
                "WebSocket route must be an absolute path: " + path
            );
        }
        if (!Modifier.isPublic(controllerClass.getModifiers())
                || !Modifier.isPublic(method.getModifiers())
                || method.getParameterCount() != 1
                || method.getParameterTypes()[0] != SocketMessage.class
                || method.getReturnType() != void.class) {
            throw new IllegalArgumentException(
                "Client route " + path + " on " + controllerClass.getName()
                    + " must be public, accept one SocketMessage, and return void"
            );
        }
    }

    private record RouteHandler(Object controller, Method method) {
    }
}
