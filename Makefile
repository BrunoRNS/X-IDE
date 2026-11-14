.DEFAULT_GOAL := help

MVN_ARGS ?=
COMPOSE_ARGS ?=
COMPOSE := docker compose --env-file .env -f infra/compose.yaml

CLIENT_JAR := client/target/client-1.0-SNAPSHOT-all.jar
CLIENT_ENDPOINT ?= wss://localhost:8443/ws/socket
CLIENT_ROUTE ?= /develop/ping
CLIENT_MESSAGE ?= ping
CLIENT_TRUST_CERT ?= infra/nginx/certs/server.crt

.PHONY: all build-client build-server build-common build-integration build-all package-all
.PHONY: test-client test-server test-common test-integration test-all
.PHONY: run-server stop-server server-logs stack-status gen-certs
.PHONY: run-client run-clients communication-test communication-test-windows remote-test
.PHONY: configure help exit quit

help:
	@printf '%s\n' \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"███████████████████████████████▓▓▓▓▓▓▓▓▒▒▓██████████████████████████████████████████████████████████" \
		"█████████████████████████████▓▒▒▓▓▓▓▓▓▓▓▒▒▓█████████████████████████████████████████████████████████" \
		"████████████████████████████▓▒▒▓▓▓▓▓▓▓▓▓▓▓▓▓████████████████████████████████████████████████████████" \
		"████████████████████████████▓▓▓▒░░░▓▓▓▒░░░▓█████████████████████████████████████████████████████████" \
		"███████████████████████████▓▓▓▓▓▓▒░░░░░░▓███████▓░░▓█████████░░░████████████████████████████████████" \
		"████████████████████████████▓▓▓▓██▓░░░▒▓█████████▒▒▓██▓▒░░░░▓░░░███▒░░░░▒███████████████████████████" \
		"████████████████████████████████▓▒░░░░░░▓███████▓░░▓▓░░░▓██▒░░░░█░░░▓██▓░░░█████████████████████████" \
		"███████████████████████████████▒░░░▓██▒░░░██████▓░░▒▒░▓██████░░░▒░░░░░░░░░░▓████████████████████████" \
		"████████████████████████████████████████████████▓░░▓▒░░██████░░▒▓░░▓████████████████████████████████" \
		"████████████████████████████████████████████████▓░░▓█▒░░░░░░░░▓██▓░░░░░░░░▓█████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"████████████████████████████████████████████████████████████████████████████████████████████████████" \
		"X-IDE project commands" \
		"" \
		"Build scopes (see each module's src/main/java and src/test/java package-info.java):" \
		"  build-common | build-client | build-server | build-integration | build-all" \
		"  package-all" \
		"" \
		"JUnit scopes:" \
		"  test-common | test-client | test-server | test-integration | test-all" \
		"" \
		"Runtime:" \
		"  run-server                          Start MySQL + Java server + Nginx." \
		"                                      Runs gen-certs first; the edge is TLS 1.3" \
		"                                      only: WSS at /ws/socket (self-signed dev" \
		"                                      certificate in infra/nginx/certs/)." \
		"                                      --user/--password: not yet implemented." \
		"  gen-certs                           Generate the self-signed dev certificate" \
		"                                      on the host (Linux/macOS/Windows, via" \
		"                                      Docker) so the container receives it." \
		"  stop-server | server-logs | stack-status" \
		"  run-client                          Send CLIENT_MESSAGE to CLIENT_ROUTE at" \
		"                                      CLIENT_ENDPOINT and print the response." \
		"                                      CLIENT_TRUST_CERT is trusted for WSS." \
		"                                      --user/--password: not yet implemented." \
		"  communication-test                  Verify ping/pong in both directions through" \
		"                                      the running stack (bash script)." \
		"  communication-test-windows          Same check via Windows PowerShell." \
		"  run-clients --count N               not yet implemented" \
		"  remote-test                         Send a ping to URL=wss://host:8443/ws/socket." \
		"                                      REMOTE_TRUST_CERT is trusted for WSS; omit it" \
		"                                      for hosts with trusted certificates." \
		"                                      --user/--password: not yet implemented." \
		"" \
		"Project:" \
		"  configure | help | exit | quit" \
		"" \
		"Example local workflow:" \
		"  run-server                          # starts the stack (TLS 1.3 edge)" \
		"  communication-test                  # ping/pong both ways through the stack" \
		"  run-client                          # CLIENT_MESSAGE defaults to ping -> prints pong" \
		"  run-client CLIENT_ENDPOINT=wss://localhost:8443/ws/socket" \
		"  stop-server" \
		"" \
		"Optional Maven options: MVN_ARGS='-DskipTests'" \
		"Optional Compose options: COMPOSE_ARGS='--remove-orphans'"

all: build-all

build-common:
	./mvnw $(MVN_ARGS) -pl common compile

build-client:
	./mvnw $(MVN_ARGS) -pl client -am compile

build-server:
	./mvnw $(MVN_ARGS) -pl server -am compile

build-integration:
	./mvnw $(MVN_ARGS) -pl client -am test-compile

build-all:
	./mvnw $(MVN_ARGS) compile

package-all:
	./mvnw $(MVN_ARGS) package

test-client:
	./mvnw $(MVN_ARGS) -pl client -am test

test-server:
	./mvnw $(MVN_ARGS) -pl server -am test

test-common:
	./mvnw $(MVN_ARGS) -pl common test

test-integration:
	./mvnw $(MVN_ARGS) -pl client -am test -Dsocket.it=true

test-all:
	./mvnw $(MVN_ARGS) test

configure:
	@if [ -e .env ]; then \
		echo ".env already exists; leaving it unchanged."; \
	else \
		cp .env.example .env && \
		echo "Created .env from .env.example; replace the example database passwords before starting."; \
	fi

gen-certs:
	bash infra/gen-certs.sh

run-server: configure gen-certs
	$(COMPOSE) up --build $(COMPOSE_ARGS)

stop-server:
	$(COMPOSE) down $(COMPOSE_ARGS)

server-logs:
	$(COMPOSE) logs --follow $(COMPOSE_ARGS)

stack-status:
	$(COMPOSE) ps

run-client:
	./mvnw $(MVN_ARGS) -pl client -am package
	java -jar $(CLIENT_JAR) "$(CLIENT_ENDPOINT)" "$(CLIENT_ROUTE)" \
		--message "$(CLIENT_MESSAGE)" \
		$(if $(wildcard $(CLIENT_TRUST_CERT)),--trust-cert "$(CLIENT_TRUST_CERT)")

communication-test:
	./mvnw $(MVN_ARGS) -pl client -am package
	CLIENT_ENDPOINT="$(CLIENT_ENDPOINT)" CLIENT_TRUST_CERT="$(CLIENT_TRUST_CERT)" \
		bash infra/communication-test.sh

communication-test-windows:
	./mvnw $(MVN_ARGS) -pl client -am package
	CLIENT_ENDPOINT="$(CLIENT_ENDPOINT)" CLIENT_TRUST_CERT="$(CLIENT_TRUST_CERT)" \
		powershell -NoProfile -ExecutionPolicy Bypass -File infra/communication-test.ps1

run-clients:
	@echo "run-clients: not yet implemented"
	@exit 1

remote-test:
	@test -n "$(URL)" || { \
		echo "remote-test: URL is required, e.g. make remote-test URL=wss://host:8443/ws/socket"; exit 1; }
	./mvnw $(MVN_ARGS) -pl client -am package
	java -jar $(CLIENT_JAR) "$(URL)" "$(CLIENT_ROUTE)" \
		--message "$(CLIENT_MESSAGE)" \
		$(if $(REMOTE_TRUST_CERT),--trust-cert "$(REMOTE_TRUST_CERT)")

exit quit:
	@echo "bye"
