---
type: ci-cd
title: Vouch System
description: PR gate that requires external contributors to be vouched by a maintainer before their PRs can land.
tags: [ci-cd, vouch, contributing, security]
---

# Vouch System

External contributors must be **vouched** by a maintainer before a pull
request can be merged. The system is implemented with two workflows and
the `mitchellh/vouch` GitHub Action, which checks
`.github/VOUCHED.td` against the PR author.

## Who is exempt

The vouch check skips:

- Users with write access on the repository.
- Bot accounts ending in `[bot]` (for example `renovate[bot]`,
  `chronova-agent[bot]`, `google-labs-jules[bot]`).

## `vouch-pr.yml`

| Field | Value |
|-------|-------|
| Name | Vouch (PR gate) |
| Trigger | `pull_request_target` opened / reopened / ready_for_review |
| Permissions | `contents: read`, `pull-requests: write`, `issues: write`, `id-token: write` |
| Concurrency | `vouch-pr-<pr-number>`, cancel-in-progress |

Steps:

1. Generate the GitHub App token.
2. `mitchellh/vouch/action/check-pr@v1` with `auto-close: true` and
   `require-vouch: true`. The action:
   - Reads `.github/VOUCHED.td`.
   - If the PR author is not vouched, the action auto-closes the PR and
     posts a comment explaining how to get vouched.
3. If the vouch check passes (`status: vouched` or `status: allowed`),
   the workflow:
   - Creates (or updates) the `vouched` label, colour `2da44e`.
   - Removes the label and re-adds it to ensure it is current.

## `vouch-manage.yml`

Maintainers manage the vouched list through **Discussion** comments. The
workflow reacts to comments containing the following commands:

| Command | Effect |
|---------|--------|
| `!vouch` | Vouches the discussion author. |
| `!vouch @user` | Vouches the specified user. |
| `!denounce @user` | Blocks the user from contributing. |
| `!unvouch @user` | Removes the user from the vouched list. |

The workflow updates `.github/VOUCHED.td` accordingly and commits the
change back to the repository.

## How a new contributor gets vouched

1. Open a **Discussion** describing the proposed contribution.
2. A maintainer comments `!vouch` on the discussion.
3. The vouch-manage workflow adds the user to `.github/VOUCHED.td`.
4. The contributor can then open PRs normally; `vouch-pr.yml` will see
   them as vouched and apply the `vouched` label.

See `CONTRIBUTING.md` for the canonical text and the bot exemption list.

## Security notes

- `pull_request_target` is used (not `pull_request`) so the action can
  post comments and close the PR. Workflows using this trigger must
  not check out untrusted code or expose secrets — the vouch workflow
  only reads repository metadata and `VOUCHED.td`.
- The GitHub App token (`actions/create-github-app-token`) is scoped to
  this repository and used instead of the default `GITHUB_TOKEN` for
  least-privilege.
- `id-token: write` is granted so the action can mint installation
  tokens as needed.
