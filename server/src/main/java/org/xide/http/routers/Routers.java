package org.xide.http.routers;

import org.xide.http.controllers.HelloController;

import io.javalin.config.JavalinConfig;


public class Routers {
    public static void register(JavalinConfig config) {
        config.routes.get("/", HelloController::index);
    }
}
