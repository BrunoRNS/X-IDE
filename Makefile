.DEFAULT_GOAL := help

MVN_ARGS ?=
COMPOSE_ARGS ?=
COMPOSE := docker compose --env-file .env -f infra/compose.yaml

.PHONY: all build-client build-server build-common build-all package-all
.PHONY: test-client test-server test-common test-all
.PHONY: configure run-server stop-server server-logs stack-status run-client help

help:
	@printf '%s\n' \
		"X-IDE project commands" \
		"" \
		"Build and test:" \
		"  build-client | build-server | build-common | build-all | package-all" \
		"  test-client | test-server | test-common | test-all" \
		"  Optional Maven options: MVN_ARGS='-DskipTests'" \
		"" \
		"Compose runtime:" \
		"  configure                         # create .env from .env.example if absent" \
		"  run-server [COMPOSE_ARGS=...]    # e.g. COMPOSE_ARGS='--detach'" \
		"  stop-server | server-logs | stack-status" \
		"  run-client [CLIENT_ENDPOINT=ws://host:port/ws] [CLIENT_ROUTE=/develop/ping]" \
		"      Sends CLIENT_MESSAGE (default: ping) and prints the server response." \
		"  Optional Compose options: COMPOSE_ARGS='--remove-orphans'"

all: build-all

build-client:
	./mvnw $(MVN_ARGS) -pl client -am compile

build-server:
	./mvnw $(MVN_ARGS) -pl server -am compile

build-common:
	./mvnw $(MVN_ARGS) -pl common compile

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

test-all:
	./mvnw $(MVN_ARGS) test

configure:
	@if [ -e .env ]; then \
		echo ".env already exists; leaving it unchanged."; \
	else \
		cp .env.example .env && \
		echo "Created .env from .env.example; replace the example database passwords before starting."; \
	fi

run-server:
	$(COMPOSE) up --build $(COMPOSE_ARGS)

stop-server:
	$(COMPOSE) down $(COMPOSE_ARGS)

server-logs:
	$(COMPOSE) logs --follow $(COMPOSE_ARGS)

stack-status:
	$(COMPOSE) ps $(COMPOSE_ARGS)

CLIENT_ENDPOINT ?= ws://127.0.0.1:8082/ws/socket
CLIENT_ROUTE ?= /develop/ping
CLIENT_MESSAGE ?= ping

run-client:
	./mvnw $(MVN_ARGS) -pl client -am package
	java -jar client/target/client-1.0-SNAPSHOT-all.jar \
		"$(CLIENT_ENDPOINT)" "$(CLIENT_ROUTE)" "$(CLIENT_MESSAGE)"
