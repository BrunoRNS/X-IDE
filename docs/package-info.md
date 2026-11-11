# Java package documentation (`package-info.java`)

X-IDE uses Java's `package-info.java` convention (Javadoc package documentation),
not a separate `package-infos/` directory.

> Compatibility note: `package-info.java` predates Java 9 (it is a long-standing Java/Javadoc convention). Java 9 introduced `module-info.java` for the module system. This project targets Java 17 and uses `package-info.java` for package-level documentation.

The documentation follows the same independent scopes as the Maven modules and
CMake targets:

| Build scope | Production package documentation | Test package documentation |
|---|---|---|
| `common` | `common/src/main/java/org/xide/common/package-info.java` | `common/src/test/java/org/xide/common/package-info.java` |
| `client` | `client/src/main/java/org/xide/client/package-info.java` | `client/src/test/java/org/xide/client/package-info.java` |
| `server` | `server/src/main/java/org/xide/server/package-info.java` | `server/src/test/java/org/xide/server/package-info.java` |
| `integration` | `integration/src/main/java/org/xide/integration/package-info.java` | No JUnit test package: this module contains executable communication probes |

`src/main/java` documents the package's production API and responsibilities.
`src/test/java` documents the corresponding test scope and its isolation rules.
A test package-info file may use the same Java package name as production because
Maven compiles the two source roots into separate output locations.

Use the CMake targets `build-common`, `build-client`, `build-server`,
`build-integration`, `test-common`, `test-client`, and `test-server` to work on
one scope at a time. The matching package-info file is the first place to look
when changing that scope.
