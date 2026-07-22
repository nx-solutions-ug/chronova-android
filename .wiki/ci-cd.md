---
type: ci-cd
title: CI/CD
description: GitHub Actions workflows, release automation, OMP agent, and wiki publishing for Chronova Android.
tags: [ci-cd, github-actions, workflow, omp, release]
---

# CI/CD

Chronova Android uses GitHub Actions for builds, issue/PR automation, agent-assisted triage/review, and wiki publishing.

All workflows live in `.github/workflows/`.

## Build workflow

`.github/workflows/build.yml`

| Property | Value |
|----------|-------|
| Trigger | `workflow_dispatch` (manual) |
| JDK | 17 (Temurin) |
| Android SDK | Installed via `android-actions/setup-android@v3` |
| Gradle cache | Enabled for `~/.gradle/caches` and `~/.gradle/wrapper` |
| Timeout | 30 minutes |

Steps:
1. Check out the repository.
2. Set up JDK 17 and the Android SDK.
3. Cache Gradle dependencies.
4. Run `./gradlew testDebugUnitTest --stacktrace`.
5. Run `./gradlew assembleDebug --stacktrace`.
6. Run `./gradlew assembleRelease --stacktrace`.
7. Upload APK and test-result artifacts.

### Artifacts

| Artifact | Path | Retention |
|----------|------|-----------|
| `app-debug` | `app/build/outputs/apk/debug/*.apk` | 7 days |
| `app-release` | `app/build/outputs/apk/release/*.apk` | 30 days |
| `test-results` | `app/build/reports/tests/` | 7 days |

## Auto-manage workflow

`.github/workflows/auto-manage.yml`

Triggered on new/reopened issues and new pull requests.

- Adds the `needs-triage` label to issues.
- Auto-assigns issues and PRs to `niklasschaeffer` using a GitHub App token (`APP_CLIENT_ID` + `APP_PRIVATE_KEY`).

## OMP agent workflows

Chronova uses the OMP agent for AI-assisted triage, labeling, review, and on-demand execution.

### `omp-ci.yml`

Runs automatically on issues and PRs:

| Job | Trigger | What it does |
|-----|---------|--------------|
| `triage-issue` | Issue opened or `workflow_dispatch` with `issue_number` | Reacts with 👀, runs `.omp/commands/triage-issue.md`, dispatches an `issue-triaged` repository event. |
| `label-pr` | PR opened / synchronized / marked ready for review | Skips if the PR already has both a type label and a priority label; otherwise runs `.omp/commands/label-pr.md`. |
| `review-pr` | PR opened/synchronized/ready_for_review or `workflow_dispatch` with `pr_number` | Reacts with 👀, runs `.omp/commands/review-pr.md`. Skips re-review when a synchronize event was authored by an agent/bot (`opencode-agent`, `github-actions`, `omp-agent`, `chronova-agent`). |

The workflows use the model `ollama-cloud/minimax-m3`. OMP is installed from `https://omp.sh/install` and authenticated with `OLLAMA_API_KEY`. A Python formatter (`.omp/stream-log.py`) structures the agent output.

### `omp.yml`

On-demand OMP execution. Triggered by an issue or PR review comment containing `/omp` (or ending with ` /omp`). It expands a command template from `.omp/commands/<command>.md` by replacing `$ARGUMENTS`, then runs OMP with the expanded prompt.

### OMP configuration

`.omp/config.yml` selects models per task:

| Mode | Model |
|------|-------|
| Default / explore / general | `ollama-cloud/devstral-2:123b` |
| Build | `ollama-cloud/glm-5.1` |

Instruction context for the agent is loaded from `AGENTS.md`. Permissions are set to `allow: "*"`.

## Wiki update workflow

`.github/workflows/update-wiki.yml`

Publishes documentation from `.wiki/` to the repository's GitHub Wiki.

| Property | Value |
|----------|-------|
| Trigger | `workflow_dispatch`, push to `main`, daily cron at `0 8 * * *` |
| Permissions | `contents: write`, `pull-requests: write` |

Steps:
1. Generate a GitHub App token (`APP_CLIENT_ID` + `APP_PRIVATE_KEY`), falling back to `GITHUB_TOKEN`.
2. Check out the repository.
3. Install Bun and Node.js 25.
4. Install `@chronova/wiki-agent` globally via Bun.
5. Run `wiki --update --print --verbose --wiki` with the model defaulting to `kimi-k2.7-code` (override via `vars.WIKI_MODEL`).
6. Detect whether the GitHub Wiki repository is initialized; warn and skip publishing if it is not.
7. If the wiki is initialized and content changed, clone the wiki repo, flatten `.wiki/` into it (including `Home.md` and `_Sidebar.md`), commit as `wiki-agent[bot]`, and push to `master`.
8. Open a staging PR with the `.wiki` changes if any content files changed.

Required secrets/vars:

| Name | Purpose |
|------|---------|
| `APP_CLIENT_ID` | GitHub App client ID for token generation |
| `APP_PRIVATE_KEY` | GitHub App private key |
| `WIKI_OLLAMA_API_KEY` | Ollama API key used by the wiki agent |
| `WIKI_PUSH_TOKEN` *(optional)* | Fallback token with `repo` scope for wiki pushes |
| `vars.WIKI_MODEL` *(optional)* | Override the default LLM model |

## Release drafter

`.github/release-drafter.yml`

Categorizes merged pull requests into release notes by label:

| Category | Labels |
|----------|--------|
| 🚀 Features | `feature`, `enhancement` |
| 🐛 Bug Fixes | `bug`, `fix` |
| 🧰 Maintenance | `chore`, `docs`, `documentation` |
| 🔄 Dependencies | `renovatebot` |

Version resolution:

| Bump | Labels |
|------|--------|
| Major | `priority: critical` |
| Minor | `feature`, `enhancement` |
| Patch | `bug`, `fix`, `chore`, `docs`, `documentation`, `renovatebot` |

Labels excluded from the draft: `needs-triage`, `needs-info`, `released`.

## Related pages

- [Build & Deploy](./build-and-deploy.md) — Gradle commands, signing, Docker.
- [Development Conventions](./development-conventions.md) — code rules enforced by the OMP reviewer.
- [AGENTS.md](../AGENTS.md) — Quick reference used by agents.
