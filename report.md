# Project Review Report: Ruhani v2

**Project:** [Ruhani v2](https://gitlab.com/khanscreations3/ruhani_v2)
**Date:** November 12, 2026
**Scope:** Architecture, code quality, security, build/CI, documentation
**Stack:** Kotlin, Jetpack Compose, Material 3, Firebase (Firestore + Auth), Adhan

---

## Executive Summary

Ruhani v2 is a single-module Android app (`com.aistudio.ruhani.kxmzpq`) offering Quran reading, prayer times, Qibla, Tasbih, Dua & Dhikr, and cloud sync with full Bangla support. The codebase is well organized into `data` / `domain` / `ui` layers, has real automated test coverage, hardened Firestore rules, and disciplined Conventional Commits. The main risks are a monolithic ViewModel, release build hygiene (no minification/shrinking), a generic `com.example` namespace, and cleartext traffic being globally enabled.

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

### P1 - Generic application namespace
`namespace = "com.example"` while `applicationId = "com.aistudio.ruhani.kxmzpq"`. The `com.example` package is a placeholder and should be renamed (e.g. `com.khanscreations.ruhani`) to avoid collisions and look production-grade.

### P2 - Cleartext traffic globally enabled
`AndroidManifest.xml` sets `android:usesCleartextTraffic="true"`. If all APIs are HTTPS, remove this or replace it with a scoped `network_security_config.xml` that only allows cleartext for specific hosts.

### P2 - Sensitive/permission-heavy manifest
`SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, and fine location are declared. These are justified by prayer alarms and Qibla, but confirm Play Store policy compliance for exact alarms (Android 13+) and consider the `USE_EXACT_ALARM` vs `SCHEDULE_EXACT_ALARM` distinction.

### P2 - CI runs build only
`.gitlab-ci.yml` has a single `build_debug_apk` job. Add a `test` stage running `./gradlew testDebugUnitTest` and a lint stage (`./gradlew lintDebug`) so regressions are caught before merge. The job also generates a `debug.keystore` on the fly, which is fine for debug but should never be reused for release.

### P3 - Dependency hygiene
`app/build.gradle.kts` carries many commented-out dependencies (Room, Retrofit, Moshi, CameraX, Coil). Remove ones with no near-term plan to keep the manifest of intent clean; version catalog (`libs.*`) usage is otherwise good.

---

## Security Notes

- Firestore rules are the strongest security asset: default-deny, owner scoping, strict schema validation. Recommend adding emulator-based rules tests (there is already a `firestore.test.js` at root to build on).
- No secrets are committed; keys flow through `.env` (gitignored) with `.env.example` as the template.
- Verify `google-services.json` and `my-upload-key.jks` are gitignored (they are referenced only via env/paths, which is correct).

---

## Action Plan

1. **Short-term (P1):** Enable R8 + resource shrinking for release; rename `com.example` namespace; begin extracting ViewModel delegates.
2. **Medium-term (P2):** Scope or remove cleartext traffic; add `test` + `lint` stages to `.gitlab-ci.yml`; review exact-alarm policy.
3. **Housekeeping (P3):** Drop unused commented dependencies; add Firestore rules tests to CI.

---

## Repository Cleanup (this change)

As part of this review the repo was cleaned of non-source artifacts: `.aistudio/` (temp media), `screenshots/`, and `design/` were removed, and the broken `## Screenshots` section was dropped from `README.md`.
