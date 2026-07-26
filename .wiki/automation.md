---
type: automation
title: Automation & CI/CD
description: GitHub Actions workflows, the OMP agent, vouch gates, release drafting, and the wiki update pipeline.
tags: [automation, ci, cd, omp, vouch, renovate]
---

# Automation & CI/CD

Chronova Android uses GitHub Actions for builds, dependency updates, contributor gating, agent-assisted triage/reviews, and wiki publishing.

## Workflow inventory

All workflows live in `.github/workflows/`.

| Workflow | File | Trigger | Purpose |
|----------|------|---------|---------|
| Build | `build.yml` | `workflow_dispatch` | Runs unit tests and builds debug + release APKs, uploading artifacts. |
| Auto Manage | `auto-manage.yml` | Issues/PRs opened/reopened | Tags new issues `needs-triage` and auto-assigns issues/PRs to `niklasschaeffer`. |
| OMP CI | `omp-ci.yml` | Issues/PRs opened + `workflow_dispatch` | Triage, label, and review PRs using the OMP agent. |
| OMP Fix Issue | `omp-fix-issue.yml` | `repository_dispatch` (`issue-triaged`) + `workflow_dispatch` | Attempts an automated fix for a triaged issue. |
| OMP On-Demand | `omp.yml` | Comments containing `/omp` | Runs the OMP agent on demand via issue/PR comments. |
| Vouch PR Gate | `vouch-pr.yml` | `pull_request_target` opened/reopened/ready_for_review | Closes PRs from unvouched users and labels vouched PRs. |
| Vouch Manage | `vouch-manage.yml` | Discussion comments | Lets maintainers vouch/denounce/unvouch users via `!vouch` / `!denounce` / `!unvouch`. |
| Wiki Update | `update-wiki.yml` | `push` to `main`, schedule, `workflow_dispatch` | Runs the wiki agent, publishes to the wiki repo, and opens a staging PR if needed. |

## Build workflow

`build.yml` is the only Android build workflow. It:

1. Checks out the repository.
2. Sets up JDK 17 (Temurin) and the Android SDK.
3. Caches Gradle packages.
4. Runs `./gradlew testDebugUnitTest --stacktrace`.
5. Builds `./gradlew assembleDebug` and `./gradlew assembleRelease`.
6. Uploads artifacts:
   - `app-debug` (7-day retention)
   - `app-release` (30-day retention)
   - `test-results` (7-day retention)

It has a 30-minute timeout and uses `concurrency` to cancel in-progress runs on the same ref.

## Dependency management

Renovate is configured in `renovate.json` with the `config:recommended` preset. It opens PRs for dependency and GitHub Action updates. The repository currently has open Renovate PRs for `actions/checkout`, `actions/cache`, Material, OkHttp, Lifecycle, Kotlin, and Gradle wrapper updates.

## Vouch contributor gate

The repository restricts PRs to vouched users, bots, and collaborators with write access.

### `vouch-pr.yml`

Runs on `pull_request_target` so it can act on fork PRs. It uses `mitchellh/vouch/action/check-pr@v1` with `auto-close: true` and `require-vouch: true`. If the PR passes, it creates/applies a `vouched` label.

### `vouch-manage.yml`

Runs on `discussion_comment` created events. Maintainers with admin/maintain/write roles can comment on a Discussion:

- `!vouch` — vouch the discussion author.
- `!vouch @user [reason]` — vouch a specific user.
- `!denounce [@user] [reason]` — remove a vouch.
- `!unvouch [@user]` — remove a vouch.

The vouched list is stored in `.github/VOUCHED.td`, which explains the syntax and how to request a vouch.

## Release Drafter

`.github/release-drafter.yml` categorizes merged PRs into:

- 🚀 Features (`feature`, `enhancement`)
- 🐛 Bug Fixes (`bug`, `fix`)
- 🧰 Maintenance (`chore`, `docs`, `documentation`)
- 🔄 Dependencies (`renovatebot`)

Version resolution uses labels for major/minor/patch bumps and excludes `needs-triage`, `needs-info`, and `released`.

## OMP agent

The OMP agent is an external agent runtime installed in CI via `curl -fsSL https://omp.sh/install | sh`. It is authenticated against an ollama-cloud provider using `OLLAMA_API_KEY` and uses models defined in `.omp/agent/config.yml` and `.omp/config.yml`.

### Configuration

- `.omp/config.yml` — top-level config: default model, per-task model roles, GitHub/vision settings, and the instruction source (`AGENTS.md`).
- `.omp/agent/config.yml` — agent-level model mappings (`build`, `explore`, `general`, default).
- `.omp/rules/` — runtime behavior rules:
  - `gh-label-idempotent.md` — append `|| true` to `gh label create` to suppress 422 errors.
  - `tool-paths-must-be-arrays.md` — `find`/`search` `paths` must be an array of strings.

### Command templates

Templates in `.omp/commands/` are expanded by replacing `$ARGUMENTS` with the provided issue/PR number.

| Template | Used by | Purpose |
|----------|---------|---------|
| `triage-issue.md` | `omp-ci.yml` `triage-issue` job | Reads issue, classifies type/priority, applies labels, posts summary, dispatches `issue-triaged`. |
| `fix-issue.md` | `omp-fix-issue.yml` | Reads issue, checks actionability, creates a branch, implements a minimal fix, runs quality gates, opens a PR. |
| `label-pr.md` | `omp-ci.yml` `label-pr` job | Reads PR and diff, applies one type and one priority label if missing. |
| `review-pr.md` | `omp-ci.yml` `review-pr` job | Reviews dependency, bot, or human PRs and posts a review or dependency summary comment. |

### `stream-log.py`

`.omp/stream-log.py` formats OMP JSONL output into human-readable CI log lines. It is piped after `omp -p --mode json` calls in the OMP workflows.

### OMP CI behavior

`omp-ci.yml` contains three jobs:

- `triage-issue` — classifies a new issue, sets type/priority, applies labels, and always dispatches `omp-fix-issue` via `repository_dispatch` (event type `issue-triaged`).
- `label-pr` — skips if the PR already has both a type and priority label; otherwise labels it.
- `review-pr` — determines review type (dependency / bot / human), skips re-review for agent-authored synchronize commits, and posts a review.

## Wiki update pipeline

`update-wiki.yml` runs the Chronova wiki agent on pushes to `main`, daily at 08:00 UTC, and on manual dispatch.

Steps:

1. Generates an app token or falls back to `GITHUB_TOKEN`.
2. Checks out the repository.
3. Sets up Bun and Node.js 25.
4. Installs `@chronova/wiki-agent` globally.
5. Runs `wiki --update --print --verbose --wiki` with the configured model (`WIKI_MODEL` defaults to `kimi-k2.7-code`).
6. Detects `.wiki` content changes (ignoring `.last-update-report.md` and `.last-updated.json`).
7. If the GitHub Wiki repo is initialized, it publishes flattened content to `https://github.com/{owner}/{repo}.wiki.git`.
8. Creates a staging PR (`wiki/staging-<timestamp>`) containing the `.wiki` changes so the source repo retains a snapshot of the docs.

The wiki agent is expected to follow the same AGENTS.md conventions and should not edit files outside `.wiki/`.

## Interaction summary

1. A new issue is opened → `auto-manage.yml` labels it `needs-triage` and assigns a maintainer.
2. `omp-ci.yml` `triage-issue` classifies it and dispatches `issue-triaged`.
3. `omp-fix-issue.yml` attempts an automated fix, runs quality gates, and opens a draft PR.
4. `omp-ci.yml` `label-pr` and `review-pr` label/review the PR.
5. A human maintainer merges after review.
6. `update-wiki.yml` refreshes the wiki to reflect any source/docs changes.

For project conventions that the OMP agent enforces, see [Development Conventions](./development-conventions.md). For manual build instructions, see [Build & Deploy](./build-and-deploy.md).
