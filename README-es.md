# OpenCode Mobile

**Cliente nativo Android para OpenCode** — conectate a tu servidor OpenCode desde tu celular.

## Features

- **Gestión de Sesiones** — Crear, listar, eliminar y hacer fork de sesiones
- **Streaming en Tiempo Real** — Ve las respuestas mientras se generan vía SSE
- **Interfaz de Chat** — Material 3 con soporte de markdown
- **Visor de Herramientas** — Observa las tools ejecutándose en vivo (bash, archivos, búsqueda)
- **Flujo de Permisos** — Aprobá o denegá acciones del agente desde el celular
- **Selección de Modelo** — Cambiá entre providers y modelos
- **Selección de Agente** — Elegí entre build/plan
- **Modo Oscuro** — Tema claro/oscuro completo

## Arquitectura

```
┌──────────────────┐    HTTP/SSE     ┌──────────────────┐
│  OpenCode Mobile │ ◄────────────► │  OpenCode Server │
│  (Kotlin/Compose)│   REST API      │  (PC, VPS, etc)  │
│  Cliente delgado │   + SSE events  │  Corre LLM+tools │
└──────────────────┘                  └──────────────────┘
```

La app es un **cliente delgado** — todo el procesamiento pesado (inferencia LLM, ejecución de herramientas, git) ocurre en el servidor. La app solo se comunica vía HTTP.

## Requisitos

- **Android 8.0+** (API 26)
- **Servidor OpenCode** corriendo y accesible (PC, VPS, o cloud)
- Conectividad de red al servidor (LAN, Tailscale VPN, o port forwarding)

## Setup

### 1. Correr el Servidor OpenCode

En tu máquina, iniciá OpenCode con acceso de red:

```bash
# Iniciar servidor en todas las interfaces
opencode serve --host 0.0.0.0 --port 4096

# O con autenticación
OPENCODE_SERVER_PASSWORD=tu-password opencode serve --host 0.0.0.0 --port 4096
```

### 2. Instalar la App

**Opción A: Descargar desde GitHub Releases**
1. Ir a [Releases](https://github.com/YOUR_USERNAME/opencode-mobile/releases)
2. Descargar el último `OpenCode-Mobile-debug.apk`
3. Instalar en tu dispositivo Android

**Opción B: Compilar desde el código**
```bash
git clone https://github.com/YOUR_USERNAME/opencode-mobile.git
cd opencode-mobile
./gradlew assembleDebug
# APK en: app/build/outputs/apk/debug/app-debug.apk
```

### 3. Configurar la Conexión

1. Abrir la app
2. Tocar **Settings** (ícono de engranaje)
3. Ingresar la URL del servidor (ej: `http://192.168.18.11:4096`)
4. Ingresar password si es necesario
5. Seleccionar provider, modelo y agente preferidos
6. Tocar **Save**

## Configuración

### Formatos de URL del Servidor

| Escenario | Formato URL |
|-----------|------------|
| LAN (misma red) | `http://192.168.x.x:4096` |
| Tailscale VPN | `http://hostname.tail:4096` |
| Port forwarding | `http://tu-dominio:4096` |

## Stack Tecnológico

| Componente | Tecnología |
|------------|-----------|
| Lenguaje | Kotlin |
| UI | Jetpack Compose (Material 3) |
| HTTP Client | Ktor + OkHttp |
| SSE | OkHttp EventSource |
| JSON | kotlinx.serialization |
| Estado | StateFlow + ViewModel |
| Navegación | Compose Navigation |
| DI | Hilt |
| Settings | DataStore |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |

## API Reference

La app consume la HTTP API de OpenCode. Endpoints principales:

| Endpoint | Método | Propósito |
|----------|--------|-----------|
| `/global/health` | GET | Health check del servidor |
| `/session` | GET/POST | Listar/Crear sesiones |
| `/session/:id` | GET/DELETE | Obtener/Eliminar sesión |
| `/session/:id/message` | GET/POST | Listar/Enviar mensajes |
| `/session/:id/prompt_async` | POST | Enviar mensaje (async) |
| `/session/:id/abort` | POST | Abortar streaming |
| `/event` | GET (SSE) | Eventos en tiempo real |
| `/provider` | GET | Listar providers |
| `/agent` | GET | Listar agents |
| `/config` | GET/PATCH | Configuración |

## Licencia

MIT
