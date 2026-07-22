---
type: build
title: Build & Deploy
description: Gradle commands, release signing, and Docker builds for Chronova Android.
tags: [build, gradle, docker, release]
---

# Build & Deploy

## Build environment

- **Gradle**: 9.2.1 (wrapper distribution URL in `gradle/wrapper/gradle-wrapper.properties`)
- **Android Gradle Plugin**: 8.13.2
- **Kotlin**: 2.1.20
- **Compile / Target SDK**: 36
- **Min SDK**: 24
- **JVM target**: 17
- **Build tool**: command line Gradle or Android Studio Ladybug+

## Gradle configuration

- **Build cache**: disabled globally (`org.gradle.caching=false`) and in `settings.gradle` (`buildCache { local { enabled = false } }`) to ensure fresh builds.
- **Repository mode**: `FAIL_ON_PROJECT_REPOS` — all dependencies must be declared in `settings.gradle`.
- **JitPack**: required for MPAndroidChart (`maven { url = 'https://jitpack.io' }`).

## Debug build

```bash
./gradlew assembleDebug
```

Output APK:

```
app/build/outputs/apk/debug/app-debug.apk
```

Install it:

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Release build

```bash
./gradlew assembleRelease
```

Output APK:

```
app/build/outputs/apk/release/app-release.apk
```

### Release signing

The release build is pre-configured with a committed keystore in `app/build.gradle`:

```gradle
signingConfigs {
    release {
        storeFile file('../chronova-release-key.keystore')
        storePassword 'chronova123'
        keyAlias 'chronova'
        keyPassword 'chronova123'
    }
}
```

> The keystore is checked into the repository for convenience in this project. For production apps, store credentials outside source control.

## Build helper scripts

### `build.sh`

```bash
./build.sh         # Debug build → ../public/downloads/chronova-debug.apk
./build.sh release # Release build → ../public/downloads/chronova-release.apk
```

The script:
1. Verifies it is run from the repository root.
2. Makes `gradlew` executable.
3. Runs `./gradlew clean`.
4. Builds the requested variant.
5. Copies the resulting APK to `../public/downloads/chronova-<variant>.apk`.
6. Prints the APK size and install instructions.

## Docker build

A self-contained Docker image downloads the Android SDK and builds the debug APK.

```bash
./docker-build.sh
```

Steps performed:

1. Build `chronova-android-builder` from `Dockerfile.build`.
2. Run the container, mounting `./build-output` to `/app/app/build/outputs`.
3. Execute `./gradlew assembleDebug` inside the container.
4. Report the resulting APK size.

The Dockerfile uses:

- `openjdk:17.0.2-jdk-slim`
- Android command-line tools `9477386`
- SDK platform `android-34` and build-tools `34.0.0`

## Cleaning

```bash
./gradlew clean
```

This removes the root `buildDir`.

## Build flavors / types

| Type | `minifyEnabled` | Signing | Notes |
|------|-----------------|---------|-------|
| `debug` | — | default debug key | `enableIncrementalCompilation = false` to avoid stale incremental artifacts. |
| `release` | `false` | `signingConfigs.release` | Uses committed release keystore; no ProGuard/R8 minification. |

## Troubleshooting

- **JDK mismatch**: ensure `JAVA_HOME` points to JDK 17. The `app/build.gradle` enforces `jvmTarget = '17'`.
- **SDK not found**: install API 36 platform and build-tools through Android Studio or `sdkmanager`.
- **Docker permission errors**: the script runs `chmod +x ./gradlew` inside the container.
