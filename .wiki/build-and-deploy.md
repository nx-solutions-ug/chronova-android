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

GitHub Actions workflows live in `.github/workflows/`.

### `build.yml` — manual build + test

Triggered by `workflow_dispatch`. It sets up JDK 17, installs the Android SDK, runs `./gradlew testDebugUnitTest`, builds both debug and release APKs, and uploads them as artifacts:

| Artifact | Retention |
|----------|-----------|
| `app-debug` | 7 days |
| `app-release` | 30 days |
| `test-results` | 7 days |

### `omp-ci.yml` — OMP agent triage, labeling, and review

Runs on issue creation and on PR `opened`, `synchronize`, and `ready_for_review`. It has three jobs:

- **triage-issue** — classifies new issues, applies labels, sets issue type/priority fields, and dispatches `omp-fix-issue.yml`.
- **label-pr** — classifies PRs with a type label (`bug`, `feature`, `enhancement`, `docs`, `chore`) and a priority label (`priority: critical`, `priority: high`, `priority: medium`, `priority: low`). Skips if both are already present.
- **review-pr** — reviews PRs using the OMP agent. It installs the `agynio/gh-pr-review` extension so it can post inline review comments on specific diff lines, and skips re-review when the latest synchronized commit was authored by a bot or agent.

All OMP jobs authenticate via a GitHub App token (`secrets.APP_CLIENT_ID` / `secrets.APP_PRIVATE_KEY`) and use the `ollama-cloud/minimax-m3` model.

### `omp.yml` — on-demand `/omp` commands

Triggered by issue or PR review comments containing `/omp` (or starting with it). It expands a command template from `.omp/commands/${CMD_NAME}.md` and runs the OMP agent. This workflow also installs the `agynio/gh-pr-review` extension so review commands can leave inline comments.

### `omp-fix-issue.yml` — automated issue fixing

Triggered by a repository dispatch event `issue-triaged` from `omp-ci.yml`, or manually via `workflow_dispatch`. It reads `.omp/commands/fix-issue.md`, creates a branch, attempts a minimal fix, and opens a PR.

### `auto-manage.yml` — issue/PR housekeeping

- Tags newly opened/reopened issues with `needs-triage`.
- Auto-assigns newly opened issues and PRs to `niklasschaeffer`.

### `vouch-pr.yml` — PR trust gate

Runs on `pull_request_target` for `opened`, `reopened`, and `ready_for_review`. Uses the `mitchellh/vouch` action to close PRs from unvouched users. Bot accounts and collaborators with write access are automatically allowed. PRs that pass are labeled `vouched`.

### `vouch-manage.yml` — vouch management via discussions

Runs on discussion comments. Maintainers with `admin`, `maintain`, or `write` roles can vouch or denounce users with `!vouch`, `!denounce`, or `!unvouch`. The vouched list is maintained in `.github/VOUCHED.td`.

### `update-wiki.yml` — wiki update pipeline

Triggered on pushes to `main`, on a daily schedule (`0 8 * * *`), and manually. It installs the `@chronova/wiki-agent`, runs the agent to update `.wiki`, flattens the output, pushes the result to the repository's wiki repo, and opens a staging PR with the `.wiki` changes.

## Release notes

`.github/release-drafter.yml` categorizes merged PRs into Features, Bug Fixes, Maintenance, and Dependencies based on labels, and drafts the next release version from label-driven semver resolution.

## Renovate

Dependency updates are managed by Renovate, configured in `renovate.json`. Major workflow actions (`actions/checkout`, `actions/cache`) and the Gradle wrapper are updated via the open PRs listed in the repository.

## Troubleshooting

- **JDK mismatch**: ensure `JAVA_HOME` points to JDK 17. The `app/build.gradle` enforces `jvmTarget = '17'`.
- **SDK not found**: install API 36 platform and build-tools through Android Studio or `sdkmanager`.
- **Docker permission errors**: the script runs `chmod +x ./gradlew` inside the container.
- **Missing dependency repository errors**: ensure no project-level repositories are added; `settings.gradle` uses `RepositoriesMode.FAIL_ON_PROJECT_REPOS` and declares JitPack explicitly for MPAndroidChart.
