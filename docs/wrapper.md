# Interactive wrappers

## Linux/macOS

```sh
./xidew.sh
```

The shell accepts repeated commands and quoted arguments. Type `help` for the full list,
`exit` to leave. Examples:

```text
xide> build-server
xide> test-server
xide> run-server
xide> run-client --user default --password 'your-password'
xide> run-clients --user default --password 'your-password' --count 8
xide> stop-server
xide> exit
```

`run-server` prompts without echo if `--password` is omitted. The password may be supplied
as a runtime argument. `run-client` and `run-clients` require `--user` and `--password`.

## Windows

Run `xidew.bat` from Command Prompt. It provides the same build/test/runtime commands;
use `xidew.bat help` for usage. For localhost clients, pass
`--trust-cert deploy/certs/server.crt` explicitly. Java, Maven, CMake, OpenSSL and
Docker/Podman Compose are required for the relevant operations.
