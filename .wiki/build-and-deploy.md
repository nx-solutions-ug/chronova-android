---
type: build
title: Build & Deploy
description: Gradle commands, release signing, and Docker builds for Chronova Android.
tags: [build, gradle, docker, release]
---

# Build & Deploy

## Build environment

- **Gradle**: 9.2.1 (wrapper distribution)
- **Android Gradle Plugin**: 8.13.2
- **Kotlin**: 2.1.20
- **Compile / Target SDK**: 36
- **Min SDK**: 24
- **JVM target**: 17
- **Build tool**: command line Gradle or Android Studio Ladybug+

## Key dependencies

Current versions are defined in `app/build.gradle`:

| Category | Library | Version |
|----------|---------|---------|
| UI core | `androidx.core:core-ktx` | 1.17.0 |
| UI core | `androidx.appcompat:appcompat` | 1.7.1 |
| UI core | `com.google.android.material:material` | 1.13.0 |
| UI core | `androidx.constraintlayout:constraintlayout` | 2.2.1 |
| Navigation | `androidx.navigation:navigation-fragment-ktx` | 2.9.8 |
| Navigation | `androidx.navigation:navigation-ui-ktx` | 2.9.6 |
| Lifecycle | `androidx.lifecycle:lifecycle-viewmodel-ktx` | 2.10.0 |
| Lifecycle | `androidx.lifecycle:lifecycle-livedata-ktx` | 2.11.0 |
| Networking | `com.squareup.retrofit2:retrofit` | 3.0.0 |
| Networking | `com.squareup.retrofit2:converter-gson` | 3.0.0 |
| Networking | `com.squareup.okhttp3:logging-interceptor` | 5.3.2 |
| Charts | `com.github.PhilJay:MPAndroidChart` | 3.1.0 |
| Lists | `androidx.recyclerview:recyclerview` | 1.4.0 |
| Paging | `androidx.viewpager2:viewpager2` | 1.1.0 |
| Storage | `androidx.preference:preference-ktx` | 1.2.1 |
| Tests | `junit:junit` (unit) | 4.13.2 |
| Tests | `androidx.test.ext:junit` (instrumented) | 1.3.0 |
| Tests | `androidx.test.espresso:espresso-core` | 3.7.0 |

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
| `debug` | — | default debug key | Incremental compilation disabled (`enableIncrementalCompilation = false`). |
| `release` | `false` | `signingConfigs.release` | Uses committed release keystore; no ProGuard/R8 minification. |

## CI/CD

The `.github/workflows/build.yml` workflow builds, tests, and uploads APKs on manual trigger (`workflow_dispatch`).

### Workflow steps

1. Check out the repository with `actions/checkout@v4`.
2. Set up **JDK 17** with `actions/setup-java@v5` (Temurin distribution).
3. Install the Android SDK with `android-actions/setup-android@v3`.
4. Cache Gradle packages with `actions/cache@v4`.
5. Run `./gradlew testDebugUnitTest`.
6. Build debug and release APKs with `./gradlew assembleDebug` and `./gradlew assembleRelease`.
7. Upload artifacts:
   - `app-debug` — retention 7 days.
   - `app-release` — retention 30 days.
   - `test-results` — retention 7 days.

### Recent CI change

`actions/setup-java` was updated to **v5** in commit `d63831c` (#28). The workflow continues to use JDK 17 and the Temurin distribution.

## Troubleshooting

- **JDK mismatch**: ensure `JAVA_HOME` points to JDK 17. The `app/build.gradle` enforces `jvmTarget = '17'`.
- **SDK not found**: install API 36 platform and build-tools through Android Studio or `sdkmanager`.
- **Docker permission errors**: the script runs `chmod +x ./gradlew` inside the container.
