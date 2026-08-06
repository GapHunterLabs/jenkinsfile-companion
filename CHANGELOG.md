<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Jenkinsfile Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Groovy syntax highlighting for files named `Jenkinsfile` (no
  extension) via a filename-only `FileTypeOverrider`.
- Structural check: a stage name defined more than once.
- Structural check: a stage with no `steps`/`parallel`/`stages` block
  (a silent no-op).
- Per-rule toggles under Settings > Tools > Jenkinsfile Companion.

[Unreleased]: https://github.com/GapHunterLabs/jenkinsfile-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/jenkinsfile-companion/commits/0.1.0
