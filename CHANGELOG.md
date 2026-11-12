# Changelog

All notable changes to Ruhani v2 are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Fixed

- Bump `versionCode` to 2 and `versionName` to `2.0.1` in `app/build.gradle.kts`, so the in-app update dialog no longer appears on every launch of the latest release.
- Show the real app version in Settings (`BuildConfig.VERSION_NAME`) instead of a hardcoded `v1.0`.

### Added

- `.gitlab-ci.yml` pipeline that builds the debug APK and keeps it as an artifact.

### Changed

- Restore `.github/workflows/build-apk.yml`, which was accidentally removed in `4771f10`.

## [2.0.1] - 2026-11-12

### Added

- KhansCreations branding: logo, company welcome popup, persistent footer bar, and branding cards on Home, More and Settings screens.
- In-app update checker that reads the latest GitHub release and shows an "Update Available" dialog.
- Developer Experience (DevEx) audit report (`report.md`).
- App screenshots in the repository and showcased in the README.
- MIT `LICENSE`.
- Adhan library (Batoul Apps) integration for prayer time calculation.
- Redesigned Tasbih counter UI.
- GitHub Actions workflow to build the debug APK.
- Hisnul Dua API integration.
- Design system and app documentation (color palettes, typography, PRD, feature matrix).
- District search filter.

### Changed

- Modernized UI to match the reference design, with responsive fit-to-screen layouts and a 2-column grid in the More hub.
- Updated the Qari catalog and Dua data structure.
- Hid navigation bars on the Qibla screen.
- Renamed `WordByWordItem` properties to better reflect their intent.

### Fixed

- Improved contrast of the Vibrant Light theme.

## [1.0] - 2026-11-10

### Added

- Initial release of Ruhani: Quran reader with audio, prayer times, Qibla, Tasbih, Dua & Dhikr, and Google Sign-In with Firestore sync.

[Unreleased]: https://gitlab.com/khanscreations3/ruhani_v2/-/compare/v2.0.1...main
[2.0.1]: https://gitlab.com/khanscreations3/ruhani_v2/-/compare/v1.0...v2.0.1
[1.0]: https://gitlab.com/khanscreations3/ruhani_v2/-/commits/v1.0
