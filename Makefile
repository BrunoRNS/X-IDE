.ONESHELL:
.DEFAULT_GOAL := help

.PHONY: all build-common build-client build-server build-integration build-all package-all
.PHONY: test-common test-client test-server test-all run-server stop-server server-logs stack-status
.PHONY: run-client run-clients communication-test remote-test configure help exit quit

help:
	@printf '%s\n' \
		"X-IDE project manager (Linux/macOS)" \
		"" \
		"Build scopes (see each module's src/main/java and src/test/java package-info.java):" \
		"  build-common | build-client | build-server | build-integration | build-all" \
		"  package-all" \
		"" \
		"JUnit scopes:" \
		"  test-common | test-client | test-server | test-all" \
		"" \
		"Secure runtime:" \
		"  run-server [--user USER] [--password PASSWORD]" \
		"      Starts MySQL + Java server + Nginx TLS 1.3/WSS. If password is omitted," \
		"      asks for it without echo. Existing MySQL user is verified, not overwritten." \
		"  stop-server | server-logs | stack-status" \
		"  run-client --user USER --password PASSWORD [--url wss://HOST:8443/ws]" \
		"      Optional: --trust-cert PEM --message TEXT" \
		"  run-clients --user USER --password PASSWORD [--count N] [--url URL] [--trust-cert PEM]" \
		"  communication-test [--user USER] [--password PASSWORD]" \
		"  remote-test --url WSS_URL --user USER --password PASSWORD [--trust-cert PEM]" \
		"" \
		"Project:" \
		"  configure | help | exit | quit" \
		"" \
		"Example local workflow:" \
		"  run-server                         # prompts for the default account password" \
		"  run-client --user default --password 'your-password' --trust-cert deploy/certs/server.crt" \
		"  run-clients --user default --password 'your-password' --count 12 --trust-cert deploy/certs/server.crt" \
		"  stop-server" \
		"" \
		"The local test certificate is self-signed and valid for localhost only. For a" \
		"remote host, install/use a trusted TLS 1.3 certificate and omit --trust-cert."

all build-common build-client build-server build-integration build-all package-all \
test-common test-client test-server test-all run-server stop-server server-logs \
stack-status run-client run-clients communication-test remote-test configure exit quit:
	@echo "in developing..."
