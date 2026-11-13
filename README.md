# Ruhani — রুহানি

**ইসলামি জীবনযাপন, ইবাদত ও কুরআন শিক্ষার বিশ্বস্ত সঙ্গী**

Developed & Powered by **[KhansCreations](https://khanscreations.com)**

Ruhani is a comprehensive Islamic lifestyle Android app built with Kotlin and Jetpack Compose. It brings together Quran reading, prayer times, Qibla direction, Tasbih, Dua & Dhikr, and many more features — all in one place, with full Bangla language support.

---

## Features

**Quran**
- Full Quran browser with Surah reader
- Audio recitation with mini player (multiple Qari support)
- Ayah bookmarks synced to the cloud

**Prayer & Worship**
- Accurate daily prayer times based on Bangladesh district and calculation method
- Qibla compass using device sensors
- Digital Tasbih counter with presets
- Salat & Qaza tracker to log and catch up on missed prayers
- Nawafil (voluntary prayer) guide

**Knowledge & Learning**
- Dua & Dhikr collection
- 99 Names of Allah
- Hadith collection
- Islamic Books library
- Fatwa reference
- Hajj & Umrah guide

**Utilities**
- Tri-Calendar: Gregorian, Hijri, and Bengali date side by side
- Zakat calculator
- Islamic baby names
- Nearby mosques finder
- Ramadan Hub with sehri/iftar schedule
- Haramain Live (Mecca & Medina)

**Account & Sync**
- Google Sign-In with Firestore cloud sync
- Guest/offline mode — no account required

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Backend | Firebase Firestore + Firebase Auth |
| Prayer Times | [Adhan](https://github.com/batoulapps/adhan-java) library |
| Auth | Google Credential Manager |
| Async | Kotlin Coroutines |
| Testing | JUnit, Robolectric, Espresso |

---

## Requirements

- Android 7.0 (API 24) or higher
- Target SDK 36
- Google Play Services (for Google Sign-In)
- Notification permission — for Azan alerts
- Exact alarm permission — on Android 12+ the user must grant it (Settings → App Settings → Prayer Time Alerts) for Azan alerts to fire on time

---

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/KhanPalash/ruhani_v2.git
cd ruhani_v2
```

### 2. Set up Firebase

1. Create a project at [Firebase Console](https://console.firebase.google.com)
2. Add an Android app with package ID `com.khanscreations.ruhani`
3. Download `google-services.json` and place it in `app/`
4. Enable **Firestore** and **Google Sign-In** in the Firebase console

> The app ID was previously `com.aistudio.ruhani.kxmzpq`. That package must be
> registered as a second Android app in the **same** Firebase project so existing
> users keep their Auth account and Firestore data, and a fresh
> `google-services.json` downloaded afterwards. Google Sign-In also needs an OAuth
> client for the new package name with the same SHA-1 fingerprint.

### 3. Configure secrets

Create a `local.properties` file in the root (if not already present) and add your keys:

```
MAPS_API_KEY=your_google_maps_api_key
```

### 4. Build and run

Open the project in Android Studio and run on a device or emulator, or use Gradle:

```bash
./gradlew assembleDebug
```

#### Troubleshooting: `bad interpreter: Permission denied`

On native Android terminals (e.g. Termux), `./gradlew` can fail with `bad interpreter: Permission denied` because `/bin/sh` does not exist at the path in the script's shebang. Run the wrapper through `bash` instead:

```bash
bash gradlew assembleDebug
```

If Gradle cannot write to its default home directory on your device, point it to a writable location with the `GRADLE_USER_HOME` environment variable:

```bash
export GRADLE_USER_HOME="$HOME/.gradle"
bash gradlew assembleDebug
```

---

## Project Structure

```
app/src/main/java/com/khanscreations/ruhani/
├── MainActivity.kt           # App entry, navigation shell
├── data/
│   ├── model/                # Data models (UserProfile, QuranBookmark, SalatLog)
│   ├── remote/               # API service (Quran API)
│   └── repository/           # Firestore repository
├── domain/                   # Business logic, prayer engine, content catalog
│   ├── AstronomicalEngine.kt
│   ├── OfflineContentAndSyncManager.kt
│   └── ...
└── ui/
    ├── RuhaniViewModel.kt    # Single source of truth for app state
    ├── screens/              # All Composable screens
    └── theme/                # Colors, typography, Material theme
```

---

## Configuration & Settings

The app supports the following user-configurable settings:

- **Language** — Bangla or English
- **Bangla digits** — Toggle between Bengali and Arabic numerals
- **Theme** — Dark, Light, or follow system
- **District** — Select your Bangladesh district for accurate prayer times
- **Calculation method** — Defaults to Islamic Foundation Bangladesh
- **Madhhab** — Hanafi (Asr timing)
- **Hijri date offset** — Adjust the Hijri calendar ±days if needed

---

## Permissions Used

| Permission | Purpose |
|---|---|
| `INTERNET` | Quran audio streaming, cloud sync |
| `ACCESS_NETWORK_STATE` | Network availability checks |
| `POST_NOTIFICATIONS` | Azan and prayer time alerts |
| `VIBRATE` | Tasbih and notification feedback |
| `RECEIVE_BOOT_COMPLETED` | Re-arm prayer alarms after reboot or a clock/timezone change |
| `SCHEDULE_EXACT_ALARM` | Fire Azan alerts at the exact prayer time (user-granted on Android 12+) |
| `ACCESS_COARSE_LOCATION` | Optional: map one approximate fix onto the nearest of the 64 built-in districts |

The app never asks for `ACCESS_FINE_LOCATION`. Location is optional and only used when
you tap **"Detect district from location"** (or on launch, once granted); a single
approximate fix is converted into the nearest district from the built-in catalogue and
then discarded. No coordinate is stored, logged, or uploaded — only the district id, and
you can always pick a district by hand instead.

All network traffic is HTTPS. `res/xml/network_security_config.xml` denies cleartext
for every host except the loopback addresses used by the Firebase emulator in local
development.

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature`)
3. Commit your changes
4. Push and open a Pull Request

---

## Developed & Powered by

**[KhansCreations](https://khanscreations.com)** — *Technology & Digital Solutions*

- Website: [https://khanscreations.com](https://khanscreations.com)
- GitHub: [@KhanPalash](https://github.com/KhanPalash)

---

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
