# VibeSchedule × Gemini 4 Argon — UI Redesign Prompt Pack

Built from your repo (`Abhinav2k/vibeschedule`, branch `main`, v1.4.35) and your 7 screenshots. Everything in here was checked against your real code and screenshots, not guessed.

**Why it's written this way:** Gemini is weak at *inventing* a design but decent at *implementing* a precise one. So this prompt hands it a complete, pixel-level design system (colors, type, geometry, states, copy) and asks it only to implement. It also forces phased, complete-file output so nothing gets truncated, and keeps the project compiling after every phase.

**Contents**
- **Part A** — Setup (for you, not for Gemini)
- **Part B** — The prompt (everything between the two COPY markers)
- **Appendix 1** — Follow-up prompt templates (continue, compile error, visual diff, polish…)
- **Appendix 2** — Re-bundle your source after you apply changes

---

# PART A — Setup (for you, not for Gemini)

## A1. What to attach in AI Studio

| Attach | Notes |
|---|---|
| The 7 screenshots, **in this order** | So Gemini can call them S1–S7 (table below) |
| `vibeschedule_source_bundle.txt` | Your whole codebase in one file (53 files, signing passwords redacted) |
| *(optional)* this `.md` file | Or just paste Part B as your first message |

| Tag | Your file | What it shows |
|---|---|---|
| S1 | `1000057810.jpg` | Home — idle, "Upcoming" state |
| S2 | `1000057812.jpg` | Schedules tab (Quick Mute + cards + FAB overlap) |
| S3 | `1000057814.jpg` | "New Schedule" dialog |
| S4 | `1000057818.jpg` | Home — schedule active (10m remaining) |
| S5 | `1000057816.jpg` | Settings |
| S6 | `1000057820.png` | Home-screen widget, active state (Skip + Cancel buttons over wallpaper) |
| S7 | `1000057822.png` | Notification card (lock screen / shade) |

## A2. Run settings

- Model: **Gemini 4 Argon**. Thinking: **highest**. Max output tokens: **the maximum offered** (a single reply is far smaller than the 1M context, which is why the prompt works in phases).
- Leave temperature at default. If code quality gets inconsistent, try 0.7.
- Tools like code execution or search grounding aren't needed.
- Paste Part B as your **first message**. If AI Studio rejects `.md` uploads, rename to `.txt` or paste the text.

**Mini-prompt**, if you upload this file instead of pasting:

```
Read the attached VibeSchedule_Gemini_Redesign_Prompt.md completely. Part B is your brief; ignore Part A and the appendices. Study the 7 screenshots (S1–S7) and the attached source bundle too. Then begin with Phase 0 exactly as the brief says.
```

## A3. Working loop (every phase)

1. Gemini replies with that phase's **complete files**.
2. Paste each into Android Studio (new file: right-click the package → New → Kotlin File; paste everything; delete files marked `DELETE`).
3. Build. If it fails, use the **Compile error** template (Appendix 1).
4. Run on your phone, screenshot it, and use the **Visual diff** template if something looks off.
5. Commit to git, then reply `continue`.

## A4. Before Phase 1: add 2 font files (2 minutes)

The prompt replaces SF Pro with two open-licence Google fonts. Download from fonts.google.com, open the zip, take the **variable** `.ttf` of each, rename exactly, and put both in `app/src/main/res/font/`:

| Family | Take this file from the zip | Rename to |
|---|---|---|
| Bricolage Grotesque | `BricolageGrotesque[opsz,wdth,wght].ttf` | `bricolage_grotesque.ttf` |
| DM Sans | `DMSans[opsz,wght].ttf` (not the Italic one) | `dm_sans.ttf` |

Then delete `sf_pro_regular.otf`, `sf_pro_medium.otf`, `sf_pro_semibold.otf`, `sf_pro_bold.otf` — **after Phase 8**, when nothing references them.
(Fallback pair with simple static files, if variable fonts misbehave: Sora + Manrope. Tell Gemini and it will switch.)

## A5. Two repo-hygiene notes (not UI, but worth fixing)

- **Your signing key is public.** `app/release.keystore` is committed and `app/build.gradle.kts` hard-codes both passwords. Anyone can sign an APK with your key. Fix: generate a new keystore, keep it out of git (`.gitignore`), and read passwords from `local.properties` or GitHub Secrets. Existing users would need to reinstall once, because the signature changes.
- **SF Pro is Apple's proprietary font.** Its licence only covers Apple-platform software and mockups, so shipping it in an Android APK in a public MIT repo is a licensing risk. The redesign removes it.

---

▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ **COPY EVERYTHING BELOW THIS LINE INTO GEMINI** ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼

# PART B — THE PROMPT

# BRIEF: Complete UI redesign of “VibeSchedule”
**Android · Kotlin · Jetpack Compose · Material 3 · repo `github.com/Abhinav2k/vibeschedule`**

Attached: seven screenshots **S1–S7** (order: S1 Home idle · S2 Schedules · S3 New-Schedule dialog · S4 Home active · S5 Settings · S6 home-screen widget · S7 notification card) and `vibeschedule_source_bundle.txt` (the full codebase; each file starts with `===== FILE: <path> =====`). The screenshots are ground truth for how the app looks **today**. The source bundle is ground truth for how it works.

## Table of contents
1 Role · 2 Output protocol · 3 Hard constraints · 4 Product context · 5 Codebase map · 6 Audit of the current UI · 7 Design direction · 8 Design tokens · 9 Component specs · 10 Screen specs · 11 Notification · 12 Widget · 13 Quick-timer popup · 14 Phases · 15 Acceptance checklist · 16 Kickoff

---

## 1. Role

You are a principal Android product designer and a senior Jetpack Compose engineer in one person. A separate senior design lead has already made every high-level design decision; they are written in this brief. Your job is to **implement them faithfully, completely and with craft**.

- Where the spec is precise, follow it exactly (hex values, dp, sp, copy, states).
- Where it is silent, choose the option most consistent with the system. Do **not** invent a new visual language, do **not** “improve” the concept, and do **not** fall back to default Material look.
- Where it is ambiguous, decide, log it in one line under **Decisions** in your reply, and move on.
- Do not ask me questions unless something blocks compilation (e.g. a file I forgot to attach).

**Priorities. When two conflict, the lower number wins:**
1. The project **compiles** after every phase (I paste code into Android Studio and build).
2. App **behaviour is untouched**. This is a UI-only redesign.
3. The spec is followed exactly.
4. **Every file is complete.** No truncation, no placeholders.
5. Craft: no text-wrapping bugs, no overlaps, 48dp touch targets, AA contrast, tasteful motion.

## 2. Output protocol (non-negotiable)

1. **Work in the numbered phases in §14.** In each reply, output as many *complete* phases as fit. Never start a file you can't finish. If you are running out of room, finish the current file, then end with:
   `⏭ NEXT: Phase N — <file list>` and wait for me to say “continue”.
2. **Every file is delivered in full**: header line `#### path/to/File.kt — NEW | REPLACE | DELETE`, then one fenced block (```kotlin or ```xml). No diffs, no “…”, no “// rest unchanged”, no “TODO”, no pseudo-code. If a file is unchanged, write `UNCHANGED` and nothing else.
3. **Imports:** include every import. Use explicit imports for icons, `Color`, `Brush`, `Shape` and anything ambiguous; wildcards are fine for `androidx.compose.foundation.layout.*` and `androidx.compose.runtime.*`.
4. **Start each phase with “Plan & Decisions”** (≤ 200 words): files you'll touch, any ambiguity you resolved. Then the code. **End with “Self-check”**: the relevant items from §15 marked ✅ or ⚠️ with a one-line reason for every ⚠️.
5. **Think before you write.** Spend your thinking budget on (a) the call-graph of every composable you change, (b) text-fit math so nothing wraps unexpectedly, and (c) compile-correctness against the pinned versions in §3.1. Do not paste your reasoning into the reply.
6. **Every phase must leave the project compiling.** Keep deprecated shims for old symbols in `ui/theme/LegacyAliases.kt` until the phase that removes their last user (final removal in Phase 8). If you change a composable's signature, update **all** call sites in the same phase.
7. **No new Gradle dependencies.** If you believe one is unavoidable, stop and tell me the exact line and why. Never output `app/build.gradle.kts` in full (it holds signing config); show only changed lines if essential.
8. **No chatter.** No apologies, no recap of this brief, no marketing language.

## 3. Hard constraints

### 3.1 Pinned toolchain (from the build files)

| Item | Value |
|---|---|
| Language | Kotlin **1.9.24** (K1; no Kotlin-2-only syntax), JDK 17 |
| Android Gradle Plugin | 8.4.2 |
| `compileSdk` / `targetSdk` / `minSdk` | 34 / 34 / **26** |
| Compose compiler | 1.5.14 |
| Compose BOM | **2024.05.00** → Compose UI/Foundation/Animation **1.6.x**, Material3 **1.2.x** |
| Other deps | core-ktx 1.13.1 · lifecycle-runtime-ktx 2.8.1 · lifecycle-viewmodel-compose 2.8.1 · activity-compose 1.9.0 · `material-icons-extended` · **Haze `dev.chrisbanes.haze:haze-android:0.7.3`** · Gson 2.11.0 |
| **Not present** | navigation-compose, lifecycle-runtime-compose, Glance, core-splashscreen, coil, hilt, room |

### 3.2 APIs that DO NOT exist in these versions (do not use)

| Don't use | Why | Use instead |
|---|---|---|
| `Modifier.animateItem()` | Foundation 1.7+ | `Modifier.animateItemPlacement()` + `@OptIn(ExperimentalFoundationApi::class)` |
| `SharedTransitionLayout`, `Modifier.sharedElement` | Compose 1.7+ | `AnimatedContent` / `AnimatedVisibility` |
| M3 Expressive: `FloatingToolbar`, `ButtonGroup`, `LoadingIndicator`, `MaterialExpressiveTheme`, `MotionScheme`, `TimePickerDialog` | Material3 1.3/1.4+ | custom composables (specified below) |
| `collectAsStateWithLifecycle()` | `lifecycle-runtime-compose` is not a dependency | `collectAsState()` |
| `HapticFeedbackType.Confirm / Reject / ToggleOn / ToggleOff / SegmentTick …` | Compose 1.7+. Only `LongPress` and `TextHandleMove` exist here | `LocalView.current.performHapticFeedback(HapticFeedbackConstants.X)` with `Build.VERSION` guards, wrapped in `Haptics.kt` |
| Haze 1.x API (`hazeSource`, `hazeEffect`, `HazeMaterials`) | Haze ≥ 1.0 | Haze **0.7.3**: `Modifier.haze(state)` and `Modifier.hazeChild(state, shape, style = HazeStyle(tint, blurRadius, noiseFactor))` |
| `TextAutoSize` / `BasicText(autoSize=…)` | Foundation 1.8 | write the `AutoSizeText` helper in §9 |
| `Modifier.blur()` as real blur on API < 31 | no-op there | don't rely on it; Haze needs API 31+ for real blur, so provide an opaque fallback |

If a composable or API sits behind `@ExperimentalMaterial3Api`, `@ExperimentalFoundationApi`, `@ExperimentalLayoutApi` or `@ExperimentalTextApi`, add the right `@OptIn`. Use `Color(0xFF……)` with the **FF alpha** for opaque colors (a missing alpha makes colors invisible). If you're not 100% sure an API exists in the pinned versions, use a simpler API you are sure of.

### 3.3 Frozen contract — behaviour you must not change

You may only change **how things look and are laid out**. These stay exactly as they are:

- **Package/ids:** `com.vibeschedule.app`, applicationId, versionCode/versionName, all manifest components, permissions, intent-filters, receivers, the widget provider declaration.
- **Data & logic:** `model/ScheduleRule.kt`, `model/SoundMode.kt`, `data/ScheduleRepository.kt`, `scheduler/AlarmScheduler.kt`, `receiver/*`, `util/QuickMuteHelper.kt`, `util/SoundModeHelper.kt`, `widget/WidgetRefresher.kt`, `ui/MainViewModel.kt` (its public API is listed in §5.2 — only *consume* it).
- **Notification plumbing:** `CHANNEL_ID = "vibe_schedule_active_v8"`, `NOTIFICATION_ID = 8823`, PendingIntent request codes 1000/1001/1002, intent actions, builder semantics (ongoing, silent, chronometer countdown, progress, public version), and the signatures of `showActiveRuleNotification`, `showActiveStatusNotification`, `dismissNotification`, `createNotificationChannel`.
- **Widget plumbing:** the actions `ACTION_WIDGET_CANCEL_ACTIVE`, `ACTION_WIDGET_SKIP_PERIOD`, `ACTION_SYNC_APP_STATE`, and these layout view IDs referenced from Kotlin: `widget_root`, `layout_widget_idle`, `btn_widget_ring`, `layout_widget_timer`, `btn_widget_timer_cancel`, `layout_widget_active`, `btn_widget_skip`, `btn_widget_cancel`. (You may add IDs; never remove or rename these.)
- **Notification layout IDs referenced from Kotlin:** `notif_root`, `notif_title`, `notif_time_text`, `notif_mode_badge`, `notif_progress`, `notif_btn_skip`, `notif_btn_cancel`.
- **Settings storage:** `PREFS_SETTINGS`, `PREF_NOTIF_HIGH_PRIORITY`, `getNotifHighPriority()`, `setNotifHighPriority()` (defined in `SettingsScreen.kt`, imported by `NotificationHelper`). Keep them, in the same package, same names.
- **Behaviour:** ViewModel is created with `by viewModels()` in `MainActivity`; navigation stays a simple `selectedTab` state (no navigation library); `QuickTimerPopupActivity` still calls `QuickMuteHelper.startQuickMute(context, minutes)` and finishes.

### 3.4 Feature parity — every one of these must remain reachable

| # | Feature | Today |
|---|---|---|
| 1 | Current ringer mode indicator (Normal / Vibrate / Silent); tap refreshes (`evaluateStatus()`) | top-bar chip |
| 2 | Active schedule: name, remaining time, progress, end time, **End now** (`stopActiveSchedule`) | Home hero |
| 3 | **Skip** to next :00 (`skipPeriod`), eligible only when `isPauseEligible`; shows “skipped until …” + **Restore** (`restoreSchedule`) when paused | Home |
| 4 | Upcoming schedule (name, start time, countdown) | Home hero |
| 5 | Standby / Stopped / Skipped states with **Restore** | Home hero |
| 6 | Quick Mute active card: mm:ss countdown, end clock, **Cancel** (`cancelQuickMute`) | Home |
| 7 | Quick Mute presets 15m / 30m / 1h / 2h + **Custom** (1–1440 min; steppers −15/−5/+5/+15; presets 5/10/20/45/90; exact-minutes field; shows end time; “Start (…)”) | Schedules |
| 8 | Quick Mute **conflict** card with *Keep current* / *Override (Nm)* (`dismissQuickMuteConflict`, `confirmQuickMuteOverride`) | Schedules |
| 9 | Schedule list with enable toggle (`toggleSchedule`), overflow **Edit / Delete** (`deleteSchedule`), tap-to-edit | Schedules |
| 10 | Empty state for zero schedules | Schedules |
| 11 | Add / Edit schedule: name (blank → “Scheduled Quiet”), start & end time (with quick AM/PM flip), repeat presets Daily / Weekdays / Weekends, per-day toggles, sound mode Vibrate / Silent, Save disabled when no day selected | dialog |
| 12 | Permission banner: DND access, exact alarms (API 31+), with deep-links to the right settings page | Schedules |
| 13 | Settings: lock-screen notification switch (writes pref + `createNotificationChannel`), “Open system notification settings” intent (with fallback), About + `BuildConfig.VERSION_NAME` | Settings |
| 14 | POST_NOTIFICATIONS permission request on first launch (`RequestNotificationPermission`) | MainActivity |
| 15 | Floating bottom navigation, 3 tabs: Home / Schedules / Settings, animated tab transition, haptic on tab change | dock |
| 16 | Notification card with mode, title, time left, progress, **Skip** + **End now** actions | NotificationHelper |
| 17 | Widget (2×1): idle ring button → opens `QuickTimerPopupActivity`; quick-timer-active cancel; schedule-active Skip + Cancel | widget |
| 18 | `QuickTimerPopupActivity`: presets 15m/30m/45m/1h/2h/4h → starts quick mute, Toast, finish; scrim tap dismisses | popup |

### 3.5 What you MAY change

All of `ui/**` (theme, components, screens, activities), `res/values/{colors,strings,themes}.xml` (+ `values-night`, `values-v31`), `res/font`, `res/layout/{notification_*,widget_*}.xml`, notification/widget drawables (+ `-night` variants), the adaptive-icon resources (optional), and the **visual** parts of `util/NotificationHelper.kt` (RemoteViews wiring, colors, which views are shown, removing `setLargeIcon`). Moving or renaming UI composables is fine as long as every call site compiles.

### 3.6 Licensing

`res/font/sf_pro_*.otf` is Apple's proprietary San Francisco font; it must not ship in this Android app. Replace it with the open-licence (OFL) fonts **Bricolage Grotesque** and **DM Sans** (variable `.ttf`, files named `bricolage_grotesque.ttf` and `dm_sans.ttf` in `res/font`). Reference only those two resource names. Never reference `R.font.sf_pro_*` after Phase 1.

---

## 4. Product context

**VibeSchedule** automatically switches a phone's ringer between **Normal (ring)**, **Vibrate** and **Silent (DND)** on a weekly schedule, then restores Normal afterwards. It also has one-tap **Quick Mute** timers, a **Skip** (ring until the next :00, then quiet resumes), an **End now**, a lock-screen/notification card with the same controls, and a 2×1 home-screen widget.

**Users:** students and office workers (the owner uses it for college lectures: “College 9:00 AM–12:00 PM Mon–Thu”, “College 1:00–4:00 PM”, “Scheduled Quiet 9:00 AM–12:20 PM Fri”). Often in a lecture or meeting, one-handed, in dim light, wanting to glance and act in under two seconds.

**The three jobs of the app, in priority order**
1. **Glance:** “Is my phone quiet right now, and when does that change?”
2. **Override fast:** skip to the next :00, end now, quick-mute for N minutes.
3. **Maintain rules:** add / edit / enable / delete weekly schedules.

**Home states** (all driven by existing ViewModel flows; every one needs a designed state): **A** schedule active · **B** upcoming schedule · **C** quick mute active · **D** skipped (paused) · **E** stopped (ended early, `hasDismissedSchedule`) · **F** idle / standby (nothing active, nothing upcoming today) · **G** no schedules exist at all.

**Tone:** calm, quiet, precise. A little playfulness comes from motion and haptics (it *is* a vibration app), never from jokey copy. Sentence case everywhere; plain verbs; no filler.

---

## 5. Codebase map

### 5.1 Files

| Path (under `app/src/main/…`) | Role | Action |
|---|---|---|
| `java/com/vibeschedule/app/ui/MainActivity.kt` | App shell: Scaffold, top bar, tab `AnimatedContent`, FAB, dock, Haze state, dialog host, notification-permission request | REPLACE |
| `ui/MainViewModel.kt` | State + actions (ticks every second via `evaluateStatus()`) | FROZEN |
| `ui/screens/HomeScreen.kt` | Home (quick-mute card, hero, skip card) | REPLACE |
| `ui/screens/SchedulesScreen.kt` | Quick-mute section, conflict card, custom-mute dialog, list, empty state | REPLACE |
| `ui/screens/SettingsScreen.kt` | Settings + the pref helper functions | REPLACE (keep helpers) |
| `ui/components/ScheduleCard.kt` | Schedule list item | REPLACE |
| `ui/components/AddEditScheduleDialog.kt` | Add/edit `AlertDialog` + legacy `android.app.TimePickerDialog` | REPLACE → sheet |
| `ui/components/GlassComponents.kt` | Floating dock (Haze + AGSL shaders) + dead code `GlassCard`, `LiquidTabSwitcher`, `GlowingIndicator` | REPLACE → `VibeDock.kt` |
| `ui/components/PermissionBanner.kt` | DND / exact-alarm banner | REPLACE |
| `ui/QuickTimerPopupActivity.kt` | Widget-launched popup | REPLACE |
| `ui/theme/{Color,Type,Theme}.kt` | Theme (dynamic color ON by default; SF Pro) | REPLACE |
| `util/NotificationHelper.kt` | Builds the notification (custom RemoteViews) | visual edits only |
| `widget/VibeWidgetProvider.kt` | Widget state switching | FROZEN (layout IDs frozen) |
| `res/layout/notification_vibe_collapsed.xml` (used), `notification_vibe_expanded.xml` (unused) | Notification RemoteViews | REPLACE |
| `res/layout/widget_vibe_schedule.xml` | Widget (3 states) | REPLACE (IDs frozen) |
| `res/drawable/*` (`widget_btn_*`, `notif_*`, `ic_widget_*`, `ic_launcher_foreground`) | Button/progress/icon drawables | REPLACE / add `-night` variants |
| `res/values/{colors,strings,themes}.xml` | Resources | REPLACE |
| `res/font/sf_pro_*.otf` | Apple font | REMOVE (Phase 8) |

### 5.2 State and actions you consume (do not change)

```kotlin
// MainViewModel (AndroidViewModel) — StateFlows
schedules: StateFlow<List<ScheduleRule>>
currentSoundMode: StateFlow<SoundMode>            // NORMAL / VIBRATE / SILENT
activeSchedule: StateFlow<ScheduleRule?>
activeRemainingMinutes: StateFlow<Int?>
upcomingSchedule: StateFlow<Pair<ScheduleRule, Int>?>   // Int = minutes until start (today only)
isPauseEligible: StateFlow<Boolean>               // active OR starts within 20 min
pausedUntilMillis: StateFlow<Long?>               // Skip in effect
hasDismissedSchedule: StateFlow<Boolean>          // End now in effect
quickMuteUntilMillis: StateFlow<Long?>
quickMuteRemainingSeconds: StateFlow<Int>
quickMuteConflictInfo: StateFlow<QuickMuteConflict?>   // QuickMuteConflict(title, message, pendingMinutes)

// Actions
evaluateStatus()  requestQuickMute(minutes)  confirmQuickMuteOverride()  dismissQuickMuteConflict()
cancelQuickMute()  skipPeriod()  pauseUntilNextOClock()  restoreSchedule()  cancelPause()
stopActiveSchedule()  addSchedule(rule)  updateSchedule(rule)  toggleSchedule(id, enabled)  deleteSchedule(id)

// Models
data class ScheduleRule(id: String, title: String, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int,
    daysOfWeek: List<Int> /* Calendar.SUNDAY=1 … SATURDAY=7 */, targetMode: SoundMode = VIBRATE,
    revertMode: SoundMode = NORMAL, isEnabled: Boolean = true)   // + formatStartTime(), formatEndTime(), daysSummary()
enum class SoundMode(ringerMode, displayName) { VIBRATE("Vibrate"), SILENT("Silent (DND)"), NORMAL("Normal (Ring)") }
```

Overnight rule: `start >= end` means it runs past midnight (the ViewModel already handles it). Week display order is Monday-first: `[2,3,4,5,6,7,1]`.

**Rules for consuming state**
- Screens become **stateless content composables** (`HomeContent(state, actions)`) wrapped by thin stateful ones (`HomeScreen(viewModel, …)`) that `collectAsState()` and build a UI-state object. This makes `@Preview`s possible without a ViewModel.
- Scope state reads narrowly: `quickMuteRemainingSeconds` changes **every second**; only the composables that show it may read it.
- The ViewModel already ticks every second; for “now” on the dial use a UI-level **minute ticker** (`LaunchedEffect` that delays to the next minute boundary), not a new ViewModel flow.
