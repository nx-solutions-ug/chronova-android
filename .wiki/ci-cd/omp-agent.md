---
type: ci-cd
title: OMP Agent
description: "The OMP (opencode agent) workflows: triage, label, review, and
  on-demand execution."
tags: [ ci-cd, omp, agent, automation, llm ]
last_updated: 2026-09-07T17:08:11.320Z
updated_by: wiki-agent
---

# OMP Agent

OMP ("opencode agent") is the project's LLM-driven automation. It is
installed in CI from `https://omp.sh/install`, configured with an
`ollama-cloud` provider, and run with the model
`ollama-cloud/glm-5.3-flash:max` (override via `WIKI_MODEL` for the wiki
pipeline). The agent's prompt templates live in `.omp/commands/`, and
its runtime is wrapped by `.omp/stream-log.py` for clean streaming logs.

The configuration (`.omp/config.yml`, `.omp/rules/`, `.omp/commands/`,
`.omp/stream-log.py`) is the agent's contract. Read it before adding new
commands or behaviour.

The workflows: `omp-ci.yml` (issue triage + PR labels), `omp-code-review.yml`
(dependency and code review), `omp.yml` (on-demand `/omp` comments), and
`omp-fix-issue.yml` (automated issue fixes).

## `omp-ci.yml`

This workflow runs the agent in two modes (PR **review** lives in its own
workflow, [`omp-code-review.yml`](#omp-code-reviewyml)). Each job has
its own concurrency group keyed on the issue/PR number.

### `triage-issue`

| Field | Value |
|-------|-------|
| Trigger | Issues opened; manual dispatch with `issue_number` |
| Concurrency | `omp-triage-<number>`, cancel-in-progress |
| Inputs | `${{ github.event.issue.number || github.event.inputs.issue_number }}` |
| Permissions | `contents: read`, `issues: write` |

Steps:

1. Check out (shallow, no persisted credentials).
2. Generate the GitHub App token.
3. Authenticate `gh` CLI.
4. React with 👀 on the issue.
5. Install Bun and the OMP CLI.
6. Insert the `ollama-cloud` credential into `~/.omp/agent/agent.db`.
7. `omp models refresh ollama-cloud > /dev/null 2>&1` to populate the model list.
8. Expand `.omp/commands/triage-issue.md` by replacing `$ARGUMENTS` with
the issue number, then run `omp --model ollama-cloud/glm-5.3-flash:max -p --mode json <prompt>`.
9. Stream the JSON through `.omp/stream-log.py` for a clean log.
10. On completion (success or failure), dispatch the `issue-triaged`
    repository event so `omp-fix-issue.yml` can pick it up.

### `label-pr`

| Field | Value |
|-------|-------|
| Trigger | PR opened / ready_for_review |
| Concurrency | `omp-label-<number>`, cancel-in-progress |
| Skip check | If the PR already has a `type` (`bug`, `feature`, `enhancement`, `docs`, `chore`) **and** a `priority:` label, the job short-circuits. |
| Command | `.omp/commands/label-pr.md` |

When the PR is **closed**, a `cancel-label-on-close` job cancels any
still-running `label-pr` run for that branch.

## `omp-code-review.yml`

The PR review agent lives in this dedicated workflow ("OMP Code
Review"). It has two jobs:

| Field | Value |
|-------|-------|
| Trigger | PR opened / synchronize / ready_for_review / review_requested; review submitted; review comment created; manual dispatch with `pr_number` |
| Concurrency | `omp-code-review-<number>`; cancel-in-progress for PR and dispatch events |
| Extensions | `agynio/gh-pr-review` (pinned v1.6.2) for inline comments |

### `dependency-review`

Runs only when the PR author is `renovate[bot]` or `dependabot[bot]`.
Runs `.omp/commands/dependency-review.md` and then verifies the agent
actually posted a review or comment — otherwise the job fails.

### `code-review`

Full code-quality review using `.omp/commands/review-pr.md`:

- **Triggers**: PR events (except `review_requested` from non-review
  flows), manual dispatch, and — as a retrigger — review submissions or
  review comments authored by "jules" (`google-labs-jules[bot]`).
- **Jules handling**: a detection step classifies the event as
  `jules-authored-pr`, `jules-review-submitted`, or
  `jules-review-comment` and passes it to the agent as context.
- **Skip rule (synchronize only)**: if the pushed head commit's
  author/committer matches an agent identity (`opencode-agent`,
  `opencode`, `github-actions`, `omp-agent`, `chronova-agent`), the
  re-review is skipped. `review_requested` never skips.
- **Checkout**: full history (`fetch-depth: 0`) so the diff against the
  base branch works for large PRs.
- **Verification**: after the review runs, the job counts unresolved
  review threads and agent reviews; if both are zero it fails the run
  (unless the PR modifies the review workflow itself, in which case
  verification is skipped by design).

## `omp.yml`

| Field | Value |
|-------|-------|
| Trigger | Issue comments created; PR review comments created |
| Skip check | Comments from `[bot]` users are ignored; the body must contain or start with `/omp` (or `/oc`) |
| Concurrency | `omp-agent-<number>`, cancel-in-progress: false |

The handler:

1. Extracts the prompt by stripping the leading `/omp` or `/oc`.
2. Looks for a command file at `.omp/commands/<first-word>.md`. If found,
   expands `$ARGUMENTS` with the rest of the comment.
3. If no command file matches, treats the comment as a freeform prompt.
   For PR comments it appends `.omp/commands/_pr-commit-push.md` so the
   agent commits and pushes its changes back to the PR branch instead
   of just staging them.
4. Runs `omp -p --model ollama-cloud/glm-5.3-flash:max --mode json <prompt>` and
   streams the result through `.omp/stream-log.py`.

The agent is **not** triggered by `[bot]` comments.

## `omp-fix-issue.yml`

Triggered by the `issue-triaged` repository_dispatch event sent at the
end of `triage-issue` (or manually with `issue_number`). It expands
`.omp/commands/fix-issue.md` to attempt an automated fix for the
triaged issue and opens a follow-up PR.

## Output streaming

`stream-log.py` consumes the agent's JSON-line stream and prints a
clean, prefixed log. It tolerates non-dict payloads and non-string text
fields so that a malformed intermediate event does not crash the run
(see the `fix(issue-76)` commit).

## Command templates

Templates live in `.omp/commands/`:

| File | Used by |
|------|---------|
| `triage-issue.md` | `omp-ci.yml` → `triage-issue` |
| `label-pr.md` | `omp-ci.yml` → `label-pr` |
| `review-pr.md` | `omp-code-review.yml` → `code-review` |
| `dependency-review.md` | `omp-code-review.yml` → `dependency-review` |
| `fix-issue.md` | `omp-fix-issue.yml` |
| `*.md` (any other) | `omp.yml` when the comment starts with `/omp <name>` |
| `_pr-commit-push.md` | Appended to freeform `/omp` prompts on PRs. |

Add a new command by dropping a Markdown file in `.omp/commands/`. Use
`$ARGUMENTS` for the runtime arguments. The agent will read the file and
follow it as instructions.

## Local development

The agent is only meaningful in CI. Reproducing it locally is not
necessary for normal development — most issues should not require
running the agent by hand.
