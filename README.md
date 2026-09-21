# 📳 VibeSchedule (React Web Edition)

A modern, lightweight automated ringer and quiet hours scheduler featuring an obsidian liquid dark glass interface. Ported from the original Jetpack Compose Android application to **React 18 + Vite + Tailwind CSS**.

---

## ✨ Features

- ⏰ **Automated Scheduling**: Set custom time slots (e.g., *Mon–Fri 09:00 to 17:00*) to automatically switch to **Vibrate** or **Silent (DND)**, and restore to **Normal Ring** when the slot ends. Supports overnight schedules crossing midnight.
- ⏱️ **Quick Mute (One-Tap)**: Need quiet right now? Instant temporary vibration timers for 15 min, 30 min, 1 hour, or 2 hours with real-time countdown. Includes conflict detection and override warnings if a scheduled rule is already running.
- ⏸️ **Pause Until Next :00**: Pause active rules or rules starting within 20 minutes until the top of the next hour with one tap.
- 🔔 **Android Status Notification Simulation**: Interactive persistent status notification bar with real-time progress bar, remaining time, mode badge, **"End Now"**, and **"Skip to :00"** buttons.
- 📱 **Android 2×1 Home Widget Preview**: Test the interactive widget with real-time running status ("● Schedule running", "Skip to :00", "End Now").
- 💎 **Liquid Glass UI**: Faithful replication of the original Jetpack Compose obsidian glass theme, pulsing glowing indicator, pill switches, and bouncy spring tactile feedback.
- 💾 **Local Persistence**: Schedule rules and notification settings persist across sessions in local storage.

---

## 🛠️ Tech Stack

- **Framework**: React 18, Vite
- **Styling**: Tailwind CSS v4, custom obsidian glass backdrop filters & gradients
- **Icons**: Lucide React
- **Language**: TypeScript
