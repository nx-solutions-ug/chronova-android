---
type: contributing
title: Contributing
description: How to contribute to Chronova Android, including the vouch system and maintainer commands.
tags: [contributing, vouch, pr, discussions]
---

# Contributing

Pull requests are welcome, but they must pass a lightweight **vouch gate** before they can be opened.

## Who can open pull requests

- **Vouched contributors** listed in `.github/VOUCHED.td`.
- **Bots** whose username ends with `[bot]`.
- **Collaborators with write access** to the repository.

This means common automation accounts — such as `renovate[bot]`, `chronova-agent[bot]`, and `google-labs-jules[bot]` — can open PRs without being individually vouched.

## How to get vouched

1. Open a **Discussion** in the repository's Discussions tab.
2. Describe what you plan to contribute.
3. A maintainer will review it and add you to the vouched list by commenting `!vouch` on the discussion.

Once vouched, you can open pull requests normally.

## Vouch commands (maintainers only)

Maintainers comment these commands on a discussion. Only users with `admin`, `maintain`, or `write` roles are honored.

| Command | Effect |
|---------|--------|
| `!vouch` | Vouch the author of the discussion. |
| `!vouch @user [reason]` | Vouch a specific user. |
| `!denounce [@user] [reason]` | Block a user from opening PRs. |
| `!unvouch [@user]` | Remove a user from the vouched list. |

The vouched list lives in `.github/VOUCHED.td`. It is one handle per line, sorted alphabetically. To denounce a user, prefix the line with `-`.

## What happens when you open a PR

The `.github/workflows/vouch-pr.yml` workflow runs on `pull_request_target` events (`opened`, `reopened`, `ready_for_review`). It checks whether the PR author is vouched, a bot, or a write-access collaborator. If not, the PR is automatically closed. Vouched or otherwise allowed PRs receive the `vouched` label.

The `.github/workflows/vouch-manage.yml` workflow listens for `discussion_comment` events and applies maintainer `!vouch`, `!denounce`, and `!unvouch` commands, updating `.github/VOUCHED.td` automatically.

See the full rules in [`CONTRIBUTING.md`](../CONTRIBUTING.md).