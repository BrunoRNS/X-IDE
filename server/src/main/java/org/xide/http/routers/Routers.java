package org.xide.http.routers;

import io.javalin.config.JavalinConfig;
import org.xide.http.controllers.HelloController;

public class Routers {
    public static void register(JavalinConfig config) {
        config.routes.get("/", HelloController::index);
    }
}
