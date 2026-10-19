---
name: tracker-review
description: >-
  Reviews the local FeedTracker branch by running the same Gradle checks as
  .github/workflows/ci.yml (ktlint, Android Lint, JVM, Android unit, and iOS
  simulator tests), then reviews the branch diff for bugs and vulnerabilities.
  Use when the user asks for tracker-review, a local branch review, or a CI
  plus security review of the current branch. Writes the report in chat only.
disable-model-invocation: true
---

# tracker-review

Review the current local branch. Run every check from the CI workflow, then review the diff for defects and vulnerabilities. Deliver one markdown report in chat. Do not create or edit files for the report.

## Scope

1. Read `.github/workflows/ci.yml` and extract every Gradle (or other) command the jobs run. Prefer those commands over any list remembered from an older version of this skill.
2. Review committed and uncommitted changes on the current branch against its base. Default base is `develop`. If the branch is `develop` or `main`, review uncommitted changes plus commits not on `origin/develop` when that remote ref exists; otherwise review the working tree against `HEAD`.
3. Skip the Release workflow (keystore, AAB, desktop packaging). It is not CI.

## CI checks

Run from the repo root. JDK 21. Use long timeouts (`block_until_ms` at least 600000 per Gradle invocation). Request permissions if the sandbox blocks the Android SDK or Gradle.

Run, in order, the commands from `ci.yml`. As of this skill they are:

```bash
./gradlew ktlintCheck --stacktrace
./gradlew :composeApp:lintDebug :shared:lintDebug --stacktrace
./gradlew :server:test :shared:jvmTest :shared:testDebugUnitTest :composeApp:jvmTest :composeApp:testDebugUnitTest --stacktrace
```

On macOS (darwin), also run the iOS job:

```bash
./gradlew :shared:iosSimulatorArm64Test :composeApp:iosSimulatorArm64Test --stacktrace
```

On other hosts, record iOS tests as not run and why. Do not substitute a different task.

If `ci.yml` adds or removes a check, run the workflow file, not this snapshot.

On failure, read the Gradle output and, when present, `**/build/reports/tests/**`, `**/build/reports/ktlint/**`, and `**/build/reports/lint-results*`. Continue the remaining checks and the code review. Do not fix code unless the user asks.

## Code and vulnerability review

Review the diff (`git diff <base>...HEAD` and unstaged/untracked source). Focus on behavior the change introduces or touches:

- Auth, tokens, sessions, password handling, SQL, path traversal, insecure deserialization, secrets in source, overly broad network or logging of credentials
- Correctness, error handling, concurrency, data loss, migrations
- Missing tests for risky behavior

Ignore style already enforced by ktlint unless the check failed. Do not invent issues. Cite `path:line` for each finding.

## Severity

| Level | Use when |
| --- | --- |
| blocking | CI check failed, or a defect/vulnerability that is unsafe to merge (auth bypass, data loss, injection, secret leak) |
| major | Real bug or security weakness with a plausible trigger, or missing tests around security-sensitive logic |
| minor | Limited impact, edge case, or maintainability issue a reviewer would still mention |

A green CI run can still have blocking findings. A red CI run is at least one blocking finding per failed check, with the failing task and the first actionable error.

## Report

Reply with one markdown message. No new files. Use this structure:

```markdown
# tracker-review

**Branch:** `<name>` vs `<base>`
**CI:** passed | failed

## Checks

| Check | Result | Notes |
| --- | --- | --- |
| ktlintCheck | pass/fail/skipped | |
| Android Lint | pass/fail/skipped | |
| JVM and Android unit tests | pass/fail/skipped | |
| iOS simulator tests | pass/fail/skipped | |

## Summary

<2–4 sentences: what was reviewed and the overall risk.>

## Issues

| Severity | Issue | Where | Proposed solution |
| --- | --- | --- | --- |
| blocking | <what is wrong and why it matters> | `path:line` | <concrete fix> |
| major | ... | ... | ... |
| minor | ... | ... | ... |

Order rows blocking, then major, then minor. If a section level has no findings, omit those rows and say so in the summary.
```

Keep each proposed solution to one or two sentences a developer can apply. Do not include exploit steps, payloads, or reproduction commands for a vulnerability; name the weakness and the fix.
