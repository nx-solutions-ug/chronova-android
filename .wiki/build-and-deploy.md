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
| Lifecycle | `androidx.lifecycle:lifecycle-livedata-ktx` | 2.10.0 |
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

## CI/CD workflows

Workflows live in `.github/workflows/`:

| Workflow | Trigger | Purpose |
|----------|---------|---------|
| `build.yml` | Manual (`workflow_dispatch`) | Build and test the app, then upload debug and release APK artifacts. |
| `update-wiki.yml` | Push to `main`, daily schedule, or manual | Runs the Wiki Agent to update `.wiki`, then publishes to the GitHub Wiki and opens a staging snapshot PR. |
| `auto-manage.yml` | Issues opened/reopened, PRs opened | Labels new issues `needs-triage` and auto-assigns them to `niklasschaeffer`. |
| `omp-ci.yml` | Issues opened, PR opened/synchronize/ready for review | OMP agent triage, labeling, and PR review. |
| `omp.yml` | Issue/PR comment containing `/omp` | Runs OMP agent commands from `.omp/commands/`. |
| `vouch-pr.yml` | PR opened/reopened/ready for review | Closes PRs from unvouched users and applies the `vouched` label to allowed PRs. |
| `vouch-manage.yml` | Discussion comment created | Applies maintainer `!vouch`, `!denounce`, and `!unvouch` commands. |

See [Contributing](./contributing.md) for how the vouch gate works.

## Troubleshooting

- **JDK mismatch**: ensure `JAVA_HOME` points to JDK 17. The `app/build.gradle` enforces `jvmTarget = '17'`.
- **SDK not found**: install API 36 platform and build-tools through Android Studio or `sdkmanager`.
- **Docker permission errors**: the script runs `chmod +x ./gradlew` inside the container.
