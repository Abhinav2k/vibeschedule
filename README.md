# 📳 VibeSchedule

**Native Automated Ringer & Quiet Hours Scheduler for Android**  
*Built with Kotlin, Jetpack Compose, Material 3 & Obsidian Liquid Glass UI*

[![Android CI](https://img.shields.io/badge/Android-SDK%2026--34-green?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-purple?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20M3-blue?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Release Version](https://img.shields.io/badge/version-v1.4.18-indigo)](app/build.gradle.kts)
[![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)](LICENSE)

---

## 📖 Overview

**VibeSchedule** is a 100% native Android application designed to automatically manage your device's ringer modes and quiet hours. It eliminates the hassle of manually muting and unmuting your phone during work, school, sleep, or meetings.

The app schedules exact system transitions between **Normal**, **Vibrate**, and **Silent (Do Not Disturb)** modes using Android's native `AudioManager` and `NotificationManager`. When a schedule window finishes, your ringer is automatically restored to your default mode.

---

## ✨ Features

### ⏰ Automated Sound Schedules
- **Precise 12-Hour AM/PM Time Setting**: Native 12-hour dial picker with dedicated AM/PM segmented selectors for rapid scheduling.
- **Overnight & Midnight Spanning**: Complete support for schedules that cross midnight (e.g., *10:00 PM to 6:30 AM*).
- **Flexible Recurrence**: Day-of-week selection with presets (*Everyday*, *Weekdays*, *Weekends*).
- **Automatic Reversion**: Restores your ringer to Normal (or your chosen revert mode) the instant a schedule ends.

### 🔕 Quick Mute Timers
- One-tap temporary quiet sessions for **15 min**, **30 min**, **1 hour**, or **2 hours**.
- Real-time countdown timer tracking elapsed and remaining minutes/seconds.
- Built-in conflict detection: warns if a recurring schedule rule is already running.

### ⏸️ Pause Until Next :00
- Need ringtone enabled temporarily during an active quiet window? Pause an active rule or a rule starting within 20 minutes until the top of the next hour with a single tap.

### 🔔 Lock Screen & Status Bar Notification Card
- High-priority, ongoing notification card displaying active rule title, mode badge, remaining countdown, and live progress bar.
- Interactive action buttons: **"End Now"** and **"Skip to :00"** accessible directly from the lock screen.
- Optimized for modern Android versions (Android 14, 15, and 16 / OriginOS).

### 📱 2×1 Interactive Home Screen Widget
- Native Android AppWidget (`VibeWidgetProvider`) displaying current active sound state, remaining schedule duration, and instant one-tap **Skip** and **Cancel** controls.

### 🔄 Reboot Persistence & Reliability
- Uses Android's `AlarmManager` with exact alarms (`setExactAndAllowWhileIdle`) for battery-optimized, reliable execution.
- Registered `BootReceiver` automatically recalculates and reschedules all active alarms upon device restart.

### 💎 Obsidian Liquid Dark Glass Design
- Modern dark obsidian aesthetic crafted with Jetpack Compose, featuring subtle translucent surfaces, responsive press animations, and high-contrast typography.

---

## 🔐 Android Permissions & Setup

To function as a system ringer scheduler, the app requires the following Android permissions:

| Permission | Purpose |
| :--- | :--- |
| `android.permission.ACCESS_NOTIFICATION_POLICY` | Change Do Not Disturb (DND) and system ringer modes |
| `android.permission.SCHEDULE_EXACT_ALARM` | Trigger schedule transitions at exact minutes |
| `android.permission.USE_EXACT_ALARM` | Guarantees exact timing on Android 13+ |
| `android.permission.POST_NOTIFICATIONS` | Show ongoing lock screen status and quick actions |
| `android.permission.RECEIVE_BOOT_COMPLETED` | Reschedule alarms automatically after phone reboot |

> **Important**: On first launch, Android requires granting **Do Not Disturb (DND) Access** via system settings (*Settings → Apps → Special app access → Do Not Disturb access*). The app displays a direct setup banner until granted.

---

## 🏗️ Project Structure

```text
├── .github/workflows/
│   └── build.yml               # Automated GitHub Actions workflow to build debug APK
├── app/                        # Native Android Application module
│   ├── src/main/
│   │   ├── AndroidManifest.xml # Permissions, activities, receivers, and widget providers
│   │   ├── java/com/vibeschedule/app/
│   │   │   ├── data/           # ScheduleRepository & SharedPreferences storage
│   │   │   ├── model/          # ScheduleRule, SoundMode data models
│   │   │   ├── receiver/       # AlarmReceiver, BootReceiver, QuickMuteReceiver
│   │   │   ├── scheduler/      # AlarmScheduler (Exact AlarmManager integration)
│   │   │   ├── ui/             # Jetpack Compose UI (Screens, Components, Theme)
│   │   │   ├── util/           # NotificationHelper, SoundModeHelper
│   │   │   └── widget/         # VibeWidgetProvider (2x1 AppWidget)
│   │   └── res/                # Vector drawables, widget layouts, strings, and icons
│   └── build.gradle.kts        # Android module build configuration (v1.4.18)
├── build.gradle.kts            # Root Gradle build script
└── settings.gradle.kts         # Root Gradle settings
```

---

## 🚀 Building the APK

### Automated GitHub Releases (Recommended)
Every push to `main` or tag trigger automatically runs `.github/workflows/build.yml` to compile both signed Release and Debug APKs, generate SHA-256 checksums, and publish an official **GitHub Release**:
- **Release APK**: `VibeSchedule-v1.4.18.apk` (Signed production build)
- **Debug APK**: `VibeSchedule-v1.4.18-debug.apk` (Testing & logcat build)
- **Direct Link**: Navigate to the **Releases** tab on GitHub to download the latest APK directly to your phone.
- **Workflow Artifacts**: Also stored under the **Actions** tab for each workflow run.

### Local Build via Android Studio or Terminal
Ensure **JDK 17** is installed and configured:

```bash
# Clone the repository
git clone https://github.com/your-username/VibeSchedule.git
cd VibeSchedule

# Build the debug APK using Gradle wrapper
./gradlew assembleDebug
```

The compiled APK will be located at:
```text
app/build/outputs/apk/debug/VibeSchedule-v1.4.18-debug.apk
```

You can install it directly to an attached Android device or emulator:
```bash
adb install -r app/build/outputs/apk/debug/VibeSchedule-v1.4.18-debug.apk
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
