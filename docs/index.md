# X-IDE Documentation

Welcome to the X-IDE documentation. This project is a Java 17 Maven multi-module platform for classroom IDE environments.

## Quick Navigation

| Section | Description |
|---------|-------------|
| [Getting Started](getting-started/index.md) | Quick setup and first run |
| [Architecture](architecture/overview.md) | System architecture and design |
| [Configuration](reference/configuration.md) | Environment variables and settings |
| [Commands](reference/commands.md) | All Makefile commands |
| [WebSocket Protocol](reference/websocket-protocol.md) | SocketMessage format and routes |
| [Development and Contributing](development.md) | Project patterns, local workflow, and pull requests |
| [Testing](development/testing.md) | Testing strategies and patterns |
| [Packaging](development/packaging.md) | Building release bundles |
| [Security](guides/security.md) | TLS, authentication, and limitations |
| [Troubleshooting](guides/troubleshooting.md) | Common issues and solutions |

## Project Overview

X-IDE is a classroom IDE platform consisting of:

- **Web UI** — Javalin + Thymeleaf served over HTTPS
- **WebSocket API** — Tyrus WebSocket server for real-time communication
- **Desktop Client** — Standalone Swing JAR with TLS pinning (TOFU)
- **Database** — MySQL 8.4 for persistent storage
- **AI Integration** — Optional Ollama for model-backed features
- **Reverse Proxy** — Nginx with TLS 1.3 termination

All components run in a single Docker/Podman Compose stack.

## Quick Links

- [Repository](https://github.com/BrunoRNS/X-IDE)
- [Issue Tracker](https://github.com/BrunoRNS/X-IDE/issues)
- [Changelog](CHANGELOG.md)
- [License](LICENSE)

## Contributing

See the [Development and Contribution Guide](development.md) for design patterns, local workflow, commit conventions, and pull request guidelines.