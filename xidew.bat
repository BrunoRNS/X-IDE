@echo off
setlocal EnableExtensions
set "ROOT=%~dp0"
set "BUILD_DIR=%ROOT%build"
if not exist "%BUILD_DIR%\CMakeCache.txt" cmake -S "%ROOT%." -B "%BUILD_DIR%"
if errorlevel 1 exit /b %errorlevel%
if not "%~1"=="" goto dispatch_args

echo X-IDE project manager (Windows)
echo Type help to list commands. Type exit to quit.
:prompt
set "LINE="
set /p "LINE=xide^> "
if not defined LINE goto prompt
call :dispatch_line %LINE%
if errorlevel 99 goto end
if errorlevel 1 echo Command failed. Use help for available commands.
goto prompt

:dispatch_args
call :dispatch_line %*
exit /b %errorlevel%

:dispatch_line
set "LINE=%*"
set "CMD="
set "ARGS="
for /f "tokens=1,*" %%A in ("%LINE%") do (set "CMD=%%A" & set "ARGS=%%B")
if /i "%CMD%"=="help" goto help
if /i "%CMD%"=="configure" goto configure
if /i "%CMD%"=="build-common" goto build_common
if /i "%CMD%"=="build-client" goto build_client
if /i "%CMD%"=="build-server" goto build_server
if /i "%CMD%"=="build-integration" goto build_integration
if /i "%CMD%"=="build-all" goto build_all
if /i "%CMD%"=="package" goto package_all
if /i "%CMD%"=="package-all" goto package_all
if /i "%CMD%"=="test-common" goto test_common
if /i "%CMD%"=="test-client" goto test_client
if /i "%CMD%"=="test-server" goto test_server
if /i "%CMD%"=="test-all" goto test_all
if /i "%CMD%"=="run-server" goto run_server
if /i "%CMD%"=="stop-server" goto stack_down
if /i "%CMD%"=="server-logs" goto stack_logs
if /i "%CMD%"=="stack-status" goto stack_status
if /i "%CMD%"=="run-client" goto run_client
if /i "%CMD%"=="run-clients" goto run_clients
if /i "%CMD%"=="exit" exit /b 99
if /i "%CMD%"=="quit" exit /b 99
echo Unknown command: %CMD%
echo Use help.
exit /b 2

:configure
cmake -S "%ROOT%." -B "%BUILD_DIR%"
exit /b %errorlevel%
:build_common
cmake --build "%BUILD_DIR%" --target build-common
exit /b %errorlevel%
:build_client
cmake --build "%BUILD_DIR%" --target build-client
exit /b %errorlevel%
:build_server
cmake --build "%BUILD_DIR%" --target build-server
exit /b %errorlevel%
:build_integration
cmake --build "%BUILD_DIR%" --target build-integration
exit /b %errorlevel%
:build_all
cmake --build "%BUILD_DIR%" --target build-all
exit /b %errorlevel%
:package_all
cmake --build "%BUILD_DIR%" --target package-all
exit /b %errorlevel%
:test_common
cmake --build "%BUILD_DIR%" --target test-common
exit /b %errorlevel%
:test_client
cmake --build "%BUILD_DIR%" --target test-client
exit /b %errorlevel%
:test_server
cmake --build "%BUILD_DIR%" --target test-server
exit /b %errorlevel%
:test_all
cmake --build "%BUILD_DIR%" --target test-all
exit /b %errorlevel%
:run_server
cmake --build "%BUILD_DIR%" --target build-server
if errorlevel 1 exit /b %errorlevel%
powershell -NoProfile -ExecutionPolicy Bypass -File "%ROOT%scripts\run-server.ps1" %ARGS%
exit /b %errorlevel%
:stack_down
cmake --build "%BUILD_DIR%" --target stack-down
exit /b %errorlevel%
:stack_logs
cmake --build "%BUILD_DIR%" --target stack-logs
exit /b %errorlevel%
:stack_status
cmake --build "%BUILD_DIR%" --target stack-status
exit /b %errorlevel%
:run_client
cmake --build "%BUILD_DIR%" --target build-client
if errorlevel 1 exit /b %errorlevel%
java -jar "%ROOT%client\target\x-ide-client.jar" %ARGS%
exit /b %errorlevel%
:run_clients
cmake --build "%BUILD_DIR%" --target build-integration
if errorlevel 1 exit /b %errorlevel%
java -jar "%ROOT%integration\target\x-ide-concurrent-probe.jar" %ARGS%
exit /b %errorlevel%
:help
echo Build: build-common, build-client, build-server, build-integration, build-all, package
 echo Tests: test-common, test-client, test-server, test-all
echo Runtime: run-server [--user USER --password PASSWORD], stop-server, server-logs, stack-status
echo Client: run-client --user USER --password PASSWORD [--url wss://host:8443/ws] [--trust-cert PEM]
echo Load: run-clients --user USER --password PASSWORD --count N [--url URL] [--trust-cert PEM]
echo Project: configure, help, exit
exit /b 0
:end
endlocal
