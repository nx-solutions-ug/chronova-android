---
type: ci-cd
title: Workflows
description: Per-workflow details for the GitHub Actions that build, test, and
  release the app.
tags: [ ci-cd, github-actions, build, release ]
last_updated: 2026-09-07T17:08:05.344Z
updated_by: wiki-agent
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

1. **Checkout** (`actions/checkout@v7`).
2. **Set up JDK 17** — `actions/setup-java@v6` with `temurin` distribution.
3. **Setup Android SDK** — `android-actions/setup-android@v4`.
4. **Cache Gradle packages** — `actions/cache@v6`, caches `~/.gradle/caches` and `~/.gradle/wrapper`,
   keyed on `runner.os` and the hash of all `*.gradle*` and
   `gradle-wrapper.properties`. Fallback restore-keys use just the OS so
   partial cache hits still warm the daemon.
5. **Grant execute permission for gradlew** — `chmod +x gradlew`.
6. **Run unit tests** — `./gradlew testDebugUnitTest --stacktrace`. Runs the single `InputStyleContractTest` (input-style regression guard).
7. **Build Debug APK** — `./gradlew assembleDebug --stacktrace`.
8. **Build Release APK** — `./gradlew assembleRelease --stacktrace`.
9. **Upload artifacts**:

| Artifact | Retention | Uploaded with |
|----------|-----------|---------------|
| `app-debug` | 7 days | `actions/upload-artifact@v7` |
| `app-release` | 30 days | `actions/upload-artifact@v7` |
| `test-results` | 7 days (always, even on failure) | `actions/upload-artifact@v7` |

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

## `omp-ci.yml`, `omp.yml`, `omp-code-review.yml`, `omp-fix-issue.yml`

See [OMP Agent](./omp-agent.md).

## `vouch-pr.yml`, `vouch-manage.yml`

See [Vouch System](./vouch-system.md).
