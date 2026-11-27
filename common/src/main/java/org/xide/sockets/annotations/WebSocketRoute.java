package org.xide.sockets.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a public method of a {@link WebSocketController} as a route handler.
 * {@code value} is the absolute route path (must start with {@code /});
 * {@code protectedRoute} requires a logged-in session on the server.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WebSocketRoute {

	String value();

	boolean protectedRoute() default false;

}
