# OpenCode Mobile

**Native Android client for OpenCode** — connect to your OpenCode server from your phone.

## Features

- **Session Management** — Create, list, delete, and fork sessions
- **Real-time Streaming** — See responses as they're generated via SSE
- **Chat Interface** — Material 3 design with markdown support
- **Tool Visualizer** — Watch tools execute in real-time (bash, file ops, search)
- **Permission Flow** — Approve or deny agent actions from your phone
- **Model Selection** — Switch between providers and models on the fly
- **Agent Selection** — Choose between build/plan agents
- **Dark Mode** — Full light/dark theme support

## Architecture

```
┌──────────────────┐    HTTP/SSE     ┌──────────────────┐
│  OpenCode Mobile │ ◄────────────► │  OpenCode Server │
│  (Kotlin/Compose)│   REST API      │  (PC, VPS, etc)  │
│  Thin client     │   + SSE events  │  Runs LLM+tools  │
└──────────────────┘                  └──────────────────┘
```

The app is a **thin client** — all heavy computation (LLM inference, tool execution, git operations) happens on the server. The app just talks to it over HTTP.

## Requirements

- **Android 8.0+** (API 26)
- **OpenCode server** running and accessible (PC, VPS, or cloud)
- Network connectivity to the server (LAN, Tailscale VPN, or port forwarding)

## Setup

### 1. Run OpenCode Server

On your machine, start OpenCode with network access:

```bash
# Start server on all interfaces
opencode serve --host 0.0.0.0 --port 4096

# Or with authentication
OPENCODE_SERVER_PASSWORD=yourpassword opencode serve --host 0.0.0.0 --port 4096
```

### 2. Install the App

**Option A: Download from GitHub Releases**
1. Go to [Releases](https://github.com/YOUR_USERNAME/opencode-mobile/releases)
2. Download the latest `OpenCode-Mobile-debug.apk`
3. Install on your Android device

**Option B: Build from Source**
```bash
git clone https://github.com/YOUR_USERNAME/opencode-mobile.git
cd opencode-mobile
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

### 3. Configure Connection

1. Open the app
2. Tap **Settings** (gear icon)
3. Enter your server URL (e.g., `http://192.168.18.11:4096`)
4. Enter password if required
5. Select your preferred provider, model, and agent
6. Tap **Save**

## Configuration

### Server URL Formats

| Scenario | URL Format |
|----------|-----------|
| LAN (same network) | `http://192.168.x.x:4096` |
| Tailscale VPN | `http://hostname.tail:4096` |
| Port forwarding | `http://your-domain:4096` |

### Android Manifest

The app requires:
- `INTERNET` permission — to connect to the server
- `ACCESS_NETWORK_STATE` — to check connectivity
- `usesCleartextTraffic="true"` — for HTTP (non-HTTPS) connections

## Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3) |
| HTTP Client | Ktor + OkHttp |
| SSE | OkHttp EventSource |
| JSON | kotlinx.serialization |
| State | StateFlow + ViewModel |
| Navigation | Compose Navigation |
| DI | Hilt |
| Settings | DataStore |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |

## API Reference

The app consumes the OpenCode HTTP API. Key endpoints:

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/global/health` | GET | Server health check |
| `/session` | GET/POST | List/Create sessions |
| `/session/:id` | GET/DELETE | Get/Delete session |
| `/session/:id/message` | GET/POST | List/Send messages |
| `/session/:id/prompt_async` | POST | Send message (async) |
| `/session/:id/abort` | POST | Abort streaming |
| `/event` | GET (SSE) | Real-time events |
| `/provider` | GET | List providers |
| `/agent` | GET | List agents |
| `/config` | GET/PATCH | Configuration |

## License

MIT
