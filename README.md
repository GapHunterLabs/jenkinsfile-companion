# Jenkinsfile Companion

IntelliJ-family plugin. Real Groovy syntax highlighting for
`Jenkinsfile` plus two genuine structural checks — 100% local, zero
network calls, never a live Jenkins server connection.

## Why it exists

Born from real evidence in JetBrains Marketplace reviews, not
assumptions. Two independent signals in this exact space:

- The leading PAID Jenkinsfile plugin (actively updated) has 31% of
  its reviews at 3 stars or fewer: *"Just by installing the plugin the
  pop-ups on hover in the editor stopped working for any language"*,
  *"code formatting in the Jenkinsfile doesn't work"*, and one paying
  user cancelled their subscription over it: *"it was, ironically,
  causing more problems with Jenkinsfiles... than it was solving."*
- A separate, abandoned FREE competitor ("Jenkins Pipeline Linter")
  has a real design flaw at its core: it validates by connecting to a
  **live Jenkins server**, requiring credentials, before it will even
  check syntax — *"All I wanted was to validate a jenkinsBuild file
  but that won't work without a connection and credentials to a
  jenkins instance. Why does it need that?"* Exactly the class of
  fragility gitlab-ci-companion (elsewhere in this catalog) already
  exists to avoid, applied here to a different CI system.

## Why built this way

- **Groovy PSI, not a custom parser.** A Jenkinsfile is Groovy code
  with a specific DSL (`pipeline { agent any; stages { stage('X') {
  steps { ... } } } }` is really `pipeline({ ... })` using Groovy's
  trailing-closure syntax). This plugin builds directly on top of the
  IDE's own bundled Groovy support
  (`org.jetbrains.plugins.groovy.lang.psi.*`) rather than hand-rolling
  a lexer for a full general-purpose language — the same "build on
  bundled PSI, don't reinvent it" call gitlab-ci-companion makes for
  YAML.
- **A `FileTypeOverrider` makes `Jenkinsfile` (no extension) resolve
  to Groovy at all** — IntelliJ has no built-in association for an
  extensionless file named exactly "Jenkinsfile", so without this,
  none of the IDE's Groovy tooling (or this plugin's own checks) would
  ever activate on it. Filename-only, never content-sniffed — avoids
  the `contentsToByteArray()` re-entrancy trap documented elsewhere in
  this catalog (`ansible-companion/KNOWN_ISSUES.md`).
- **No custom formatter, no custom hover provider.** The cited
  competitor's own bugs are specifically about interfering with
  unrelated IDE features (breaking hover for every language, breaking
  formatting) — this plugin deliberately never touches either surface,
  so it cannot reproduce that exact failure mode.
- **Two checks, both genuinely structural facts, not guesses:** a
  stage name defined more than once (confusing at best — which run in
  the Jenkins UI is "Build"?), and a stage with none of a `steps {}`,
  a nested `stages {}`/`parallel {}` block, or a `matrix {}` block (a
  real, silent no-op Jenkins itself won't warn about).
- **v1 scope cuts, deliberate:** Declarative Pipeline syntax only
  (`pipeline { ... }`); Scripted Pipeline (`node { ... }`) is a
  materially different, more free-form structure and is out of scope
  for now. No live pipeline status (already built, separately, as an
  opt-in feature of gitlab-ci-companion for GitLab — a Jenkins
  equivalent would need its own real API research, not assumed).

## Usage

Open any file named exactly `Jenkinsfile` (or ending in
`.jenkinsfile`) — Groovy syntax highlighting and structural warnings
apply automatically. Disable individual checks under Settings > Tools
> Jenkinsfile Companion.

## Enterprise / Team Licensing

Need enterprise features, custom pipeline validation rules, or team
licensing? Contact us at **gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
