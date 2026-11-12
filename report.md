# Developer Experience (DevEx) Audit Report: Ruhani v2

**Target Project:** [Ruhani v2](https://github.com/KhanPalash/ruhani_v2)  
**Date:** November 12, 2026  
**Auditor:** Antigravity Autonomous CTO & Lead DevEx Reviewer  

---

## Executive Summary

A comprehensive Developer Experience (DevEx / DX) live audit was conducted on the `ruhani_v2` Android codebase (Kotlin, Jetpack Compose, Material 3, Firebase, Adhan). This report outlines findings across seven core DX dimensions, evaluates developer friction points, and provides prioritized remediation steps to streamline onboarding, testing, and continuous delivery.

**Overall DevEx Score:** **6.1 / 10 (Grade: B-)**

---

## DevEx Scorecard

| # | Dimension | Score | Evidence | Status |
|---|---|:---:|---|---|
| 1 | **Getting Started & Onboarding** | **6.5 / 10** | `README.md`, `local.properties` | Acceptable (Minor setup friction) |
| 2 | **Architecture & Code Ergonomics** | **7.0 / 10** | `RuhaniViewModel.kt` | Good modular domains, monolithic ViewModel |
| 3 | **Error Handling & Versioning** | **5.5 / 10** | `UpdateChecker.kt`, `app/build.gradle.kts` | Critical version mismatch bug |
| 4 | **Documentation & Visual Assets** | **7.5 / 10** | `README.md`, `screenshots/` | High quality (1 broken link) |
| 5 | **CI/CD & Automation** | **3.0 / 10** | `.github/workflows/` | Poor (Workflow deleted in commit `4771f10`) |
| 6 | **Testing & Quality Assurance** | **8.0 / 10** | Robolectric UI & JUnit tests | Strong automated coverage |
| 7 | **Community & Ecosystem** | **5.0 / 10** | Missing `CHANGELOG.md`, `LICENSE` | Needs Work |

---

## Key Strengths

1. **Graceful Firebase Fallback:**
   In `app/build.gradle.kts`:
   ```kotlin
   googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }
   ```
   Developers can clone and build the project immediately without a mandatory `google-services.json` file.
2. **Comprehensive Automated Test Coverage:**
   The repository includes both unit tests (`ExampleUnitTest.kt`) for astronomical/prayer calculations and full Compose UI tests (`ExampleRobolectricTest.kt`) covering navigation, Tasbih state persistence, and Dua search.
3. **Consistent Commit Discipline:**
   Git history strictly follows Conventional Commits (`feat:`, `fix:`, `refactor:`, `docs:`), facilitating automated changelog generation and clear audit trails.

---

## Critical Issues & Friction Points

### 1. [P0 BUG] Hardcoded Version Mismatch Causing Infinite Update Dialog
- **Location:** `app/build.gradle.kts` (Lines 20–21), `RuhaniViewModel.kt` (Lines 237–249)
- **Problem:**
  ```kotlin
  versionCode = 1
  versionName = "1.0"
  ```
  The app is published as `v2.0.1` on GitHub. When built from source, `packageManager.getPackageInfo().versionName` reads `"1.0"`. `UpdateChecker.isNewer("2.0.1", "1.0")` evaluates to `true`, displaying an intrusive "Update Available" popup on every launch even for users already running the latest build.
- **Remediation:** Bump `versionCode = 2` and `versionName = "2.0.1"` in `app/build.gradle.kts`.

### 2. [P0 CI/CD] Accidental Deletion of GitHub Actions APK Builder
- **Location:** Commit `4771f10` (`feat: add KhansCreations branding...`)
- **Problem:**
  `.github/workflows/build-apk.yml` was deleted during commit `4771f10`. Currently, no automated CI build or APK artifact generator runs on commits or pull requests.
- **Remediation:** Restore `.github/workflows/build-apk.yml` to ensure continuous compilation, test execution, and automated APK artifact builds.

### 3. [P1 DX] Monolithic ViewModel Architecture (1,760 LOC)
- **Location:** `app/src/main/java/com/example/ui/RuhaniViewModel.kt`
- **Problem:** A single ViewModel class handles prayer calculation, digital Tasbih state, Quran audio playback, Firestore sync, analytics, advertising modal dialogs, and district filtering.
- **Remediation:** Refactor `RuhaniViewModel` into focused domain delegates or sub-ViewModels:
  - `PrayerViewModel`
  - `QuranAudioViewModel`
  - `TasbihViewModel`
  - `SyncAndAccountViewModel`

### 4. [P1 Docs] Broken License Link & Missing Documentation Files
- **Location:** `README.md` (Line 175)
- **Problem:** `README.md` references `[LICENSE](LICENSE)`, but no `LICENSE` file exists in the repository root (results in 404). Additionally, `CHANGELOG.md` and `.github/ISSUE_TEMPLATE` are missing.
- **Remediation:** Add standard MIT `LICENSE` file and initialize `CHANGELOG.md`.

### 5. [P2 Environment] Execution Permission on Native Android Terminals
- **Problem:** Running `./gradlew` on Android userspace fails with `bad interpreter: Permission denied` due to `/bin/sh` path differences.
- **Remediation:** Document using `bash gradlew <task>` and set `systemProp.gradle.user.home` in `gradle.properties`.

---

## Action Plan & Roadmap

1. **Immediate (P0):**
   - Update `app/build.gradle.kts` to `versionCode = 2`, `versionName = "2.0.1"`.
   - Restore `.github/workflows/build-apk.yml`.
2. **Short-Term (P1):**
   - Add MIT `LICENSE` and create `CHANGELOG.md`.
   - Add GitHub issue and PR templates.
3. **Medium-Term (P2):**
   - Modularize `RuhaniViewModel.kt` into domain-specific view models.
