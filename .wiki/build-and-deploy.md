---
type: build
title: Build & Deploy
description: Gradle commands, release signing, and Docker builds for Chronova Android.
tags: [build, gradle, docker, release]
---

# Build & Deploy

## Build environment

- **Gradle**: 9.6.1 (wrapper distribution)
- **Android Gradle Plugin**: 8.13.2
- **Kotlin**: 2.1.20
- **Compile / Target SDK**: 36
- **Min SDK**: 24
- **JVM target**: 17
- **Build tool**: command line Gradle or Android Studio Ladybug+
- **Build cache**: Disabled (`org.gradle.caching=false` in `gradle.properties`; local cache off in `settings.gradle`) to ensure fresh builds.
- **Repository mode**: `FAIL_ON_PROJECT_REPOS` — all dependency repositories must be declared in `settings.gradle`.

## Key dependencies

Current versions are defined in `app/build.gradle`:

| Category | Library | Version |
|----------|---------|---------|
| UI core | `androidx.core:core-ktx` | 1.19.0 |
| UI core | `androidx.appcompat:appcompat` | 1.7.1 |
| UI core | `com.google.android.material:material` | 1.13.0 |
| UI core | `androidx.constraintlayout:constraintlayout` | 2.2.1 |
| Navigation | `androidx.navigation:navigation-fragment-ktx` | 2.9.8 |
| Navigation | `androidx.navigation:navigation-ui-ktx` | 2.9.8 |
| Lifecycle | `androidx.lifecycle:lifecycle-viewmodel-ktx` | 2.10.0 |
| Lifecycle | `androidx.lifecycle:lifecycle-livedata-ktx` | 2.11.0 |
| Networking | `com.squareup.retrofit2:retrofit` | 3.0.0 |
| Networking | `com.squareup.retrofit2:converter-gson` | 3.0.0 |
| Networking | `com.squareup.okhttp3:logging-interceptor` | 5.3.2 |
| Charts | `com.github.PhilJay:MPAndroidChart` (JitPack) | 3.1.0 |
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

## `build.sh` helper

The repository includes `build.sh` to clean, build, and copy the resulting APK to `../public/downloads/`.

```bash
./build.sh        # Debug APK
./build.sh release  # Release APK
```

Output is copied to:

```
../public/downloads/chronova-debug.apk
../public/downloads/chronova-release.apk
```

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
| `debug` | — | default debug key | Incremental compilation disabled (`ext.enableIncrementalCompilation = false`). |
| `release` | `false` | `signingConfigs.release` | Uses committed release keystore; no ProGuard/R8 minification. |

## CI/CD

The `build.yml` workflow is triggered manually (`workflow_dispatch`). It sets up JDK 17, installs the Android SDK, runs unit tests, builds both debug and release APKs, and uploads them as artifacts with the following retention:

| Artifact | Retention |
|----------|-----------|
| `app-debug` | 7 days |
| `app-release` | 30 days |
| `test-results` | 7 days |

## Vouch merge gate

Pull requests from external contributors require a vouch. Two workflows enforce this:

- `vouch-pr.yml` — runs on opened/reopened/ready PRs, checks `.github/VOUCHED.td` via `mitchellh/vouch`, and auto-closes unvouched PRs. It also applies the `vouched` label when the check passes.
- `vouch-manage.yml` — lets maintainers manage `.github/VOUCHED.td` through Discussion comments (`!vouch`, `!denounce`, `!unvouch`).

Write access collaborators and bot accounts are automatically allowed. See `CONTRIBUTING.md` and [Development Conventions](./development-conventions.md) for the contribution process.

## Renovate

Dependency updates are managed by Renovate, configured in `renovate.json`. Major workflow actions (`actions/checkout`, `actions/cache`) and the Gradle wrapper are updated via the open PRs listed in the repository.

## Troubleshooting

- **JDK mismatch**: ensure `JAVA_HOME` points to JDK 17. The `app/build.gradle` enforces `jvmTarget = '17'`.
- **SDK not found**: install API 36 platform and build-tools through Android Studio or `sdkmanager`.
- **Docker permission errors**: the script runs `chmod +x ./gradlew` inside the container.
- **Missing dependency repository errors**: ensure no project-level repositories are added; `settings.gradle` uses `RepositoriesMode.FAIL_ON_PROJECT_REPOS` and declares JitPack explicitly for MPAndroidChart.
