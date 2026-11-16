.DEFAULT_GOAL := help

MVN_ARGS ?=
COMPOSE_ARGS ?=
COMPOSE := docker compose --env-file .env -f infra/compose.yaml
COMPOSE_DEV := $(COMPOSE) -f infra/compose.dev.yaml

CLIENT_JAR := client/target/client-1.0-SNAPSHOT-all.jar
CLIENT_ENDPOINT ?= wss://localhost:8443/ws/socket
CLIENT_ROUTE ?= /develop/ping
CLIENT_MESSAGE ?= ping
MODEL_NAME ?= qwen2.5-coder:3b

# Auto-detected LAN IP: source address of the default route, used as TLS_HOST when
# neither the shell environment nor .env defines one. Fallback: hostname -I.
LAN_IP := $(shell ip -4 route get 1.1.1.1 2>/dev/null | sed -n 's/.*src \([0-9.]*\).*/\1/p' | tr -d '\r' | head -n 1)
ifeq ($(LAN_IP),)
LAN_IP := $(shell hostname -I 2>/dev/null | awk '{print $$1}')
endif

# Effective TLS_HOST: shell environment first, then .env, then the auto-detected
# LAN IP. TLS_HOST_AUTO=1 lets the container entrypoint regenerate the self-signed
# certificate when the auto-detected IP is not covered by the persistent one.
TLS_HOST_VALUE := $(if $(TLS_HOST),$(TLS_HOST),$(strip $(shell sed -n 's/^TLS_HOST=//p' .env 2>/dev/null | tail -n 1 | tr -d '\r')))
ifeq ($(strip $(TLS_HOST_VALUE)),)
TLS_HOST_VALUE := $(LAN_IP)
TLS_HOST_AUTO := 1
endif

.PHONY: all build-client build-server build-common build-integration build-all package-all
.PHONY: test-client test-server test-common test-integration test-all
.PHONY: run-server run-dev-server stop-server server-logs stack-status
.PHONY: run-client run-clients communication-test communication-test-windows remote-test
.PHONY: run-model
.PHONY: format lint
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
		"Code style (Spotless, configured in pom.xml):" \
		"  format                              Rewrite all Java sources in the style." \
		"  lint                                Fail when a source is not formatted." \
		"" \
		"Runtime:" \
		"  run-server                          Start MySQL + Java server + Nginx." \
		"                                      Generates a persistent self-signed TLS" \
		"                                      certificate. TLS_HOST is auto-detected" \
		"                                      from the LAN IP when unset; keep" \
		"                                      BIND_ADDRESS=0.0.0.0 so student clients" \
		"                                      on the LAN can connect. The server prints" \
		"                                      the student WSS URL and fingerprint." \
		"                                      --user/--password: not yet implemented." \
		"  stop-server | server-logs | stack-status" \
		"  run-dev-server                      Like run-server, but bind-mounts" \
		"                                      server/src/main/resources into the app" \
		"                                      container with the Thymeleaf cache off:" \
		"                                      edit templates/*.html or public/* and" \
		"                                      just refresh the browser (no rebuild)." \
		"  run-model                           Start Ollama and download MODEL_NAME." \
		"                                      Default: qwen2.5-coder:3b (CPU)." \
		"                                      Use MODEL_NAME=qwen2.5-coder:1.5b for faster inference." \
		"  run-client                          Open the client window to enter the server IP," \
		"                                      then send CLIENT_MESSAGE to CLIENT_ROUTE." \
		"                                      First WSS connection verifies a server fingerprint." \
		"                                      XIDE_TLS_FINGERPRINT supports automation." \
		"                                      --user/--password: not yet implemented." \
		"  communication-test                  Verify ping/pong in both directions through" \
		"                                      the running stack (bash script)." \
		"  communication-test-windows          Same check via Windows PowerShell." \
		"  run-clients --count N               not yet implemented" \
		"  remote-test                         Send a ping to URL=wss://host:8443/ws/socket." \
		"                                      Verify the server fingerprint on first contact;" \
		"                                      XIDE_TLS_FINGERPRINT supports automation." \
		"                                      --user/--password: not yet implemented." \
		"" \
		"Project:" \
		"  configure | help | exit | quit" \
		"" \
		"Example local workflow:" \
		"  run-server                          # starts the stack " \
		"  communication-test                  # ping/pong both ways through the stack" \
		"  run-client                          # opens the server connection window" \
		"  run-client CLIENT_ROUTE=/develop/ping" \
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

format:
	./mvnw $(MVN_ARGS) spotless:apply

lint:
	./mvnw $(MVN_ARGS) spotless:check

configure:
	@if [ -e .env ]; then \
		echo ".env already exists; leaving it unchanged."; \
	else \
		cp .env.example .env && \
		echo "Created .env from .env.example; replace the example database passwords before starting."; \
	fi


run-server: configure
	@if [ -n "$(TLS_HOST_AUTO)" ]; then \
		echo "TLS_HOST not set; auto-detected LAN IP: $(LAN_IP)"; \
	elif [ -z "$(TLS_HOST_VALUE)" ]; then \
		echo "No LAN IP detected and no TLS_HOST set; the stack will fall back to localhost." >&2; \
	fi
	$(COMPOSE) build --no-cache app
	TLS_HOST='$(TLS_HOST_VALUE)' TLS_HOST_AUTO='$(TLS_HOST_AUTO)' \
		$(COMPOSE) up --force-recreate $(COMPOSE_ARGS) app

# Dev loop: same stack as run-server, but the image is built with cache and the
# server resources directory is bind-mounted (infra/compose.dev.yaml). HTML and
# static asset edits are picked up on browser refresh; restart is only needed
# for Java source changes (rebuild with: make build-server && docker compose
# --env-file .env -f infra/compose.yaml -f infra/compose.dev.yaml build app).
run-dev-server: configure
	@if [ -n "$(TLS_HOST_AUTO)" ]; then \
		echo "TLS_HOST not set; auto-detected LAN IP: $(LAN_IP)"; \
	elif [ -z "$(TLS_HOST_VALUE)" ]; then \
		echo "No LAN IP detected and no TLS_HOST set; the stack will fall back to localhost." >&2; \
	fi
	$(COMPOSE) build app
	@echo "Dev mode: server/src/main/resources is bind-mounted; edit templates/*.html or public/* and refresh the browser."
	TLS_HOST='$(TLS_HOST_VALUE)' TLS_HOST_AUTO='$(TLS_HOST_AUTO)' \
		$(COMPOSE_DEV) up --force-recreate $(COMPOSE_ARGS) app

run-model: configure
	$(COMPOSE) up -d --wait $(COMPOSE_ARGS) ollama
	$(COMPOSE) exec -T ollama ollama pull "$(MODEL_NAME)"

stop-server:
	$(COMPOSE) down $(COMPOSE_ARGS)

server-logs:
	$(COMPOSE) logs --follow $(COMPOSE_ARGS)

stack-status:
	$(COMPOSE) ps

run-client:
	./mvnw $(MVN_ARGS) -pl client -am package
	java -jar $(CLIENT_JAR) "$(CLIENT_ROUTE)" \
		--message "$(CLIENT_MESSAGE)"

communication-test:
	./mvnw $(MVN_ARGS) -pl client -am package
	CLIENT_ENDPOINT="$(CLIENT_ENDPOINT)" \
		bash infra/communication-test.sh

communication-test-windows:
	./mvnw $(MVN_ARGS) -pl client -am package
	CLIENT_ENDPOINT="$(CLIENT_ENDPOINT)" \
		powershell -NoProfile -ExecutionPolicy Bypass -File infra/communication-test.ps1

run-clients:
	@echo "run-clients: not yet implemented"
	@exit 1

remote-test:
	@test -n "$(URL)" || { \
		echo "remote-test: URL is required, e.g. make remote-test URL=wss://host:8443/ws/socket"; exit 1; }
	./mvnw $(MVN_ARGS) -pl client -am package
	java -jar $(CLIENT_JAR) --endpoint "$(URL)" "$(CLIENT_ROUTE)" \
		--message "$(CLIENT_MESSAGE)"

exit quit:
	@echo "bye"
