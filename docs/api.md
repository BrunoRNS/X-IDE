# X-IDE API Reference

## HTTP API (Javalin, port 8081 → Nginx 8443)

All endpoints require HTTPS in production. Base path: `https://<host>:8443/`

### Authentication

Currently no HTTP-level authentication (session-based via WebSocket). Instructor pages assume trusted LAN.

---

### Student Management

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/students` | List all students (HTML page) |
| `GET` | `/pdf-users` | Import students from PDF roster |
| `POST` | `/students/register` | Register new student (form) |
| `PUT` | `/students/update/<id>` | Update student info |
| `DELETE` | `/students/delete/<id>` | Delete student |

**Register Student (form):**
```
POST /students/register
Content-Type: application/x-www-form-urlencoded

name=John+Doe&email=john%40example.com&password=secret&classId=1
```

---

### Classroom Management

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/classroom` | Classroom dashboard (hosted folders) |
| `GET` | `/class` | List all classes (HTML) |
| `GET` | `/class/{id}` | Show class details |
| `POST` | `/class/{id}` | Join class / create classroom folder |

---

### Competition Management

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/competition` | List competitions |
| `POST` | `/competition/start` | Start a competition |
| `DELETE` | `/competition/stop` | Stop active competition |

**Start Competition:**
```
POST /competition/start
Content-Type: application/json

{
  "name": "ICPC Regional 2024",
  "durationMinutes": 300,
  "problemIds": [1, 2, 3, 4, 5],
  "classId": 1
}
```

---

### Problem Management (Problens)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/problens/new` | Create problem (multipart) |
| `PUT` | `/problems/{id}` | Update problem |
| `GET` | `/problens/all` | List all problems (JSON) |
| `POST` | `/problens/id` | Get problem by ID (JSON) |
| `GET` | `/problems/{id}/pdf` | Download problem PDF |
| `DELETE` | `/problems/delete/{id}` | Delete problem |

**Create Problem (multipart/form-data):**
```
POST /problens/new
Content-Type: multipart/form-data

title: "Two Sum"
description: "Find two numbers that add up to target..."
timeLimitMs: 1000
memoryLimitMb: 256
difficulty: "EASY"
balloonColor: "RED"
examples[0].input: "2 7 11 15\n9"
examples[0].output: "0 1"
examples[0].explanation: "Indices 0 and 1 sum to 9"
testCases[0].input: "..." (hidden)
testCases[0].output: "..." (hidden)
pdfFile: (optional PDF statement)
```

---

### Health / Utility

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/` | Landing page (HTML) |

---

## WebSocket API (Tyrus, port 8082 → Nginx 8443/ws)

Connection URL: `wss://<host>:8443/ws/socket`

### Frame Format

All frames are JSON objects matching `org.xide.protocol.SocketMessage`:

```json
{
  "type": "FRAME_TYPE",
  "payload": { ... },
  "requestId": "optional-correlation-id"
}
```

**Special rule:** A frame with `"payload": "pong"` is a **response** and is never re-dispatched.

### Connection Lifecycle

```
1. CONNECT → wss://host:8443/ws/socket
2. SEND AUTH frame (required first)
3. RECEIVE AUTH_RESULT
4. SEND/RECEIVE domain frames
5. DISCONNECT (or server closes)
```

---

### Authentication Frames

#### AUTH (Client → Server)

**Token-based:**
```json
{
  "type": "AUTH",
  "payload": { "token": "eyJhbGciOiJIUzI1NiIs..." }
}
```

**Credentials-based:**
```json
{
  "type": "AUTH",
  "payload": { "username": "student@example.com", "password": "secret" }
}
```

#### AUTH_RESULT (Server → Client)

**Success:**
```json
{
  "type": "AUTH_RESULT",
  "payload": {
    "success": true,
    "user": { "id": 123, "name": "John Doe", "role": "STUDENT" },
    "token": "eyJhbGciOiJIUzI1NiIs..." (new token if credentials used)
  }
}
```

**Failure:**
```json
{
  "type": "AUTH_RESULT",
  "payload": { "success": false, "error": "Invalid credentials" }
}
```

---

### Develop Frames (Debug/Sandbox)

| Frame Type | Direction | Description |
|------------|-----------|-------------|
| `PING` | C→S | Health check |
| `PONG` | S→C | Response to PING (special: not re-dispatched) |
| `RUN_CODE` | C→S | Execute code snippet |
| `RUN_RESULT` | S→C | Execution result |

#### RUN_CODE
```json
{
  "type": "RUN_CODE",
  "payload": {
    "code": "#include <stdio.h>\nint main() { printf(\"hello\"); }",
    "language": "CPP",
    "input": ""
  },
  "requestId": "req-123"
}
```

#### RUN_RESULT
```json
{
  "type": "RUN_RESULT",
  "payload": {
    "stdout": "hello",
    "stderr": "",
    "exitCode": 0,
    "timeMs": 12,
    "memoryKb": 1024
  },
  "requestId": "req-123"
}
```

---

### Competition Frames (Real-time)

| Frame Type | Direction | Description |
|------------|-----------|-------------|
| `COMPETITION_STATE` | S→C | Full state sync (on join) |
| `SCOREBOARD_UPDATE` | S→C | Incremental scoreboard delta |
| `TIMER_TICK` | S→C | Remaining time (every second) |
| `BALLOON_AWARDED` | S→C | Team solved problem → balloon color |

#### COMPETITION_STATE
```json
{
  "type": "COMPETITION_STATE",
  "payload": {
    "competitionId": 1,
    "name": "ICPC Regional",
    "startTime": "2024-01-15T10:00:00Z",
    "endTime": "2024-01-15T15:00:00Z",
    "problems": [
      { "id": 1, "title": "A", "balloonColor": "RED" },
      { "id": 2, "title": "B", "balloonColor": "BLUE" }
    ],
    "scoreboard": [
      { "teamId": 1, "name": "Team Alpha", "solved": 1, "penalty": 20, "problems": { "1": "AC", "2": "--" } }
    ]
  }
}
```

---

### Submission Frames

| Frame Type | Direction | Description |
|------------|-----------|-------------|
| `SUBMIT` | C→S | Submit solution for judging |
| `SUBMISSION_STATUS` | S→C | Status update (queued, compiling, running) |
| `SUBMISSION_RESULT` | S→C | Final verdict |

#### SUBMIT
```json
{
  "type": "SUBMIT",
  "payload": {
    "problemId": 1,
    "language": "CPP",
    "code": "#include <bits/stdc++.h>\nusing namespace std;\nint main() { ... }",
    "competitionId": 1
  },
  "requestId": "sub-456"
}
```

#### SUBMISSION_STATUS
```json
{
  "type": "SUBMISSION_STATUS",
  "payload": {
    "submissionId": 789,
    "status": "RUNNING",
    "testCase": 3,
    "totalTestCases": 10
  },
  "requestId": "sub-456"
}
```

#### SUBMISSION_RESULT
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
    "testResults": [
      { "testCase": 1, "status": "ACCEPTED", "timeMs": 5, "memoryKb": 1024 },
      { "testCase": 2, "status": "ACCEPTED", "timeMs": 8, "memoryKb": 1024 }
    ]
  },
  "requestId": "sub-456"
}
```

**Verdict values:** `ACCEPTED`, `WRONG_ANSWER`, `TIME_LIMIT_EXCEEDED`, `MEMORY_LIMIT_EXCEEDED`, `RUNTIME_ERROR`, `COMPILATION_ERROR`, `INTERNAL_ERROR`

---

### Error Frames

```json
{
  "type": "ERROR",
  "payload": {
    "code": "AUTH_REQUIRED",
    "message": "Authentication required before submitting"
  },
  "requestId": "req-123"
}
```

**Error codes:** `AUTH_REQUIRED`, `INVALID_FRAME`, `RATE_LIMITED`, `COMPETITION_NOT_ACTIVE`, `PROBLEM_NOT_FOUND`, `INTERNAL_ERROR`

---

## Data Models (JSON)

### User
```json
{
  "id": 123,
  "name": "John Doe",
  "email": "john@example.com",
  "role": "STUDENT",
  "classId": 1,
  "createdAt": "2024-01-10T08:00:00Z"
}
```

### Problem
```json
{
  "id": 1,
  "title": "Two Sum",
  "description": "Find two numbers...",
  "timeLimitMs": 1000,
  "memoryLimitMb": 256,
  "difficulty": "EASY",
  "balloonColor": "RED",
  "examples": [
    { "input": "2 7 11 15\n9", "output": "0 1", "explanation": "..." }
  ],
  "createdAt": "2024-01-10T08:00:00Z"
}
```

### Submission
```json
{
  "id": 789,
  "userId": 123,
  "problemId": 1,
  "competitionId": 1,
  "language": "CPP",
  "code": "...",
  "status": "ACCEPTED",
  "timeMs": 45,
  "memoryKb": 2048,
  "submittedAt": "2024-01-15T10:30:00Z"
}
```

### Competition
```json
{
  "id": 1,
  "name": "ICPC Regional 2024",
  "startTime": "2024-01-15T10:00:00Z",
  "endTime": "2024-01-15T15:00:00Z",
  "status": "RUNNING",
  "problems": [1, 2, 3, 4, 5],
  "classId": 1
}
```

---

## Client Usage Examples

### Java (Direct JAR)
```bash
# Interactive (prompts for server IP)
java -jar client/target/client-1.0-SNAPSHOT-all.jar

# Automated (with TOFU fingerprint)
XIDE_TLS_FINGERPRINT="AA:BB:CC:..." \
java -jar client/target/client-1.0-SNAPSHOT-all.jar \
  --endpoint wss://192.168.1.100:8443/ws/socket /develop/ping --message ping
```

### Make Commands
```bash
make run-client                                    # Interactive
make communication-test                            # Ping/pong both ways
make remote-test URL=wss://192.168.1.100:8443/ws/socket
```