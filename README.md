# 📳 VibeSchedule

**Native Automated Ringer & Quiet Hours Scheduler for Android**  
*Built with Kotlin, Jetpack Compose, Material 3 & Obsidian Liquid Glass UI*

[![Android CI](https://img.shields.io/badge/Android-SDK%2026--34-green?logo=android&logoColor=white)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.24-purple?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2B%20M3-blue?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Release Version](https://img.shields.io/badge/version-v1.4.30-indigo)](https://github.com/Abhinav2k/vibeschedule/releases/latest)
[![License](https://img.shields.io/badge/license-MIT-lightgrey.svg)](LICENSE)

---

## 📖 Overview

**VibeSchedule** is a lightweight, 100% native Android application designed to automatically manage your device's ringer modes and quiet hours. It eliminates the hassle of manually muting and unmuting your phone during work, classes, sleep, or meetings.

The app schedules exact system transitions between **Normal**, **Vibrate**, and **Silent (Do Not Disturb)** modes using Android's native `AudioManager` and `NotificationManager`. When a schedule window finishes, your ringer is automatically restored to your default mode.

---

## 🚀 Download & Installation

Get the latest signed APK directly from GitHub Releases:

- 📦 **[Download VibeSchedule v1.4.30 APK](https://github.com/Abhinav2k/vibeschedule/releases/latest)**

---

## ✨ Features

### ⏰ Automated Sound Schedules
- **Precise 12-Hour AM/PM Time Setting**: Native 12-hour dial picker with dedicated AM/PM segmented selectors for rapid scheduling.
- **Overnight & Midnight Spanning**: Complete support for schedules that cross midnight (e.g., *10:00 PM to 6:30 AM*).
- **Flexible Recurrence**: Day-of-week selection with presets (*Everyday*, *Weekdays*, *Weekends*).
- **Automatic Reversion**: Restores your ringer to Normal (or your chosen revert mode) the instant a schedule ends.

### 🔕 Quick Mute Timers
- One-tap temporary quiet sessions for **15 min**, **30 min**, **1 hour**, **2 hours**, or **4 hours**.
- Conflict detection: warns if a recurring schedule rule is already running.

### 📱 Redesigned Interactive Home Screen Widget (2×1)
- **Idle State**: Shows only a minimal, centered **Ring icon**. Tapping it opens a quick popup dialog to select a timer directly without cluttering your home screen.
- **Active Timer State**: Displays remaining time and elapsed progress with a single centered **Red X** cancel button.
- **Active Schedule State**: Displays the active rule name and duration with two interactive controls: **Skip to :00** and **Cancel**.

### 🔔 Status Bar & Lock Screen Notification Card
- **App Status Bar Icon**: Shows the official VibeSchedule app icon directly in your device's status bar.
- **Lock Screen & AOD Visible**: High-priority ongoing status card with remaining countdown, mode badge, and live progress bar.
- **Quick Controls**: Instant **"End Now"** and **"Skip to :00"** actions accessible without unlocking or expanding.

### ⏸️ Pause Until Next :00
- Need ringtone enabled temporarily during an active quiet window? Pause an active rule or an upcoming rule until the top of the next hour with a single tap.

### 🔄 Reboot Persistence & Battery Efficiency
- Uses Android's `AlarmManager` with exact alarms (`setExactAndAllowWhileIdle`) for zero background battery drain.
- Registered `BootReceiver` automatically recalculates and reschedules all active alarms upon device restart.

### 💎 Floating Liquid Glass Compact Dock
- **Compact Centered Dock**: Floating pill capsule with G2 continuous-curvature superellipse squircle shape (`SquircleShape`), multi-layered specular rim lighting, top lens sheen, soft ambient elevation, and interactive touch glow.
- **Animated Expandable Labels**: Only the active tab displays its title, expanding with fluid spring animation (`expandHorizontally + fadeIn`), while inactive tabs remain compact and sleek with pure icons.
- **Sliding Translucent Frosted Glass Pill**: A translucent liquid glass indicator pill with luminous specular borders and radial sheen that glides seamlessly across tabs with spatial spring physics and tactile haptic feedback.
- Fully edge-to-edge transparent system navigation bar without opaque bars or docked slots.

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
│   └── build.yml               # Automated GitHub Actions workflow to build release & debug APKs
├── app/                        # Native Android Application module
│   ├── src/main/
│   │   ├── AndroidManifest.xml # Permissions, activities, receivers, and widget providers
│   │   ├── java/com/vibeschedule/app/
│   │   │   ├── data/           # ScheduleRepository & SharedPreferences storage
│   │   │   ├── model/          # ScheduleRule, SoundMode data models
│   │   │   ├── receiver/       # AlarmReceiver, BootReceiver, QuickMuteReceiver
│   │   │   ├── scheduler/      # AlarmScheduler (Exact AlarmManager integration)
│   │   │   ├── ui/             # Jetpack Compose UI (Screens, Popup Activity, Theme)
│   │   │   ├── util/           # NotificationHelper, SoundModeHelper, QuickMuteHelper
│   │   │   └── widget/         # VibeWidgetProvider (2x1 AppWidget)
│   │   └── res/                # Vector drawables, widget layouts, strings, and icons
│   └── build.gradle.kts        # Android module build configuration (v1.4.24)
├── build.gradle.kts            # Root Gradle build script
├── gradle.properties           # Gradle JVM settings
└── settings.gradle.kts         # Root Gradle settings
```

---

## 🚀 Building from Source

Ensure **JDK 17** is installed and configured:

```bash
# Clone the repository
git clone https://github.com/Abhinav2k/vibeschedule.git
cd vibeschedule

# Build release and debug APKs using Gradle
gradle assembleRelease assembleDebug
```

Compiled APKs will be located at:
```text
app/build/outputs/apk/release/app-release.apk
app/build/outputs/apk/debug/app-debug.apk
```

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
