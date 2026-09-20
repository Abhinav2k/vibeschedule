# 📳 VibeSchedule

A modern, lightweight, and battery-friendly Android application built with **Jetpack Compose** and **Material 3** that automates switching your phone to **Vibration** or **Silent (DND)** mode on scheduled times and days.

---

## ✨ Features

- ⏰ **Automated Scheduling**: Set custom time slots (e.g., *Mon–Fri 09:00 to 17:00*) to automatically switch to **Vibrate** or **Silent**, and restore to **Normal Ring** when the slot ends.
- ⚡ **Zero Battery Impact**: Uses Android's `AlarmManager.setExactAndAllowWhileIdle()` to sleep until the exact trigger second without background battery drain.
- 🔁 **Survives Phone Reboots**: Registered `BootReceiver` automatically re-arms your active schedules whenever your phone restarts.
- ⏱️ **Quick Mute (One-Tap)**: Need quiet right now? Tap 15 min, 30 min, 1 hour, or 2 hours for instant temporary vibration mode.
- 🔔 **Discreet Status Notification**: Shows a quiet persistent notification while active with a 1-tap **"Revert to Normal"** button.

---

## 🚀 How to Build the APK (Zero-Install Cloud Build)

Since you don't need Android Studio installed, **GitHub Actions** compiles the `.apk` file for free in the cloud.

### Step 1: Initialize Git and Commit
Open your terminal in this directory and run:

```bash
git init
git add .
git commit -m "Initial commit of VibeSchedule app"
```

### Step 2: Push to GitHub
1. Go to [GitHub](https://github.com/new) and create a new repository (e.g., `vibeschedule`).
2. Link your local project and push:
```bash
git branch -M main
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/vibeschedule.git
git push -u origin main
```

### Step 3: Download Your APK!
1. Go to your GitHub repository in your browser.
2. Click on the **Actions** tab at the top.
3. You will see the **"Build Android APK"** workflow running automatically.
4. Once it finishes (takes ~2 minutes), click on the workflow run.
5. Under **Artifacts** at the bottom of the page, click **`VibeSchedule-Debug-APK`** to download your zip containing `app-debug.apk`.
6. Transfer or download it to your Android phone, tap to install, and you're good to go!

---

## 📱 Permissions Used & Why

| Permission | Reason |
| :--- | :--- |
| `SCHEDULE_EXACT_ALARM` | Ensures schedules trigger at the exact minute even when the device is in deep Doze mode. |
| `ACCESS_NOTIFICATION_POLICY` | Android requires this to let apps change ringer modes (Normal / Vibrate / Silent). |
| `RECEIVE_BOOT_COMPLETED` | Restores your scheduled alarms automatically when your phone restarts. |
| `POST_NOTIFICATIONS` | Displays a status notification while active with a 1-tap "Revert to Normal" button. |

---

## 🛠️ Tech Stack
- **Language**: Kotlin 1.9.24
- **UI**: Jetpack Compose (Material 3)
- **Target SDK**: Android 14 (API 34)
- **Min SDK**: Android 8.0 (API 26) - Works on 99%+ of Android phones
- **Architecture**: MVVM with StateFlow & SharedPreferences persistence
