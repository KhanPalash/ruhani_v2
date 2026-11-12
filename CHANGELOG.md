# Changelog

All notable changes to Ruhani v2 are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Security

- Removed the global `android:usesCleartextTraffic="true"` flag and added `res/xml/network_security_config.xml`. Cleartext is now denied by default for every host, with cleartext allowed only for the loopback/emulator addresses used by the Firebase emulator in local development. Every production endpoint the app talks to is already HTTPS.
- Removed the unused `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` and `WAKE_LOCK` permissions, plus the `android.hardware.location.gps` feature declaration. Prayer times and Qibla are derived from the built-in district catalogue, so the app never requests device location.
- Documented the exact-alarm permission choice in the manifest: the app keeps `SCHEDULE_EXACT_ALARM` (user-grantable, and the correct fit for prayer-time alerts) rather than `USE_EXACT_ALARM`, which Google Play restricts to alarm-clock and calendar apps.

### Added

- Settings card and prayer-screen banner that surface the Android 12+ exact-alarm grant, with a direct link to the system grant screen. Previously the app silently degraded to inexact alarms, which could delay an azan notification by several minutes.
- `ExactAlarmPermissionTest` unit tests covering the exact-alarm grant check, the grant-screen intent, and the exact-to-inexact alarm fallback.
- GitHub Actions `unit_tests` and `lint` jobs, mirroring the GitLab CI stages, with the APK build now gated on both.

### Fixed

- Enabled core library desugaring (`com.android.tools:desugar_jdk_libs`) so the `java.time` APIs used by the calendar and date-converter screens work on Android 7.0/7.1. Previously those calls threw `NoClassDefFoundError` on API 24-25 devices, and `lintDebug` failed with 39 `NewApi` errors, which left the CI lint stage permanently red.
- Bump `versionCode` to 2 and `versionName` to `2.0.1` in `app/build.gradle.kts`, so the in-app update dialog no longer appears on every launch of the latest release.
- Show the real app version in Settings (`BuildConfig.VERSION_NAME`) instead of a hardcoded `v1.0`.

### Changed

- Restore `.github/workflows/build-apk.yml`, which was accidentally removed in `4771f10`.
- `.gitlab-ci.yml` now runs `testDebugUnitTest` and `lintDebug` stages before the APK build (previously build only).
- Prayer alerts now re-arm with exact timing as soon as the user grants the exact-alarm permission, instead of staying on the inexact fallback until the next reboot.
- Renamed the source namespace from the `com.example` placeholder to `com.khanscreations.ruhani`, and moved the sources to `app/src/*/java/com/khanscreations/ruhani/`.
- Changed `applicationId` from `com.aistudio.ruhani.kxmzpq` to `com.khanscreations.ruhani` so the installed package matches the source package. **Breaking for existing installs:** Android treats a new application ID as a different app, so users must uninstall and reinstall, which clears on-device state (settings, offline downloads, notification history, tasbih counters). Firebase Auth accounts and Firestore data survive as long as the new package is registered as a second Android app in the same Firebase project. Google Sign-In needs an OAuth client for the new package with the same SHA-1 fingerprint.
- Upgraded the google-services Gradle plugin from 4.5.0 to 4.6.0.
- Raised the unit-test task timeout from 2 to 10 minutes. The suite needs ~40s of test time, but a cold run (first Robolectric sandbox, first Firebase init) exceeds two minutes and was tripping the old cap, making CI flaky.

### Security

- Added `google-services.json` and `my-upload-key.jks`/`*.jks` to `.gitignore`, and removed a stray `-e` pattern. Neither the Firebase config nor the signing material was previously ignored, so a `git add .` would have committed them.

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
