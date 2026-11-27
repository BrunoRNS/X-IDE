package org.xide.sockets.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as a WebSocket controller scanned by
 * {@link org.xide.sockets.annotations.WebSocketScanner}; route methods are declared with
 * {@link WebSocketRoute}. Controllers need a public no-argument constructor.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface WebSocketController {

}
