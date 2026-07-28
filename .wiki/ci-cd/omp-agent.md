---
type: ci-cd
title: OMP Agent
description: The OMP (opencode agent) workflows: triage, label, review, and on-demand execution.
tags: [ci-cd, omp, agent, automation, llm]
---

# OMP Agent

OMP ("opencode agent") is the project's LLM-driven automation. It is
installed in CI from `https://omp.sh/install`, configured with an
`ollama-cloud` provider, and run with the model
`ollama-cloud/minimax-m3` (override via `WIKI_MODEL` for the wiki
pipeline). The agent's prompt templates live in `.omp/commands/`, and
its runtime is wrapped by `.omp/stream-log.py` for clean streaming logs.

The configuration (`.omp/config.yml`, `.omp/rules/`, `.omp/commands/`,
`.omp/stream-log.py`) is the agent's contract. Read it before adding new
commands or behaviour.

## `omp-ci.yml`

This workflow runs the agent in three modes. Each is a separate job with
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
   the issue number, then run `omp --model ollama-cloud/minimax-m3 -p --mode json <prompt>`.
9. Stream the JSON through `.omp/stream-log.py` for a clean log.
10. On completion (success or failure), dispatch the `issue-triaged`
    repository event so `omp-fix-issue.yml` can pick it up.

### `label-pr`

| Field | Value |
|-------|-------|
| Trigger | PR opened / synchronize / ready_for_review |
| Concurrency | `omp-label-<number>`, cancel-in-progress |
| Skip check | If the PR already has a `type` (`bug`, `feature`, `enhancement`, `docs`, `chore`) **and** a `priority:` label, the job short-circuits. |
| Command | `.omp/commands/label-pr.md` |

### `review-pr`

| Field | Value |
|-------|-------|
| Trigger | PR opened (and subsequent non-closed events); manual dispatch with `pr_number` |
| Concurrency | `omp-review-<number>`, cancel-in-progress: false (reviews should not cancel) |
| Skip check (synchronize only) | If the head commit's author/committer matches an agent identity (`opencode-agent`, `opencode`, `github-actions`, `omp-agent`, `chronova-agent`), the re-review is skipped. |
| Prefix | For Dependabot/Renovate authors the prompt is prefixed `dep:`; for `[bot]` or `opencode-agent` authors it is `bot:`; otherwise no prefix. |
| Extensions | `agynio/gh-pr-review` for inline comments. |

The review command is selected by `${{ steps.review-type.outputs.prefix }}<cmd>.md`
from `.omp/commands/`.

## `omp.yml`

Triggered when a comment on an issue or PR contains `/omp` (or `/oc`).
The handler:

1. Extracts the prompt by stripping the leading `/omp` or `/oc`.
2. Looks for a command file at `.omp/commands/<first-word>.md`. If found,
   expands `$ARGUMENTS` with the rest of the comment.
3. If no command file matches, treats the comment as a freeform prompt.
   For PR comments it appends `.omp/commands/_pr-commit-push.md` so the
   agent commits and pushes its changes back to the PR branch instead
   of just staging them.
4. Runs `omp -p --model ollama-cloud/minimax-m3 --mode json <prompt>` and
   streams the result through `.omp/stream-log.py`.

The agent is **not** triggered by `[bot]` comments.

## `omp-fix-issue.yml`

Triggered by the `issue-triaged` repository_dispatch event sent at the
end of `triage-issue`. It runs an agent command to attempt an automated
fix for the triaged issue and opens a follow-up PR.

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
| `review-pr.md` (plus `dep:` / `bot:` variants) | `omp-ci.yml` → `review-pr` |
| `*.md` (any other) | `omp.yml` when the comment starts with `/omp <name>` |
| `_pr-commit-push.md` | Appended to freeform `/omp` prompts on PRs. |

Add a new command by dropping a Markdown file in `.omp/commands/`. Use
`$ARGUMENTS` for the runtime arguments. The agent will read the file and
follow it as instructions.

## Local development

The agent is only meaningful in CI. Reproducing it locally is not
necessary for normal development — most issues should not require
running the agent by hand.
