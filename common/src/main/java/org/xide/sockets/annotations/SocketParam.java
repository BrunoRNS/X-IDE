package org.xide.sockets.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a route method parameter to a named field of the request payload. The field value
 * is converted to the declared parameter type; a missing field makes the request fail
 * with an error response.
 *
 * @since v1.0.0
 * @author LuisOtavio13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface SocketParam {

	String value();

}
