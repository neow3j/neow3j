# Agent Instructions

## Project structure
- This is a Java 8-targeted Gradle multi-project build. Check `settings.gradle` and the relevant module's `build.gradle` before changing dependencies or build behavior.
- `core` contains the SDK primitives and protocol code; `contract` builds on `core`; `devpack` provides smart-contract APIs; `compiler` compiles contracts. `int-tests` contains cross-module integration tests.
- Keep changes in the module that owns the behavior. Avoid introducing dependencies between modules without a clear need.

## Compatibility and correctness
- Treat public APIs, serialized formats, transaction construction, and smart-contract syscall behavior as compatibility-sensitive.
- Before changing or removing a public API, check its callers and document intentional breaking changes.
- Prefer focused changes and tests that cover both the expected behavior and relevant failure cases.
- Do not silently guess transaction fees, signer capabilities, or contract behavior when required information is missing. Make assumptions explicit and report limitations.

## Development and tests
- Read `README.md` for setup, Java requirements, and the standard test commands.
- Run the narrowest relevant Gradle test task; report exactly what ran and what did not.
- Integration tests in `int-tests` use Testcontainers and require a Docker environment accessible to the Gradle process. A failure during container initialization is an environment/setup failure, not a test assertion failure.
- Preserve the repository's existing Java style and build conventions. There is no configured automatic formatter; consult `.idea` style settings when relevant.
- Do not run publishing, signing, release, or deployment tasks unless explicitly requested.

## Git and attribution
- Name task branches with a lowercase type prefix and kebab-case description, using prefixes such as `feat/`, `fix/`, `chore/`, `docs/`, `refactor/`, or `test/`. When a GitHub issue exists, include its number, e.g., `feat/1175-stackitem-script-hash`.
- Use Conventional Commits for commit subjects: `<type>(<optional-scope>): <short imperative summary>`. Use the GitHub issue reference at the end when applicable, e.g., `feat: add StackItem script hash accessor (#1175)`.
- Keep the scope attached to the type with no space, e.g., `chore(minor): update dependencies (#1200)`. Do not invent issue numbers; omit the reference if there is no associated issue.
- Do not create commits, amend commits, or rewrite history unless explicitly requested.
- Preserve the configured Git author. Do not add AI-tool co-author or attribution trailers unless the user explicitly requests them.
- For an authorized commit, inspect the final message for unintended attribution trailers.
- Inspect `git status` before editing. Preserve user changes and untracked files; do not discard unrelated work.
