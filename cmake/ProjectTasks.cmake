find_package(Java 17 REQUIRED COMPONENTS Runtime Development)
find_program(MAVEN_EXECUTABLE mvn REQUIRED)
find_program(DOCKER_EXECUTABLE docker)
find_program(PODMAN_EXECUTABLE podman)
find_program(PODMAN_COMPOSE_EXECUTABLE podman-compose)

set(ROOT "${CMAKE_SOURCE_DIR}")
set(XIDE_CLIENT_URL "wss://localhost:8443/ws" CACHE STRING "WSS endpoint used by client probes")
set(XIDE_CLIENT_USER "default" CACHE STRING "Default X-IDE login used by CMake client targets")
set(XIDE_CLIENT_PASSWORD "" CACHE STRING "Password for XIDE_CLIENT_USER (prefer xidew run-client to avoid storing it in CMake cache)")
set(XIDE_CLIENT_MESSAGE "hello from X-IDE" CACHE STRING "Message sent by run-client")
set(XIDE_CLIENT_COUNT "5" CACHE STRING "Number of concurrent client sessions")
set(XIDE_CLIENT_TRUST_CERT "${ROOT}/deploy/certs/server.crt" CACHE FILEPATH "Optional PEM certificate for the local self-signed TLS certificate")
set(XIDE_CLIENT_TRUST_ARGS "")
if(NOT XIDE_CLIENT_TRUST_CERT STREQUAL "")
  list(APPEND XIDE_CLIENT_TRUST_ARGS --trust-cert "${XIDE_CLIENT_TRUST_CERT}")
endif()

set(XIDE_COMPOSE_COMMAND "")
if(XIDE_CONTAINER_ENGINE STREQUAL "docker" OR XIDE_CONTAINER_ENGINE STREQUAL "auto")
  if(DOCKER_EXECUTABLE)
    execute_process(COMMAND "${DOCKER_EXECUTABLE}" compose version RESULT_VARIABLE DCR OUTPUT_QUIET ERROR_QUIET)
    if(DCR EQUAL 0)
      set(XIDE_COMPOSE_COMMAND "${DOCKER_EXECUTABLE};compose")
    endif()
  endif()
endif()
if(NOT XIDE_COMPOSE_COMMAND AND (XIDE_CONTAINER_ENGINE STREQUAL "podman" OR XIDE_CONTAINER_ENGINE STREQUAL "auto"))
  if(PODMAN_EXECUTABLE)
    execute_process(COMMAND "${PODMAN_EXECUTABLE}" compose version RESULT_VARIABLE PCR OUTPUT_QUIET ERROR_QUIET)
    if(PCR EQUAL 0)
      set(XIDE_COMPOSE_COMMAND "${PODMAN_EXECUTABLE};compose")
    elseif(PODMAN_COMPOSE_EXECUTABLE)
      set(XIDE_COMPOSE_COMMAND "${PODMAN_COMPOSE_EXECUTABLE}")
    endif()
  endif()
endif()
if(XIDE_CONTAINER_ENGINE STREQUAL "docker" AND NOT XIDE_COMPOSE_COMMAND)
  message(FATAL_ERROR "Docker Compose v2 was requested but is unavailable.")
elseif(XIDE_CONTAINER_ENGINE STREQUAL "podman" AND NOT XIDE_COMPOSE_COMMAND)
  message(FATAL_ERROR "Podman Compose is unavailable.")
elseif(NOT XIDE_CONTAINER_ENGINE MATCHES "^(auto|docker|podman)$")
  message(FATAL_ERROR "XIDE_CONTAINER_ENGINE must be auto, docker, or podman.")
endif()
message(STATUS "X-IDE Compose command: ${XIDE_COMPOSE_COMMAND}")

# Build scopes map one-to-one to Maven modules and their Java package-info.java documentation.
add_custom_target(build-common COMMAND "${MAVEN_EXECUTABLE}" -B -pl common -am package -DskipTests WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
add_custom_target(build-client COMMAND "${MAVEN_EXECUTABLE}" -B -pl client -am package -DskipTests WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
add_custom_target(build-server COMMAND "${MAVEN_EXECUTABLE}" -B -pl server -am package -DskipTests WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
add_custom_target(build-integration COMMAND "${MAVEN_EXECUTABLE}" -B -pl integration -am package -DskipTests WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
add_custom_target(build-all COMMAND "${MAVEN_EXECUTABLE}" -B package -DskipTests WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
add_custom_target(package-all COMMAND "${MAVEN_EXECUTABLE}" -B package WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)

add_custom_target(test-common COMMAND "${MAVEN_EXECUTABLE}" -B -pl common -am test -Dtest=ProtocolMessageTest -Dsurefire.failIfNoSpecifiedTests=false WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
add_custom_target(test-client COMMAND "${MAVEN_EXECUTABLE}" -B -pl client -am test -Dtest=ClientUriTest -Dsurefire.failIfNoSpecifiedTests=false WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
add_custom_target(test-server COMMAND "${MAVEN_EXECUTABLE}" -B -pl server -am test -Dtest=MessageServiceTest,PasswordHasherTest -Dsurefire.failIfNoSpecifiedTests=false WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
add_custom_target(test-all COMMAND "${MAVEN_EXECUTABLE}" -B test WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)

add_custom_target(run-server COMMAND "${CMAKE_COMMAND}" -E env "XIDE_CONTAINER_ENGINE=${XIDE_CONTAINER_ENGINE}" "${ROOT}/scripts/run-server.sh" DEPENDS build-server WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
if(XIDE_COMPOSE_COMMAND)
  add_custom_target(stack-down COMMAND ${XIDE_COMPOSE_COMMAND} --env-file "${ROOT}/deploy/.env" -f "${ROOT}/deploy/compose.yaml" down WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
  add_custom_target(stack-logs COMMAND ${XIDE_COMPOSE_COMMAND} --env-file "${ROOT}/deploy/.env" -f "${ROOT}/deploy/compose.yaml" logs -f --tail=100 WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
  add_custom_target(stack-status COMMAND ${XIDE_COMPOSE_COMMAND} --env-file "${ROOT}/deploy/.env" -f "${ROOT}/deploy/compose.yaml" ps WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
else()
  foreach(T stack-down stack-logs stack-status)
    add_custom_target(${T} COMMAND "${CMAKE_COMMAND}" -E echo "Install Docker Compose v2 or Podman Compose." COMMAND "${CMAKE_COMMAND}" -E false)
  endforeach()
endif()

if(XIDE_CLIENT_PASSWORD STREQUAL "")
  add_custom_target(run-client COMMAND "${CMAKE_COMMAND}" -E echo "Set -DXIDE_CLIENT_PASSWORD=... or use ./xidew.sh run-client --user USER --password PASSWORD" COMMAND "${CMAKE_COMMAND}" -E false)
  add_custom_target(run-clients COMMAND "${CMAKE_COMMAND}" -E echo "Set -DXIDE_CLIENT_PASSWORD=... or use ./xidew.sh run-clients --user USER --password PASSWORD" COMMAND "${CMAKE_COMMAND}" -E false)
else()
  add_custom_target(run-client
    COMMAND "${MAVEN_EXECUTABLE}" -B -pl client -am package -DskipTests
    COMMAND "${Java_JAVA_EXECUTABLE}" -jar "${ROOT}/client/target/x-ide-client.jar" --url "${XIDE_CLIENT_URL}" --user "${XIDE_CLIENT_USER}" --password "${XIDE_CLIENT_PASSWORD}" --message "${XIDE_CLIENT_MESSAGE}" ${XIDE_CLIENT_TRUST_ARGS}
    WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
  add_custom_target(run-clients
    COMMAND "${MAVEN_EXECUTABLE}" -B -pl integration -am package -DskipTests
    COMMAND "${Java_JAVA_EXECUTABLE}" -jar "${ROOT}/integration/target/x-ide-concurrent-probe.jar" --url "${XIDE_CLIENT_URL}" --user "${XIDE_CLIENT_USER}" --password "${XIDE_CLIENT_PASSWORD}" ${XIDE_CLIENT_TRUST_ARGS} --count "${XIDE_CLIENT_COUNT}"
    WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
endif()
add_custom_target(communication-test
  COMMAND "${MAVEN_EXECUTABLE}" -B -pl server,integration -am package -DskipTests
  COMMAND "${CMAKE_COMMAND}" -E env "XIDE_CONTAINER_ENGINE=${XIDE_CONTAINER_ENGINE}" "${ROOT}/scripts/communication-test.sh"
  WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
if(XIDE_CLIENT_PASSWORD STREQUAL "")
  add_custom_target(remote-communication-test
    COMMAND "${CMAKE_COMMAND}" -E echo "Set -DXIDE_CLIENT_PASSWORD=... or use ./xidew.sh remote-test --url URL --user USER --password PASSWORD"
    COMMAND "${CMAKE_COMMAND}" -E false)
else()
  add_custom_target(remote-communication-test
    COMMAND "${MAVEN_EXECUTABLE}" -B -pl integration -am package -DskipTests
    COMMAND "${Java_JAVA_EXECUTABLE}" -jar "${ROOT}/integration/target/x-ide-communication-probe.jar" --url "${XIDE_CLIENT_URL}" --user "${XIDE_CLIENT_USER}" --password "${XIDE_CLIENT_PASSWORD}" ${XIDE_CLIENT_TRUST_ARGS}
    WORKING_DIRECTORY "${ROOT}" USES_TERMINAL)
endif()
