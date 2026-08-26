<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Jenkinsfile Companion Changelog

## [Unreleased]

## [0.1.1]

### Added

- Review/star CTA: after 10 distinct real findings across either rule
  (duplicate stage name, stage missing steps), a one-time notification
  asks whether to rate the plugin on Marketplace, with a permanent
  "Don't ask again" option. Standard mechanism used catalog-wide since
  2026-08-24, rolled out to this plugin now.

## [0.1.0]

### Added

- Groovy syntax highlighting for files named `Jenkinsfile` (no
  extension) via a filename-only `FileTypeOverrider`.
- Structural check: a stage name defined more than once.
- Structural check: a stage with no `steps`/`parallel`/`stages` block
  (a silent no-op).
- Per-rule toggles under Settings > Tools > Jenkinsfile Companion.

[Unreleased]: https://github.com/GapHunterLabs/jenkinsfile-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/jenkinsfile-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/jenkinsfile-companion/commits/0.1.0
