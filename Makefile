.DEFAULT_GOAL := help

MVN_ARGS ?=
COMPOSE_ARGS ?=
COMPOSE := docker compose --env-file .env -f infra/compose.yaml
COMPOSE_DEV := $(COMPOSE) -f infra/compose.dev.yaml

CLIENT_JAR := client/target/client-1.0-SNAPSHOT-all.jar
CLIENT_ENDPOINT ?= wss://localhost:8443/ws/socket
CLIENT_ROUTE ?= /develop/ping
CLIENT_MESSAGE ?= ping
CLIENT_DEV_MODE ?= true
MODEL_NAME ?= qwen2.5-coder:3b
RUN_SERVER_BUILD ?= 1

# Windows support: GNU make on Windows runs recipes with cmd.exe unless SHELL
# points to a POSIX shell (Git Bash/MSYS2). It also defaults SHELL to "sh.exe"
# even when no sh.exe exists, so probe the shell that actually runs: cmd.exe
# does not expand $0 and prints it literally, while a POSIX shell prints its
# own name. Both shells are supported: the recipes use the syntax of the shell
# that actually runs them, and the Maven wrapper is selected accordingly
# (mvnw.cmd for cmd.exe, ./mvnw for POSIX shells).
ifeq ($(OS),Windows_NT)
ifeq ($(basename $(notdir $(SHELL))),cmd)
POSIX_SHELL :=
else
SHELL_PROBE := $(shell echo $$0)
ifneq ($(strip $(SHELL_PROBE)),$$0)
POSIX_SHELL := 1
endif
endif
else
POSIX_SHELL := 1
endif

ifeq ($(POSIX_SHELL),1)
MVNW := ./mvnw
EXIT_FAIL := exit 1
NOOP := :
else
MVNW := mvnw.cmd
EXIT_FAIL := exit /b 1
NOOP := rem
endif

# Auto-detected LAN IP: source address of the default route, used as TLS_HOST when
# neither the shell environment nor .env defines one. Fallback: hostname -I on
# POSIX shells and Find-NetRoute on Windows cmd.exe.
ifeq ($(POSIX_SHELL),1)
LAN_IP := $(shell ip -4 route get 1.1.1.1 2>/dev/null | sed -n 's/.*src \([0-9.]*\).*/\1/p' | tr -d '\r' | head -n 1)
ifeq ($(LAN_IP),)
LAN_IP := $(shell hostname -I 2>/dev/null | awk '{print $$1}')
endif
ifeq ($(LAN_IP),)
LAN_IP := $(shell powershell -NoProfile -Command "[Console]::Out.Write((Find-NetRoute -RemoteIPAddress 1.1.1.1 | Select-Object -First 1).IPAddress)" 2>/dev/null)
endif
else
LAN_IP := $(shell powershell -NoProfile -Command "[Console]::Out.Write((Find-NetRoute -RemoteIPAddress 1.1.1.1 | Select-Object -First 1).IPAddress)")
endif
LAN_IP := $(strip $(LAN_IP))

# Effective TLS_HOST: shell environment first, then .env, then the auto-detected
# LAN IP. TLS_HOST_AUTO=1 lets the container entrypoint regenerate the self-signed
# certificate when the auto-detected IP is not covered by the persistent one.
ifeq ($(POSIX_SHELL),1)
TLS_HOST_VALUE := $(if $(TLS_HOST),$(TLS_HOST),$(strip $(shell sed -n 's/^TLS_HOST=//p' .env 2>/dev/null | tail -n 1 | tr -d '\r')))
else
TLS_HOST_VALUE := $(if $(TLS_HOST),$(TLS_HOST),$(strip $(shell powershell -NoProfile -Command "if (Test-Path .env) { $$v = Get-Content .env | Where-Object { $$_ -match '^TLS_HOST=' } | Select-Object -Last 1; if ($$v) { [Console]::Out.Write((($$v -replace '^TLS_HOST=','').Trim())) } }")))
endif
ifeq ($(strip $(TLS_HOST_VALUE)),)
TLS_HOST_VALUE := $(LAN_IP)
TLS_HOST_AUTO := 1
endif

.PHONY: all build-client build-server build-server-image build-common build-integration build-all package-all package-server
.PHONY: test-client test-server test-common test-integration test-all
.PHONY: run-server run-dev-server stop-server server-logs stack-status
.PHONY: run-client run-clients communication-test communication-test-windows remote-test run-dev-client
.PHONY: run-model
.PHONY: format lint
.PHONY: configure help exit quit

define HELP_TEXT
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
███████████████████████████████▓▓▓▓▓▓▓▓▒▒▓██████████████████████████████████████████████████████████
█████████████████████████████▓▒▒▓▓▓▓▓▓▓▓▒▒▓█████████████████████████████████████████████████████████
████████████████████████████▓▒▒▓▓▓▓▓▓▓▓▓▓▓▓▓████████████████████████████████████████████████████████
████████████████████████████▓▓▓▒░░░▓▓▓▒░░░▓█████████████████████████████████████████████████████████
███████████████████████████▓▓▓▓▓▓▒░░░░░░▓███████▓░░▓█████████░░░████████████████████████████████████
████████████████████████████▓▓▓▓██▓░░░▒▓█████████▒▒▓██▓▒░░░░▓░░░███▒░░░░▒███████████████████████████
████████████████████████████████▓▒░░░░░░▓███████▓░░▓▓░░░▓██▒░░░░█░░░▓██▓░░░█████████████████████████
███████████████████████████████▒░░░▓██▒░░░██████▓░░▒▒░▓██████░░░▒░░░░░░░░░░▓████████████████████████
████████████████████████████████████████████████▓░░▓▒░░██████░░▒▓░░▓████████████████████████████████
████████████████████████████████████████████████▓░░▓█▒░░░░░░░░▓██▓░░░░░░░░▓█████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
████████████████████████████████████████████████████████████████████████████████████████████████████
X-IDE project commands

Build scopes (see each module's src/main/java and src/test/java package-info.java):
  build-common | build-client | build-server | build-integration | build-all
  package-server | package-all

JUnit scopes:
  test-common | test-client | test-server | test-integration | test-all

Code style (Spring Java Format, configured in pom.xml and .springjavaformatconfig):
  format                              Rewrite all Java sources in the style.
  lint                                Fail when a source is not formatted.

Runtime:
  build-server-image                  Build the Docker image used by run-server.
  run-server                          Start MySQL + Java server + Nginx.
                                      Generates a persistent self-signed TLS
                                      certificate. TLS_HOST is auto-detected
                                      from the LAN IP when unset; keep
                                      BIND_ADDRESS=0.0.0.0 so student clients
                                      on the LAN can connect. The server prints
                                      the student WSS URL and fingerprint.
                                      --user/--password: not yet implemented.
                                      RUN_SERVER_BUILD=0 uses a preloaded image.
  stop-server | server-logs | stack-status
  run-dev-server                      Like run-server, but bind-mounts
                                      server/src/main/resources into the app
                                      container with the Thymeleaf cache off:
                                      edit templates/*.html or public/* and
                                      just refresh the browser (no rebuild).
  run-model                           Start Ollama and download MODEL_NAME.
                                      Default: qwen2.5-coder:3b (CPU).
                                      Use MODEL_NAME=qwen2.5-coder:1.5b for faster inference.
  run-client                          Open the client window to enter the server IP,
                                      then send CLIENT_MESSAGE to CLIENT_ROUTE.
                                      First WSS connection verifies a server fingerprint.
                                      XIDE_TLS_FINGERPRINT supports automation.
                                      --user/--password: not yet implemented.
  communication-test                  Verify ping/pong in both directions through
                                      the running stack (bash script).
  communication-test-windows          Same check via Windows PowerShell.
  run-clients --count N               not yet implemented
  remote-test                         Send a ping to URL=wss://host:8443/ws/socket.
                                      Verify the server fingerprint on first contact;
                                      XIDE_TLS_FINGERPRINT supports automation.
                                      --user/--password: not yet implemented.

Project:
  configure | help | exit | quit

Example local workflow:
  run-server                          # starts the stack
  communication-test                  # ping/pong both ways through the stack
  run-client                          # opens the server connection window
  run-client CLIENT_ROUTE=/develop/ping
  stop-server

Optional Maven options: MVN_ARGS='-DskipTests'
Optional Compose options: COMPOSE_ARGS='--remove-orphans'
endef

help:
	$(info $(HELP_TEXT))
	@$(NOOP)

all: build-all

build-common:
	$(MVNW) $(MVN_ARGS) -pl common compile

build-client:
	$(MVNW) $(MVN_ARGS) -pl client -am compile

build-server:
	$(MVNW) $(MVN_ARGS) -pl server -am compile

build-integration:
	$(MVNW) $(MVN_ARGS) -pl client -am test-compile

build-all:
	$(MVNW) $(MVN_ARGS) compile

package-all:
	$(MVNW) $(MVN_ARGS) package

package-server:
	$(MVNW) $(MVN_ARGS) -B -ntp -U -pl server -am -DskipTests package

test-client:
	$(MVNW) $(MVN_ARGS) -pl client -am test

test-server:
	$(MVNW) $(MVN_ARGS) -pl server -am test

test-common:
	$(MVNW) $(MVN_ARGS) -pl common test

test-integration:
	$(MVNW) $(MVN_ARGS) -pl client -am test -Dsocket.it=true

test-all:
	$(MVNW) $(MVN_ARGS) test

format:
	$(MVNW) $(MVN_ARGS) spring-javaformat:apply

lint:
	$(MVNW) $(MVN_ARGS) spring-javaformat:validate

ifeq ($(POSIX_SHELL),1)
configure:
	@if [ -e .env ]; then \
		echo ".env already exists; leaving it unchanged."; \
	else \
		cp .env.example .env && \
		echo "Created .env from .env.example; replace the example database passwords before starting."; \
	fi
else
configure:
	@if exist .env (echo .env already exists; leaving it unchanged.) else (copy /Y .env.example .env >NUL && echo Created .env from .env.example; replace the example database passwords before starting.)
endif


build-server-image: configure package-server
	$(COMPOSE) build --no-cache app

run-server: configure

ifeq ($(RUN_SERVER_BUILD),1)
run-server: package-server
endif
	$(if $(TLS_HOST_AUTO),$(info TLS_HOST not set; auto-detected LAN IP: $(LAN_IP)),$(if $(TLS_HOST_VALUE),,$(info No LAN IP detected and no TLS_HOST set; the stack will fall back to localhost.)))
ifeq ($(RUN_SERVER_BUILD),1)
	$(COMPOSE) build --no-cache app
endif
ifeq ($(POSIX_SHELL),1)
	TLS_HOST='$(TLS_HOST_VALUE)' TLS_HOST_AUTO='$(TLS_HOST_AUTO)' $(COMPOSE) up --force-recreate $(if $(filter 0,$(RUN_SERVER_BUILD)),--no-build,) $(COMPOSE_ARGS) app
else
	set "TLS_HOST=$(TLS_HOST_VALUE)" && set "TLS_HOST_AUTO=$(TLS_HOST_AUTO)" && $(COMPOSE) up --force-recreate $(if $(filter 0,$(RUN_SERVER_BUILD)),--no-build,) $(COMPOSE_ARGS) app
endif

# Dev loop: same stack as run-server, but the image is built with cache and the
# server resources directory is bind-mounted (infra/compose.dev.yaml). HTML and
# static asset edits are picked up on browser refresh; restart is only needed
# for Java source changes (rebuild with: make package-server && docker compose
# --env-file .env -f infra/compose.yaml -f infra/compose.dev.yaml build app).
run-dev-server: configure package-server
	$(if $(TLS_HOST_AUTO),$(info TLS_HOST not set; auto-detected LAN IP: $(LAN_IP)),$(if $(TLS_HOST_VALUE),,$(info No LAN IP detected and no TLS_HOST set; the stack will fall back to localhost.)))
	$(COMPOSE) build app
	$(info Dev mode: server/src/main/resources is bind-mounted; edit templates/*.html or public/* and refresh the browser.)
ifeq ($(POSIX_SHELL),1)
	TLS_HOST='$(TLS_HOST_VALUE)' TLS_HOST_AUTO='$(TLS_HOST_AUTO)' $(COMPOSE_DEV) up --force-recreate $(COMPOSE_ARGS) app
else
	set "TLS_HOST=$(TLS_HOST_VALUE)" && set "TLS_HOST_AUTO=$(TLS_HOST_AUTO)" && $(COMPOSE_DEV) up --force-recreate $(COMPOSE_ARGS) app
endif

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
	$(MVNW) $(MVN_ARGS) -pl client -am package
	java -jar $(CLIENT_JAR) "$(CLIENT_ROUTE)" --message "$(CLIENT_MESSAGE)"

run-dev-client:
	$(MVNW) $(MVN_ARGS) -DskipTests -pl client -am package
	java -Dxide.dev.mode=$(CLIENT_DEV_MODE) -jar $(CLIENT_JAR) "$(CLIENT_ROUTE)" --message "$(CLIENT_MESSAGE)"
communication-test:
	$(MVNW) $(MVN_ARGS) -pl client -am package
ifeq ($(POSIX_SHELL),1)
	CLIENT_ENDPOINT="$(CLIENT_ENDPOINT)" bash infra/communication-test.sh
else
	set "CLIENT_ENDPOINT=$(CLIENT_ENDPOINT)" && bash infra/communication-test.sh
endif

communication-test-windows:
	$(MVNW) $(MVN_ARGS) -pl client -am package
ifeq ($(POSIX_SHELL),1)
	CLIENT_ENDPOINT="$(CLIENT_ENDPOINT)" powershell -NoProfile -ExecutionPolicy Bypass -File infra/communication-test.ps1
else
	set "CLIENT_ENDPOINT=$(CLIENT_ENDPOINT)" && powershell -NoProfile -ExecutionPolicy Bypass -File infra/communication-test.ps1
endif

run-clients:
	@echo run-clients: not yet implemented
	@$(EXIT_FAIL)

remote-test:
ifeq ($(strip $(URL)),)
	@echo remote-test: URL is required, e.g. make remote-test URL=wss://host:8443/ws/socket
	@$(EXIT_FAIL)
else
	$(MVNW) $(MVN_ARGS) -pl client -am package
	java -jar $(CLIENT_JAR) --endpoint "$(URL)" "$(CLIENT_ROUTE)" --message "$(CLIENT_MESSAGE)"
endif

exit quit:
	@echo bye
