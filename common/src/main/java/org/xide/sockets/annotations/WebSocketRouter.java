package org.xide.sockets.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.xide.protocol.SocketMessage;

public class WebSocketRouter {
    private Map<String, RouteHandler> routes = new ConcurrentHashMap<>();

    public void register(Object controller) {
        Objects.requireNonNull(controller, "controller");
        if (!controller.getClass().isAnnotationPresent(WebSocketController.class)) {
            throw new IllegalArgumentException(
                "WebSocket controller must be annotated with @WebSocketController"
            );
        }
        for (Method method : controller.getClass().getDeclaredMethods()) {
            WebSocketRoute route = method.getAnnotation(WebSocketRoute.class);
            if (route == null) {
                continue;
            }
            validateRoute(controller, method, route.value());
            RouteHandler previous = routes.putIfAbsent(
                route.value(),
                new RouteHandler(controller, method)
            );
            if (previous != null) {
                throw new IllegalStateException("Duplicate WebSocket route: " + route.value());
            }
        }
    }

    public SocketMessage dispatch(SocketMessage request) {
        Objects.requireNonNull(request, "request");
        RouteHandler handler = routes.get(request.route());
        if (handler == null) {
            throw new IllegalArgumentException("Unknown WebSocket route: " + request.route());
        }

        try {
            SocketMessage response =
                (SocketMessage) handler.method().invoke(handler.controller(), request);
            if (response == null) {
                throw new IllegalStateException(
                    "WebSocket route returned null: " + request.route()
                );
            }
            return response;
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(
                "WebSocket route failed: " + request.route(),
                e.getCause()
            );
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot invoke WebSocket route: " + request.route(), e);
        }
    }

    private static void validateRoute(Object controller, Method method, String path) {
        if (path == null || !path.startsWith("/") || path.startsWith("//")) {
            throw new IllegalArgumentException(
                "WebSocket route must be an absolute path: " + path
            );
        }
        if (!Modifier.isPublic(method.getModifiers())
                || method.getParameterCount() != 1
                || method.getParameterTypes()[0] != SocketMessage.class
                || method.getReturnType() != SocketMessage.class) {
            throw new IllegalArgumentException(
                "Route " + path + " on " + controller.getClass().getName()
                    + " must be public and accept/return SocketMessage"
            );
        }
    }

    private record RouteHandler(Object controller, Method method) {
    }
}
