/**
 * Integration tests of the client module: the Docker Compose stack (MySQL + Java server +
 * Nginx) is started by the test itself and the real client connects through the Nginx
 * proxy (see {@link org.xide.client.ClientServerConnectionTest}).
 *
 * <p>
 * Make scope: {@code test-integration} (enable with {@code -Dsocket.it=true}).
 *
 * @author LuisOtavio13
 * @since v1.0.0
 */
package org.xide.client;
