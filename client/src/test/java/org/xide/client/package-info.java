/**
 * Integration tests of the client module: the Docker Compose stack (MySQL + Java server +
 * Nginx) is started by the test itself and the real client connects through the Nginx
 * proxy (see {@link org.xide.client.ClientServerConnectionTest}).
 *
 * <p>
 * Make scope: {@code test-integration} (enable with {@code -Dsocket.it=true}).
 */
package org.xide.client;
