# Project Review Report: Ruhani v2

**Project:** [Ruhani v2](https://gitlab.com/khanscreations3/ruhani_v2)
**Date:** November 15, 2026
**Previous review:** November 12, 2026 (score 7.2 / 10)
**Scope:** Architecture, code quality, security, build/CI, testing, documentation
**Stack:** Kotlin 2.2.10, Jetpack Compose (BOM 2024.09.00), Material 3, AGP 9.1.1, Firebase (Firestore + Auth), Adhan 1.2.1

---

## Executive Summary

Ruhani v2 is a single-module Android app (`com.khanscreations.ruhani`) offering Quran reading, prayer times, Qibla, Tasbih, Dua & Dhikr, and cloud sync with full Bangla support. The codebase is organised into `data` / `domain` / `ui` layers, has real automated test coverage, hardened Firestore rules, and disciplined Conventional Commits.

**Overall score: 7.1 / 10 (Grade: B)** — essentially unchanged from the previous 7.2, and that is the honest result.

The three headline items from the November 12 review have all landed: the monolithic ViewModel has been split into five focused sub-ViewModels, the release build now runs R8 with the resource shrinker, and CI runs unit tests and lint on both GitLab and GitHub with the APK build gated on both. Those are real, well-executed improvements, and the security posture around secrets, cleartext traffic and permissions is genuinely above average.

The score barely moves because this review surfaced four P0 defects in the app's core behaviour that the previous review could not see from static reading alone — including a prayer-notification bug that is confirmed empirically against the compiled engine. The structural wins and the behavioural bugs roughly cancel out. The pattern to watch is consistent across the whole codebase: **intent is strong, behaviour lags behind it.** The privacy promises, the notification logic and the test coverage all claim more than they deliver.

---

## Scorecard

| # | Dimension | Score | Status |
|---|---|:---:|---|
| 1 | Architecture & structure | 8.0 / 10 | ViewModel split done; state-merge duplication remains |
| 2 | Code quality & ergonomics | 7.0 / 10 | Good patterns; a few silent-failure paths |
| 3 | Security | 6.5 / 10 | Strong rules; backup + WebView gaps |
| 4 | Build & release config | 7.0 / 10 | R8 correct; Robolectric path breaks the suite |
| 5 | CI/CD | 7.5 / 10 | Test + lint gates both platforms; no emulator |
| 6 | Testing & QA | 6.5 / 10 | Real coverage; rules tests never execute |
| 7 | Documentation | 7.5 / 10 | Strong README; two inaccurate claims |

---

## Verification Performed

Everything below was executed against the working tree at commit `800854f`.

| Check | Command | Result |
|---|---|---|
| Compilation | `compileDebugKotlin` | PASS |
| Lint | `lintDebug` | **PASS** — 24 warnings, 0 errors |
| Unit tests | `testDebugUnitTest` | 28 passed / 5 skipped / 0 failed, **after** working around P0-4 |
| Engine behaviour | custom JVM probe against the compiled `AstronomicalEngine` | **P0-1 confirmed empirically** |
| Secrets hygiene | `git ls-files` | PASS — no keys, keystores or `google-services.json` tracked |

Lint's 24 warnings are exactly the 17 deliberately-pinned dependency versions (`GradleDependency` / `NewerVersionAvailable`), 5 launcher-icon shape advisories and 2 AGP-version advisories. The CHANGELOG's claim of "121 → 24" is accurate.

> **Local environment note (not a repository defect).** `local.properties` on the review machine pointed at `/home/khanscreations/Android/Sdk`, a Linux path that does not exist on Windows. The review ran with `ANDROID_HOME` set instead of editing the file. `local.properties` is gitignored, so nothing is wrong with the repository here — but a stale `sdk.dir` will block the first build on any new machine.

---

## Strengths

1. **The ViewModel split landed well.** `RuhaniViewModel` is now a thin facade over `PrayerViewModel`, `TasbihViewModel`, `QuranAudioViewModel`, `SyncAndAccountViewModel` and `WorshipToolsViewModel`, each owning its own slice of state. The public API of the facade is preserved, so the screens needed no rework.
2. **Release build hygiene is now correct.** R8 and the resource shrinker are on, and `proguard-rules.pro` keeps the right things for the right reasons: the Firestore document models that `toObject()` instantiates reflectively, the `@ServerTimestamp`/`@DocumentId` annotations, and the enum `values()`/`valueOf()` members used to read stored setting names back. `res/raw/keep.xml` additionally protects the Firebase configuration strings that `FirebaseOptions.fromResource()` resolves by name through `Resources.getIdentifier` — a genuinely subtle trap, handled correctly.
3. **`firestore.rules` remains the strongest asset in the repository.** Global default-deny, per-entity `hasAll` / `hasOnly` schema validation, owner scoping, and immutability checks on `createdAt`, `userId` and `dateKey`.
4. **CI is real on both platforms.** `.gitlab-ci.yml` and `.github/workflows/build-apk.yml` each run `testDebugUnitTest` and `lintDebug`, and the GitHub APK job declares `needs: [unit_tests, lint]`, so a red test or lint genuinely blocks the artifact.
5. **No secrets are committed.** `google-services.json`, `debug.keystore` and `*.jks` are untracked and gitignored; keys flow through `.env` / `.env.example` via the Secrets Gradle plugin.
6. **Cleartext is denied by default** via `res/xml/network_security_config.xml`, with exemptions only for the loopback addresses the Firebase emulator needs.
7. **Location handling is careful.** Coarse only, no `ACCESS_FINE_LOCATION`, a single fix converted to a district centroid and then discarded, and a 150 km guard so a user abroad is not mapped onto a Bangladeshi district.
8. **The comments explain *why*, not just *what*.** `libs.versions.toml`, `proguard-rules.pro` and `keep.xml` all record the constraint behind a decision ("firebase-auth 25.0.0 is compiled with Kotlin 2.4 metadata and cannot be read by this project's Kotlin 2.2.10 compiler"). This is the single best predictor that the next contributor will not break the build by bumping a pin.

---

## P0 — Correctness

### P0-1 · Every prayer fires **two** azan notifications, and Dhuhr fires ~5.5 hours early

There are two independent notification paths and they disagree:

- `PrayerAlarmScheduler` → `PrayerNotificationReceiver` (`domain/PrayerAlarmScheduler.kt:14-35`) fires at `PrayerWindow.startMinutes`, which is correct.
- `PrayerViewModel.startLivePrayerClock()` (`ui/PrayerViewModel.kt:129-151`) fires whenever `schedule.currentWaqt` *changes*.

The defect is in `domain/AstronomicalEngine.kt:311-317`: `currentWaqt` becomes `DHUHR` at **sunrise**, not at `dhuhrStart`. The engine's own prayer list (`:361-368`) uses `dhuhrStart`, so the two disagree for about five and a half hours every day.

Probing the compiled engine directly (Dhaka, 2026-11-15):

```
04:50  sunrise=06:13 AM  currentWaqt=ISHA    | FAJR=04:55 AM  DHUHR=11:44 AM  ASR=03:37 PM  MAGHRIB=05:13 PM  ISHA=06:31 PM*
06:30  sunrise=06:13 AM  currentWaqt=DHUHR   | FAJR=04:55 AM  DHUHR=11:44 AM  ASR=03:37 PM  MAGHRIB=05:13 PM  ISHA=06:31 PM
09:00  sunrise=06:13 AM  currentWaqt=DHUHR   | (no prayer marked current)
11:50  sunrise=06:13 AM  currentWaqt=DHUHR   | ... DHUHR=11:44 AM*
```

`*` marks the prayer the engine's own list reports as "is current".

**Net effect:** ten notifications per day instead of five. Fajr, Asr, Maghrib and Isha each notify twice at the same minute. **Dhuhr announces itself at 06:13 AM and again at 11:44 AM.** Only users with the app open are affected — otherwise only the alarm path fires — which is exactly when a duplicate is most noticeable.

**Fix:** make `currentWaqt` agree with the prayer list (DHUHR from `dhuhrStart`), then pick a single notification authority. The alarm should win; the in-app clock should only fire when no alarm was scheduled.

### P0-2 · Failed downloads are recorded as successful, and 1 KB of garbage is written to disk

`domain/AuthOfflineAndNotificationEngine.kt:735-760` — on any network error, `downloadBinaryOrCreateOfflineHeader` writes `ByteArray(1024) { 0x49 }` and returns `1024`. Callers treat that as success:

- `:496-500` — `bytes > 500 && targetFile.length() > 500L` passes, so `downloadAyahAudioForOffline` returns `true`.
- `:471-474` — `isAyahAudioCachedOffline` uses `length() > 256L`, so the placeholder also passes as "cached offline".
- `ui/QuranAudioViewModel.kt:602-614` — `toggleOfflineSurahDownload` discards the `Result` entirely and always marks the surah as downloaded.

The UI therefore reports success, the file exists, and it is 1 KB of `'I'` bytes that cannot play. The failure branch at `ui/QuranAudioViewModel.kt:578-581` is effectively unreachable. The offline-audio feature is broken, and it fails silently.

There is also a threshold inconsistency: the player path uses `length() > 1500L` (`ui/QuranAudioViewModel.kt:304`) while the cache check uses `> 256L` — two different definitions of "cached" for the same file.

**Fix:** remove the placeholder-bytes path, return `false` on failure, and propagate the `Result` to the caller.

### P0-3 · "Delete Data" does not delete the cloud account, and deleted local data comes back

- `domain/FirebaseAuthManager.kt:144` defines `deleteCurrentAccount()`, but it is **never called** anywhere in the tree — the only match is its own declaration.
- `ui/SyncAndAccountViewModel.kt:420-435` clears `ruhani_local_prefs`, but the sub-ViewModels keep their **in-memory** state. The next `incrementTasbih()` (`ui/TasbihViewModel.kt:160-166`) or `adjustQazaCount()` (`ui/WorshipToolsViewModel.kt:197-204`) writes the old values straight back to disk.
- `ui/RuhaniViewModel.kt:687-695` resets only `bookmarks` and `exportedUserDataJson`; tasbih totals, qaza counts and salat logs remain on screen after the deletion.
- The success message reads *"আপনার অ্যাকাউন্ট ও লোকাল ডেটা সম্পূর্ণ মুছে ফেলা হয়েছে"* — "your account and local data have been completely deleted". For the cloud account that is false: the Firebase user and their Firestore documents survive.
- There is **no confirmation dialog** before this destructive one-tap wipe.

**Fix:** call `deleteCurrentAccount()` and delete the Firestore documents, reset the sub-ViewModel states, add a confirmation step, and make the wording match what actually happens.

### P0-4 · The test suite cannot run on any machine whose home directory contains a space

`app/build.gradle.kts:88-97` sets `robolectric.dependency.dir` to a **raw filesystem path**. Robolectric treats that value as a URL and percent-encodes it:

```
NoSuchFileException: C:\Users\Jaia%20Telecom\.m2\repository\org\robolectric\
  android-all-instrumented\16-robolectric-13921718-i7\android-all-instrumented-...jar
```

The jar is present at the unencoded path (203.5 MB). Three problems compound:

1. The property requires a `file:` URL (`roboDir.toURI()`), not `.absolutePath`.
2. The guard checks the **directory**, not the jar — and Robolectric downloads the artifact into that exact directory, so the guard is self-fulfilling.
3. `failFast = true` (`:83`) means one environment failure aborts the whole suite; only 2 of 30 tests ran.

Temporarily disabling that block produced **BUILD SUCCESSFUL, 28 passed / 5 skipped / 0 failed**; the edit was then reverted and the working tree confirmed clean. CI on Linux never hits this, so it stays invisible there.

**Fix:**

```kotlin
val roboJar = roboDir.resolve("android-all-instrumented-16-robolectric-13921718-i7.jar")
if (roboJar.exists()) {
  systemProperty("robolectric.dependency.dir", roboDir.toURI().toString())
  systemProperty("robolectric.offline", "true")
}
```

---

## P1 — Security & Privacy

### P1-1 · The Firestore rules have zero executed test coverage

- `RuhaniRepositoryRuleTest` last run: `tests="5" skipped="5"`. All five skip through `Assume.assumeTrue` (`base/FirestoreEmulatorTestBase.kt:43-46`) because no emulator is running.
- `firestore.test.js` at the repository root **cannot run at all** — there is no `package.json`, no `node_modules`, and it requires `@firebase/rules-unit-testing`. Neither CI configuration mentions `firestore`, `emulator` or `node`.
- `base/FirestoreEmulatorTestBase.kt:50` also hardcodes a stale database id from the old project name (`ai-studio-android-ruhani-…`).

The green CI badge therefore says nothing about the rules. This was flagged as P3 in the November 12 review and remains open — and it is the item that matters most, because `firestore.rules` is the repository's strongest security asset.

**Fix:** add a `package.json`, run the emulator via `firebase emulators:exec` in CI, and make the emulator-backed Kotlin tests **fail** rather than skip when the emulator is expected but absent.

### P1-2 · Cloud Backup includes the auth vault and the offline cache

`res/xml/backup_rules.xml` and `res/xml/data_extraction_rules.xml` are the **untouched Android Studio templates** — every rule is commented out, and one still literally contains `TODO:`. With `android:allowBackup="true"` and no `<exclude>`:

- `ruhani_secure_auth_vault` — PBKDF2 password **hashes, salts and the active session** — is uploaded to Google Drive and restored onto a new device.
- `ruhani_notifications_vault` and the entire offline Quran/audio cache are backed up as well.

**Fix:** add explicit `<exclude>` entries for the auth vault and the offline files, or set `android:allowBackup="false"`.

### P1-3 · The local auth vault is weaker than its name and its marketing claim

- `PBKDF2_ITERATIONS = 12_000` (`domain/AuthOfflineAndNotificationEngine.kt:116`); OWASP currently recommends **600,000** for PBKDF2-HMAC-SHA256.
- Hashes and salts are stored in plain `SharedPreferences` (`:110`, `:196-199`), not `EncryptedSharedPreferences`.
- The exported privacy JSON advertises *"PBKDF2-HMAC-SHA256 + Zero-Trust Firestore Rules"* — the claim overstates what is delivered.

Registration and login now route through `FirebaseAuthManager`, so this vault is largely vestigial. **Deleting it is better than hardening it.**

### P1-4 · The WebView grants geolocation to any origin and launches unsanitised `intent://` intents

`ui/screens/MosqueNearbyMapCard.kt` — the newest code in the repository (last three commits):

- `:95-100` — `callback.invoke(origin, true, false)` grants the geolocation prompt to **any** origin, unconditionally.
- `:112-123` — `Intent.parseUri(url, Intent.URI_INTENT_SCHEME)` is started as-is. A parsed `intent://` can carry `component=`, `package=` or `EXTRA_INTENT`, letting a page launch arbitrary activities with arbitrary extras. `shouldOverrideUrlLoading` is not restricted to `request.isForMainFrame`, so an **iframe** can trigger it.

The README's privacy claim — *"No coordinate is stored, logged, or uploaded — only the district id"* — no longer holds either: the WebView can now obtain the device's coarse location and hand it to the Maps embed.

**Fix:** grant geolocation only for `https://maps.google.com`, gate on `isForMainFrame`, and sanitise the parsed intent (clear `component`, allowlist the package, drop `EXTRA_INTENT`).

---

## P2 — Robustness

**P2-1 · Sync failures are reported as success and the retry queue is discarded.**
`ui/WorshipToolsViewModel.kt:177-183` ignores the `Result` from `saveSalatLog` and calls `markQueueSynchronized` regardless; `ui/SyncAndAccountViewModel.kt:567-586` behaves the same way. A failed write clears the queue and reports *"সব অফলাইন পরিবর্তন ও অগ্রগতি সফলভাবে সিঙ্ক হয়েছে"*. This is silent data loss.

**P2-2 · The Bangla-digits setting is never persisted.**
`ui/SyncAndAccountViewModel.kt:266-268` updates only in-memory state and `loadInitialState()` never reads it back, so the setting resets on every app restart — although the README lists it as a persisted setting.

**P2-3 · Bookmarks are never persisted locally.**
`ui/QuranAudioViewModel.kt:514-550` touches only in-memory state and Firestore. In guest/offline mode bookmarks vanish on restart, contradicting the README's "Guest/offline mode — no account required".

**P2-4 · The prayer schedule is recomputed every second, including in the background.**
`ui/PrayerViewModel.kt:129-151` calls `refreshPrayerSchedule()` → the full Adhan solar calculation plus Hijri and Bangla conversions and roughly thirty string formats, on a 1 Hz loop, on the Activity-scoped `viewModelScope` — which is *not* cancelled when the app backgrounds (`collectAsStateWithLifecycle` stops only the UI collection). Each tick also allocates a new ~100-field `RuhaniUiState` and triggers recomposition. Separate the countdown tick from the schedule computation.

**P2-5 · The alarm scheduler uses the device timezone; the engine uses `Asia/Dhaka`.**
`domain/PrayerAlarmScheduler.kt:130-135` uses `Calendar.getInstance()` (system TZ) against `domain/AstronomicalEngine.kt:218`'s Dhaka times. Correct inside Bangladesh, wrong for a user abroad who is still tracking Dhaka prayer times.

**P2-6 · `RuhaniViewModel` maps the same ~90 fields twice.**
`observeSubViewModels()` (`ui/RuhaniViewModel.kt:272-414`) and `mergeStates()` (`:416-522`) are field-for-field duplicates. Adding a field to any sub-state requires editing both, and nothing enforces that. Collapse to a single derived state.

**P2-7 · `findDistrict()` silently falls back to Dhaka.**
`domain/RuhaniContentCatalog.kt:187-192` — a renamed district id in a cloud profile would silently change the user's prayer times with no signal.

---

## P3 — Documentation & Housekeeping

- **`CHANGELOG.md:19` is inaccurate.** It states *"The debug APK drops from 25.9 MB to 4.8 MB"*, but R8 and the resource shrinker run on the **release** build type only. Measured on the review machine: `app-debug.apk` = **26.11 MB**, `app-release.apk` = **4.78 MB**.
- **`README.md:114-120` is obsolete.** It instructs the reader to put `MAPS_API_KEY` in `local.properties`, but the Secrets Gradle plugin reads `.env` / `.env.example`, `.env.example` contains only a commented-out `GEMINI_API_KEY`, and no Maps key is required any more — the map is a key-less `output=embed` iframe. No code reads `BuildConfig.MAPS_API_KEY`.
- **Template leftovers.** `ExampleUnitTest.kt:13-15` still asserts `4 == 2 + 2`. The `Example*` names are misleading: `ExampleRobolectricTest` in fact contains real Compose UI smoke tests.
- `domain/AuthOfflineAndPrivacyEngine` → `domain/AuthOfflineAndNotificationEngine.kt:172` derives the user id from `email.hashCode()` — a 32-bit, trivially reversible hash that can collide between two users.
- `ui/WorshipToolsViewModel.kt` rehydrates only *today's* salat log from prefs; earlier days are lost locally in guest mode.
- The pending-sync queue in `domain/AuthOfflineAndNotificationEngine.kt:653-676` grows without bound, and its id can collide for two actions in the same millisecond.

---

## Resolved Since the November 12 Review

| Previous finding | Status |
|---|---|
| P1 — Monolithic `RuhaniViewModel` | **DONE.** Split into five focused sub-ViewModels behind a thin facade. |
| P1 — Release build not minified or shrunk | **DONE.** R8 + resource shrinker enabled, with correct and well-explained keep rules. |
| P1 — Generic `com.example` namespace | **DONE.** `com.khanscreations.ruhani`; sources, packages and `applicationId` all migrated. |
| P2 — Cleartext traffic globally enabled | **DONE.** Scoped `network_security_config.xml`; loopback exemptions only. |
| P2 — Sensitive / permission-heavy manifest | **DONE.** `ACCESS_FINE_LOCATION` and `WAKE_LOCK` removed; coarse location added back deliberately for district detection. |
| P2 — CI runs build only | **DONE.** Test and lint stages on GitLab and GitHub, with the APK build gated on both. |
| P3 — Dependency hygiene | **DONE.** Commented-out dependencies and unused catalog entries removed. |

---

## Security Notes

- `firestore.rules` remains the strongest security asset: default-deny, owner scoping, strict schema validation, and immutability checks on identity fields. It needs its tests actually running (P1-1) before it can be relied on as a control rather than as documentation.
- No secrets are committed; keys flow through `.env` (gitignored) with `.env.example` as the template. `google-services.json`, `debug.keystore` and `*.jks` are untracked.
- Cleartext is denied by default, with exemptions only for the Firebase emulator loopback hosts.
- The requested-permission footprint is limited to `INTERNET`, `ACCESS_NETWORK_STATE`, `VIBRATE`, `POST_NOTIFICATIONS`, `RECEIVE_BOOT_COMPLETED`, `SCHEDULE_EXACT_ALARM` and the optional `ACCESS_COARSE_LOCATION`. The app never asks for fine location — but it does now hand a WebView the ability to request coarse location (P1-4).
- The permission and privacy story is well documented in the manifest and README. The gap is not the documentation but the places where behaviour diverges from it: backup inclusion (P1-2) and the delete-account claim (P0-3).

---

## Action Plan

**Immediate (P0)** — each is small, self-contained, and disproportionate in user impact:

1. Fix `currentWaqt` / Dhuhr timing and collapse the two notification paths into one authority.
2. Delete the placeholder-bytes download fallback and propagate `Result`s to the callers.
3. Correct `robolectric.dependency.dir` to a proper `file:` URL and check the jar rather than the directory.
4. Make "Delete Data" actually delete the cloud account, reset sub-ViewModel state, and add a confirmation step.

**Short term (P1)**:

5. Exclude the auth vault and offline files from Cloud Backup.
6. Scope the WebView's geolocation grant and sanitise parsed `intent://` intents.
7. Wire the Firestore rules tests into CI and make a missing emulator fail rather than skip.
8. Propagate sync `Result`s and stop clearing the pending queue on failure.

**Medium term (P2)**:

9. Persist the Bangla-digits setting and local bookmarks.
10. Split the countdown tick from the schedule computation; align the alarm timezone with the engine.
11. Collapse the duplicated state mapping in `RuhaniViewModel`.

**Housekeeping (P3)**:

12. Correct the CHANGELOG APK-size claim and the obsolete README secrets step; remove template leftovers.

---

## Appendix — Reproduction

```bash
# Local environment for this review (Windows)
export ANDROID_HOME="C:\Users\<you>\AppData\Local\Android\Sdk"

# Lint (currently green: 24 warnings, 0 errors)
./gradlew lintDebug

# Unit tests
./gradlew testDebugUnitTest --no-configuration-cache
# -> fails with "Unable to load Robolectric native runtime library" on a
#    home directory containing a space, until P0-4 is fixed

# The empirical probe behind P0-1
# (compile the app, then run a small main() against the compiled
#  AstronomicalEngine with the Adhan jar on the classpath, calling
#  calculateSchedule(district, method, madhhab, 0, nowMillis, 0) at
#  06:30 and 11:50 local time and printing currentWaqt against
#  fivePrayers[].isCurrent)
```

---

*Review conducted against commit `800854f`. All findings were verified by reading the code, by running the build, lint and test tasks, and by executing the compiled engine directly.*
