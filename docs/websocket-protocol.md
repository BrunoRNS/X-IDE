# X-IDE WebSocket Protocol

## Overview

The WebSocket protocol uses JSON frames over a single persistent connection.
Defined in `common/src/main/java/org/xide/protocol/SocketMessage.java`.

**Connection URL:** `wss://<host>:8443/ws/socket`

## Frame Structure

```json
{
  "type": "STRING",
  "payload": "OBJECT|STRING",
  "requestId": "STRING (optional)"
}
```

| Field | Required | Description |
|-------|----------|-------------|
| `type` | ✅ | Frame type (routes to `@OnMessage` handler) |
| `payload` | ✅ | Frame data (object for requests, `"pong"` for pong responses) |
| `requestId` | ❌ | Client-generated correlation ID (echoed in response) |

**Critical Rule:** A frame with `"payload": "pong"` is a **response** and is **never re-dispatched** to controllers. This implements the ping/pong heartbeat.

---

## Connection Lifecycle

```
┌─────────────┐     TCP + TLS      ┌─────────────┐
│   Client    │ ◄─────────────────► │   Server    │
└──────┬──────┘                     └──────┬──────┘
       │                                    │
       │  1. WebSocket Handshake            │
       │  (Upgrade, Sec-WebSocket-Key)      │
       ├───────────────────────────────────►│
       │                                    │
       │  2. AUTH frame (REQUIRED first)    │
       ├───────────────────────────────────►│
       │                                    │
       │  3. AUTH_RESULT                    │
       │◄───────────────────────────────────┤
       │                                    │
       │  4. Domain frames (bidirectional)  │
       ├───────────────────────────────────►│
       │◄───────────────────────────────────┤
       │                                    │
       │  5. Close (optional)               │
       ├───────────────────────────────────►│
```

**Authentication is mandatory.** Unauthenticated connections are closed after a timeout.

---

## Frame Types Reference

### System Frames

| Type | Direction | Description |
|------|-----------|-------------|
| `AUTH` | C→S | Authenticate (token or credentials) |
| `AUTH_RESULT` | S→C | Authentication result |
| `PING` | C→S | Heartbeat request |
| `PONG` | S→C | Heartbeat response (special: not re-dispatched) |
| `ERROR` | S→C | Protocol/application error |

### Develop Frames (Debug)

| Type | Direction | Description |
|------|-----------|-------------|
| `RUN_CODE` | C→S | Execute code snippet |
| `RUN_RESULT` | S→C | Execution output |

### Competition Frames

| Type | Direction | Description |
|------|-----------|-------------|
| `COMPETITION_STATE` | S→C | Full state sync (on join/start) |
| `SCOREBOARD_UPDATE` | S→C | Incremental scoreboard delta |
| `TIMER_TICK` | S→C | Remaining seconds (1Hz) |
| `BALLOON_AWARDED` | S→C | Problem solved → balloon color |

### Submission Frames

| Type | Direction | Description |
|------|-----------|-------------|
| `SUBMIT` | C→S | Submit solution for judging |
| `SUBMISSION_STATUS` | S→C | Status update (queued→compiling→running) |
| `SUBMISSION_RESULT` | S→C | Final verdict with test results |

---

## Detailed Frame Specifications

### AUTH (Client → Server)

**Token Authentication (preferred):**
```json
{
  "type": "AUTH",
  "payload": { "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." },
  "requestId": "auth-1"
}
```

**Credentials Authentication:**
```json
{
  "type": "AUTH",
  "payload": {
    "username": "student@example.com",
    "password": "plaintext-password"
  },
  "requestId": "auth-2"
}
```

**Response (AUTH_RESULT):**
```json
{
  "type": "AUTH_RESULT",
  "payload": {
    "success": true,
    "user": {
      "id": 123,
      "name": "John Doe",
      "email": "john@example.com",
      "role": "STUDENT"
    },
    "token": "eyJhbGciOiJIUzI1NiIs..."  // New token if credentials used
  },
  "requestId": "auth-1"
}
```

**Failure:**
```json
{
  "type": "AUTH_RESULT",
  "payload": {
    "success": false,
    "error": "Invalid token",
    "code": "AUTH_FAILED"
  },
  "requestId": "auth-1"
}
```

---

### PING / PONG (Heartbeat)

**Client → Server:**
```json
{ "type": "PING", "payload": {}, "requestId": "ping-1" }
```

**Server → Client (special handling):**
```json
{ "type": "PONG", "payload": "pong", "requestId": "ping-1" }
```

> **Note:** The string `"pong"` as payload marks this as a response frame. The router **never** dispatches it to `@OnMessage` handlers.

---

### RUN_CODE / RUN_RESULT (Develop Sandbox)

**Request:**
```json
{
  "type": "RUN_CODE",
  "payload": {
    "code": "print('hello')",
    "language": "PYTHON",
    "input": "test input"
  },
  "requestId": "run-123"
}
```

**Supported Languages:** `C`, `CPP`, `JAVA`, `PYTHON`, `JAVASCRIPT`, `GO`, `RUST`, `KOTLIN`

**Response:**
```json
{
  "type": "RUN_RESULT",
  "payload": {
    "stdout": "hello\n",
    "stderr": "",
    "exitCode": 0,
    "timeMs": 45,
    "memoryKb": 2048,
    "timedOut": false
  },
  "requestId": "run-123"
}
```

---

### COMPETITION_STATE (Server → Client)

Sent on: successful auth during active competition, competition start, explicit request.

```json
{
  "type": "COMPETITION_STATE",
  "payload": {
    "competition": {
      "id": 1,
      "name": "ICPC Regional 2024",
      "startTime": "2024-01-15T10:00:00Z",
      "endTime": "2024-01-15T15:00:00Z",
      "status": "RUNNING"
    },
    "problems": [
      { "id": 1, "title": "A - Two Sum", "balloonColor": "RED", "order": 1 },
      { "id": 2, "title": "B - Binary Search", "balloonColor": "BLUE", "order": 2 }
    ],
    "scoreboard": [
      {
        "teamId": 10,
        "teamName": "Alpha",
        "rank": 1,
        "solved": 1,
        "penalty": 20,
        "problems": {
          "1": { "status": "AC", "attempts": 1, "time": 20 },
          "2": { "status": "--", "attempts": 0, "time": 0 }
        }
      }
    ],
    "myTeamId": 10  // Only present for authenticated user's team
  }
}
```

---

### SCOREBOARD_UPDATE (Server → Client)

Incremental delta (sent on each submission result).

```json
{
  "type": "SCOREBOARD_UPDATE",
  "payload": {
    "teamId": 10,
    "teamName": "Alpha",
    "rank": 1,
    "solved": 2,
    "penalty": 65,
    "problemUpdates": {
      "2": { "status": "AC", "attempts": 2, "time": 45 }
    }
  }
}
```

---

### TIMER_TICK (Server → Client)

Sent every second during competition.

```json
{
  "type": "TIMER_TICK",
  "payload": {
    "remainingSeconds": 7200,
    "formatted": "02:00:00"
  }
}
```

---

### BALLOON_AWARDED (Server → Client)

```json
{
  "type": "BALLOON_AWARDED",
  "payload": {
    "teamId": 10,
    "teamName": "Alpha",
    "problemId": 2,
    "problemTitle": "B - Binary Search",
    "balloonColor": "BLUE",
    "timestamp": "2024-01-15T10:45:00Z"
  }
}
```

---

### SUBMIT (Client → Server)

```json
{
  "type": "SUBMIT",
  "payload": {
    "problemId": 1,
    "competitionId": 1,
    "language": "CPP",
    "code": "#include <bits/stdc++.h>\nusing namespace std;\nint main() { ... }"
  },
  "requestId": "sub-456"
}
```

---

### SUBMISSION_STATUS (Server → Client)

Sent during judging pipeline.

```json
{
  "type": "SUBMISSION_STATUS",
  "payload": {
    "submissionId": 789,
    "status": "COMPILING",
    "progress": 0.3,
    "message": "Compiling with g++ -std=c++17 -O2 -pipe"
  },
  "requestId": "sub-456"
}
```

**Status values:** `QUEUED`, `COMPILING`, `RUNNING`, `JUDGING`

---

### SUBMISSION_RESULT (Server → Client)

Final verdict.

```json
{
  "type": "SUBMISSION_RESULT",
  "payload": {
    "submissionId": 789,
    "problemId": 1,
    "verdict": "ACCEPTED",
    "timeMs": 45,
    "memoryKb": 2048,
    "score": 100,
    "compileOutput": "",
    "testResults": [
      { "testCase": 1, "status": "ACCEPTED", "timeMs": 5, "memoryKb": 1024, "output": "0 1" },
      { "testCase": 2, "status": "ACCEPTED", "timeMs": 8, "memoryKb": 1024, "output": "1 2" },
      { "testCase": 3, "status": "WRONG_ANSWER", "timeMs": 10, "memoryKb": 1024, "output": "wrong" }
    ]
  },
  "requestId": "sub-456"
}
```

**Verdict values:**
- `ACCEPTED` — All test cases passed
- `WRONG_ANSWER` — Output mismatch
- `TIME_LIMIT_EXCEEDED` — Exceeded problem time limit
- `MEMORY_LIMIT_EXCEEDED` — Exceeded problem memory limit
- `RUNTIME_ERROR` — Non-zero exit code / signal
- `COMPILATION_ERROR` — Compile failed
- `INTERNAL_ERROR` — Judge system error

---

### ERROR (Server → Client)

```json
{
  "type": "ERROR",
  "payload": {
    "code": "RATE_LIMITED",
    "message": "Too many submissions, please wait"
  },
  "requestId": "sub-456"
}
```

**Error codes:**
| Code | Cause |
|------|-------|
| `AUTH_REQUIRED` | Frame sent before AUTH |
| `AUTH_FAILED` | Invalid token/credentials |
| `INVALID_FRAME` | Malformed JSON or unknown type |
| `RATE_LIMITED` | Too many requests |
| `COMPETITION_NOT_ACTIVE` | Competition not running |
| `PROBLEM_NOT_FOUND` | Invalid problemId |
| `INTERNAL_ERROR` | Server exception |

---

## Client Implementation Notes

### TOFU Certificate Pinning

On first connection, client verifies server certificate fingerprint:
1. Computes SHA-256 of server cert
2. Stores in `~/.xide/trusted-certificates.properties`
3. Subsequent connections must match pinned fingerprint
4. Mismatch → connection rejected (security)

**Automation:** Set `XIDE_TLS_FINGERPRINT` env var with verified fingerprint.

### Reconnection

Client implements exponential backoff:
- Initial: 1s
- Max: 30s
- Jitter: ±20%
- Resends AUTH on reconnect

### Frame Ordering

- Frames processed sequentially per connection
- `requestId` correlates requests/responses
- No guaranteed ordering across different `requestId`s

---

## Server Implementation Notes

### Router

`WebSocketRouter` maps `type` → `@OnMessage` method:
```java
@OnMessage("SUBMIT")
public void handleSubmit(Session session, SocketMessage msg) { ... }
```

### Session Management

`ClientSessions` tracks authenticated sessions:
- `userId` → `Session` mapping
- Used for targeted pushes (scoreboard, timer)
- Cleaned up on close/error

### Threading

- Tyrus calls `@OnMessage` on its worker threads
- Controllers must be thread-safe (stateless)
- Async sends: `session.getAsyncRemote().sendText(json)`

---

## Testing

```bash
# Unit test router
make test-server

# Integration test (full stack)
make run-server
make communication-test

# Manual with wscat
wscat -c wss://localhost:8443/ws/socket
> {"type":"AUTH","payload":{"token":"..."}}
< {"type":"AUTH_RESULT","payload":{"success":true,...}}
> {"type":"PING","payload":{}}
< {"type":"PONG","payload":"pong"}
```