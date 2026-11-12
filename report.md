# Project Review Report: Ruhani v2

**Project:** [Ruhani v2](https://gitlab.com/khanscreations3/ruhani_v2)
**Date:** November 12, 2026
**Scope:** Architecture, code quality, security, build/CI, documentation
**Stack:** Kotlin, Jetpack Compose, Material 3, Firebase (Firestore + Auth), Adhan

---

## Executive Summary

Ruhani v2 is a single-module Android app (`com.khanscreations.ruhani`) offering Quran reading, prayer times, Qibla, Tasbih, Dua & Dhikr, and cloud sync with full Bangla support. The codebase is well organized into `data` / `domain` / `ui` layers, has real automated test coverage, hardened Firestore rules, and disciplined Conventional Commits. The main risks are a monolithic ViewModel and release build hygiene (no minification/shrinking); the generic `com.example` namespace and globally-enabled cleartext traffic have since been fixed.

**Overall score: 7.2 / 10 (Grade: B)**

Most P0 items from the prior DevEx audit are now resolved: version is bumped to `2.0.1` (`versionCode = 2`), `.gitlab-ci.yml` builds the debug APK, and `LICENSE` + `CHANGELOG.md` exist.

---

## Scorecard

| # | Dimension | Score | Status |
|---|---|:---:|---|
| 1 | Architecture & structure | 7.5 / 10 | Clean layering; ViewModel too large |
| 2 | Code quality & ergonomics | 7.0 / 10 | Good patterns; a few monolith files |
| 3 | Security | 6.0 / 10 | Strong Firestore rules; manifest/build gaps |
| 4 | Build & release config | 6.5 / 10 | Works; release not minified/shrunk |
| 5 | CI/CD | 7.0 / 10 | Debug APK pipeline; no test/lint stage |
| 6 | Testing & QA | 7.5 / 10 | Unit + Robolectric + Espresso present |
| 7 | Documentation | 8.0 / 10 | Strong README, CHANGELOG, LICENSE |

---

## Strengths

1. **Clean layered architecture.** Source is split into `data/` (model, remote, repository), `domain/` (prayer engine, catalogs, schedulers), and `ui/` (ViewModel, screens, theme), which keeps concerns separated and discoverable.
2. **Hardened Firestore rules.** `firestore.rules` uses a global default-deny, per-entity validation blueprints, owner-only access, field allow-lists (`hasAll` / `hasOnly`), and immutability checks on `createdAt` and identity fields. This is notably above average for a hobby/indie app.
3. **Real test coverage.** `app/src/test` contains unit, Robolectric, and a prayer-notification/data test; `androidTest` has instrumented coverage. `failFast`, a 2-minute per-test timeout, and offline Robolectric config are set in `app/build.gradle.kts`.
4. **Graceful Firebase fallback.** `missingGoogleServicesStrategy = WARN` plus `googleServices.missing.passthrough=true` let contributors build without `google-services.json`.
5. **Documentation and release hygiene.** README is thorough, `CHANGELOG.md` follows Keep a Changelog + SemVer, and secrets are managed via `.env` / `.env.example` through the Secrets Gradle plugin.

---

## Issues & Recommendations

### P1 - Monolithic ViewModel
`app/src/main/java/com/example/ui/RuhaniViewModel.kt` is the single source of truth for prayer calc, Tasbih, Quran audio, Firestore sync, update checks, and dialog state. Split into focused delegates: `PrayerViewModel`, `QuranAudioViewModel`, `TasbihViewModel`, `SyncAndAccountViewModel`.

### P1 - Release build not minified or shrunk
In `app/build.gradle.kts` the `release` type sets `isMinifyEnabled = false` and `proguard-rules.pro` is effectively empty. Enable R8 (`isMinifyEnabled = true`, `isShrinkResources = true`) and add keep rules for Firebase/Adhan models to reduce APK size and strip unused code.

### P1 - Generic application namespace — **FIXED**
The `com.example` placeholder namespace is gone. `namespace` is now `com.khanscreations.ruhani`, and the 39 Kotlin source files were moved from `app/src/*/java/com/example/` to `app/src/*/java/com/khanscreations/ruhani/` with their package and import statements updated.

`applicationId` was also changed from `com.aistudio.ruhani.kxmzpq` to `com.khanscreations.ruhani`, so the source package and the installed package now agree and the AI-Studio-generated placeholder prefix is gone.

**This is a breaking change for existing installs.** Android treats a different application ID as a different app, so the new build cannot update `com.aistudio.ruhani.kxmzpq` in place — users must uninstall and reinstall, which discards on-device state (SharedPreferences, offline surah downloads, notification vault, tasbih counters). Auth accounts and Firestore documents live server-side in the same Firebase project and survive, provided the new package is registered as a second Android app in that project and a fresh `google-services.json` is downloaded. Google Sign-In needs an OAuth client for the new package with the same SHA-1 fingerprint.

### P2 - Cleartext traffic globally enabled — **FIXED**
`AndroidManifest.xml` previously set `android:usesCleartextTraffic="true"`. The flag has been removed and replaced with a scoped `app/src/main/res/xml/network_security_config.xml`: cleartext is denied for every host by default, and is permitted only for `localhost` / `127.0.0.1` / `10.0.2.2`, which the Firebase emulator suite uses in local development. Every production endpoint (Quran API, AlAdhan, Hisnul Muslim, Bangla Hadith, GitHub releases, Firebase) was already HTTPS, so no behaviour changed.

### P2 - Sensitive/permission-heavy manifest — **FIXED**
`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` and `WAKE_LOCK` are now removed, along with the `android.hardware.location.gps` feature declaration. The app never requested location at runtime: prayer times and the Qibla bearing are derived from the built-in district catalogue (fixed coordinates), and `play-services-location` is not even on the classpath. `RECEIVE_BOOT_COMPLETED` is kept and justified — `BootReceiver` re-arms the prayer alarms after reboot, package replace, or a clock/timezone change.

On exact alarms, the app keeps `SCHEDULE_EXACT_ALARM` and does **not** move to `USE_EXACT_ALARM`: the latter is auto-granted but Google Play restricts it to alarm-clock and calendar apps, while a prayer app's core function depends on a precise user-facing timer, which is exactly what `SCHEDULE_EXACT_ALARM` covers. The permission is user-grantable, so the app now asks for it rather than assuming it:

- `PrayerAlarmScheduler.canScheduleExactAlarms(context)` / `exactAlarmSettingsIntent(context)` expose the grant state and the system grant screen.
- A Settings card ("Prayer Time Alerts") shows the status and links straight to the grant screen.
- A warning banner appears on the prayer schedule when azan alerts are on but exact alarms are not yet granted.
- `ExactAlarmPermissionTest` verifies the grant check, the intent, and the exact → inexact fallback.

### P2 - CI runs build only — **FIXED**
`.gitlab-ci.yml` already had `test` (`testDebugUnitTest`) and `lint` (`lintDebug`) stages added in `82838de`; the report above was written against an earlier revision. The remaining gap was the GitHub Actions workflow, which only built. `.github/workflows/build-apk.yml` now has matching `unit_tests` and `lint` jobs, and the APK build job depends on both, so a failing test or lint run blocks the artifact. The CI-generated `debug.keystore` remains debug-only and is never reused for release.

Turning on the lint stage also exposed that it was **red on `main`**: `lintDebug` failed with 39 `NewApi` errors, because the app calls `java.time.LocalDate` / `YearMonth` / `DateTimeFormatter` while `minSdk` is 24 and those APIs only exist from API 26. That is not just a lint complaint — on Android 7.0/7.1 the affected calls throw `NoClassDefFoundError`, which the surrounding `catch (_: Exception)` blocks do not catch. Core library desugaring is now enabled (`isCoreLibraryDesugaringEnabled = true` with `com.android.tools:desugar_jdk_libs:2.1.5`), which backports `java.time` to API 24, clears all 39 errors, and makes the declared `minSdk 24` genuinely supported. `lintDebug` is green.

### P3 - Dependency hygiene
`app/build.gradle.kts` carries many commented-out dependencies (Room, Retrofit, Moshi, CameraX, Coil). Remove ones with no near-term plan to keep the manifest of intent clean; version catalog (`libs.*`) usage is otherwise good.

---

## Security Notes

- Firestore rules are the strongest security asset: default-deny, owner scoping, strict schema validation. Recommend adding emulator-based rules tests (there is already a `firestore.test.js` at root to build on).
- No secrets are committed; keys flow through `.env` (gitignored) with `.env.example` as the template.
- Verify `google-services.json` and `my-upload-key.jks` are gitignored (they are referenced only via env/paths, which is correct).
- Cleartext traffic is denied by default via `res/xml/network_security_config.xml`; only the Firebase emulator loopback hosts are exempt.
- The requested-permission footprint is now limited to `INTERNET`, `ACCESS_NETWORK_STATE`, `VIBRATE`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED` and `SCHEDULE_EXACT_ALARM`. The app collects no device location.

---

## Action Plan

1. **Short-term (P1):** Enable R8 + resource shrinking for release; ~~rename `com.example` namespace~~ **done**; begin extracting ViewModel delegates.
2. **Medium-term (P2):** ~~Scope or remove cleartext traffic~~ **done**; ~~add `test` + `lint` stages to CI~~ **done**; ~~review exact-alarm policy~~ **done**.
3. **Housekeeping (P3):** Drop unused commented dependencies; add Firestore rules tests to CI.

---

## Repository Cleanup (this change)

As part of this review the repo was cleaned of non-source artifacts: `.aistudio/` (temp media), `screenshots/`, and `design/` were removed, and the broken `## Screenshots` section was dropped from `README.md`.
