---
type: ci-cd
title: Workflows
description: Per-workflow details for the GitHub Actions that build, test, and release the app.
tags: [ci-cd, github-actions, build, release]
---

# Workflows

This page documents the workflows that build and test the APK.

## `build.yml`

| Field | Value |
|-------|-------|
| Name | Build Android App |
| Trigger | `workflow_dispatch` (manual button in the Actions tab) |
| Concurrency | `${{ github.workflow }}-${{ github.ref }}` with `cancel-in-progress: true` |
| Runner | `ubuntu-latest`, 30 min timeout |

### Steps

1. **Checkout** (`actions/checkout@v4`).
2. **Set up JDK 17** — `actions/setup-java@v5` with `temurin` distribution.
3. **Setup Android SDK** — `android-actions/setup-android@v3`.
4. **Cache Gradle packages** — caches `~/.gradle/caches` and `~/.gradle/wrapper`,
   keyed on `runner.os` and the hash of all `*.gradle*` and
   `gradle-wrapper.properties`. Fallback restore-keys use just the OS so
   partial cache hits still warm the daemon.
5. **Grant execute permission for gradlew** — `chmod +x gradlew`.
6. **Run unit tests** — `./gradlew testDebugUnitTest --stacktrace`. Currently
   a no-op because no tests exist.
7. **Build Debug APK** — `./gradlew assembleDebug --stacktrace`.
8. **Build Release APK** — `./gradlew assembleRelease --stacktrace`.
9. **Upload artifacts**:

| Artifact | Retention |
|----------|-----------|
| `app-debug` | 7 days |
| `app-release` | 30 days |
| `test-results` | 7 days (always, even on failure) |

### Concurrency

The concurrency group is shared with `cancel-in-progress: true`, so two
manual builds cancel each other instead of queueing.

## `auto-manage.yml`

| Field | Value |
|-------|-------|
| Name | Auto Manage |
| Trigger | Issues opened/reopened; PRs opened |
| Purpose | Add the `needs-triage` label to new issues and auto-assign every new issue/PR to `niklasschaeffer`. |

Both jobs authenticate with the same `chronova-agent` GitHub App token used
by the other workflows.

## `update-wiki.yml`

See [Wiki Pipeline](./wiki-pipeline.md) for the full flow. In short:
- Installs `@chronova/wiki-agent` and Bun.
- Runs `wiki --update --print --verbose --wiki`.
- If `.wiki/` changed (excluding the run metadata), opens a `wiki/staging-<timestamp>`
  PR against `main` and (if the wiki repo is initialized) pushes the
  flattened output to the GitHub wiki repo.

## `omp-ci.yml`, `omp.yml`, `omp-fix-issue.yml`

See [OMP Agent](./omp-agent.md).

## `vouch-pr.yml`, `vouch-manage.yml`

See [Vouch System](./vouch-system.md).
