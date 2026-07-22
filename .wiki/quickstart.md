---
type: quickstart
title: Quickstart
description: Build, install, and run the Chronova Android app for the first time.
tags: [quickstart, build, install]
---

# Quickstart

Get the Chronova Android app running on a device or emulator.

## Prerequisites

- Android Studio Ladybug (2024.2.1) or newer.
- JDK 17 or higher.
- Android SDK API 36.
- A Chronova server URL and API key, or Chronova account credentials.

## Clone the repository

```bash
git clone <repository-url>
cd chronova-android
```

## Build a debug APK

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

## Install on a device or emulator

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## First launch

1. Open the app. It starts at `MainActivity` and redirects to `LoginActivity` if no API key is saved.
2. Enter your **server URL** (default is `https://chronova.dev/`).
3. Authenticate in one of two ways:
   - **Login**: email + password → the app saves the returned API key.
   - **API key**: paste an existing API key directly.
4. Tap the login / API key button. On success, `MainActivity` loads the dashboard.

## Server configuration

The server URL is stored in `SharedPreferences` under the key `server_url`. The app writes it through [`ChronovaRepository.saveServerUrl()`](./data-layer.md#server-url). The default value is `https://chronova.dev/`.

## Docker build

If you prefer a containerized build:

```bash
./docker-build.sh
```

This builds `chronova-android-builder` from `Dockerfile.build`, runs `./gradlew assembleDebug`, and writes the APK to `build-output/apk/debug/app-debug.apk`.

## Next steps

- Read [Architecture Overview](./architecture/overview.md) to understand the layers.
- Read [Development Conventions](./development-conventions.md) before making code changes.
