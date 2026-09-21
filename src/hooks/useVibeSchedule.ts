import { useState, useEffect, useCallback, useMemo } from 'react';
import { ScheduleRule, SoundMode, QuickMuteConflict, AppSettings } from '../types';
import { DEFAULT_SCHEDULES } from '../data/initialData';
import { playAudioFeedback, triggerHapticFeedback } from '../utils/audioVibe';

const STORAGE_KEY_SCHEDULES = 'vibe_schedules_prefs';
const STORAGE_KEY_SETTINGS = 'vibe_settings_prefs';

export function formatTime24(hour: number, minute: number): string {
  return `${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}`;
}

export function formatDaysSummary(daysOfWeek: number[]): string {
  if (daysOfWeek.length === 7) return 'Everyday';
  if (daysOfWeek.length === 5 && !daysOfWeek.includes(1) && !daysOfWeek.includes(7)) {
    return 'Weekdays (Mon-Fri)';
  }
  if (daysOfWeek.length === 2 && daysOfWeek.includes(1) && daysOfWeek.includes(7)) {
    return 'Weekends (Sat-Sun)';
  }
  const dayNames: Record<number, string> = {
    1: 'Sun',
    2: 'Mon',
    3: 'Tue',
    4: 'Wed',
    5: 'Thu',
    6: 'Fri',
    7: 'Sat',
  };
  return [...daysOfWeek]
    .sort((a, b) => a - b)
    .map((d) => dayNames[d])
    .filter(Boolean)
    .join(', ');
}

export function useVibeSchedule() {
  // 1. Schedules state
  const [schedules, setSchedules] = useState<ScheduleRule[]>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY_SCHEDULES);
      if (saved) {
        const parsed = JSON.parse(saved);
        if (Array.isArray(parsed) && parsed.length > 0) return parsed;
      }
    } catch (e) {
      console.error('Failed to load schedules from localStorage', e);
    }
    return DEFAULT_SCHEDULES;
  });

  // 2. Settings state
  const [settings, setSettings] = useState<AppSettings>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY_SETTINGS);
      if (saved) return JSON.parse(saved);
    } catch {
      // ignore
    }
    return { notifHighPriority: true };
  });

  // 3. Transient runtime states
  const [pausedUntilMillis, setPausedUntilMillis] = useState<number | null>(null);
  const [quickMuteUntilMillis, setQuickMuteUntilMillis] = useState<number | null>(null);
  const [quickMuteRemainingSeconds, setQuickMuteRemainingSeconds] = useState<number>(0);
  const [quickMuteConflictInfo, setQuickMuteConflictInfo] = useState<QuickMuteConflict | null>(null);

  // Time ticker (updates current second & minute)
  const [now, setNow] = useState<Date>(() => new Date());

  // Save schedules when changed
  const saveSchedules = useCallback((newSchedules: ScheduleRule[]) => {
    setSchedules(newSchedules);
    try {
      localStorage.setItem(STORAGE_KEY_SCHEDULES, JSON.stringify(newSchedules));
    } catch (e) {
      console.error('Failed to save schedules', e);
    }
  }, []);

  const updateSettings = useCallback((newSettings: Partial<AppSettings>) => {
    setSettings((prev) => {
      const updated = { ...prev, ...newSettings };
      try {
        localStorage.setItem(STORAGE_KEY_SETTINGS, JSON.stringify(updated));
      } catch (e) {
        console.error('Failed to save settings', e);
      }
      return updated;
    });
  }, []);

  // Timer loop - runs every 1 second
  useEffect(() => {
    const timer = setInterval(() => {
      setNow(new Date());
    }, 1000);
    return () => clearInterval(timer);
  }, []);

  // Evaluate Quick Mute countdown
  useEffect(() => {
    if (quickMuteUntilMillis !== null) {
      const diffMs = quickMuteUntilMillis - now.getTime();
      if (diffMs <= 0) {
        setQuickMuteUntilMillis(null);
        setQuickMuteRemainingSeconds(0);
        playAudioFeedback('ring');
      } else {
        setQuickMuteRemainingSeconds(Math.ceil(diffMs / 1000));
      }
    } else {
      setQuickMuteRemainingSeconds(0);
    }
  }, [now, quickMuteUntilMillis]);

  // Evaluate Pause expiration
  useEffect(() => {
    if (pausedUntilMillis !== null && now.getTime() >= pausedUntilMillis) {
      setPausedUntilMillis(null);
      playAudioFeedback('vibe');
    }
  }, [now, pausedUntilMillis]);

  // Calendar day index: JS getDay() is 0=Sun..6=Sat; Calendar.SUNDAY=1..SATURDAY=7
  const currentCalendarDay = now.getDay() === 0 ? 1 : now.getDay() + 1;
  const yesterdayJsDay = (now.getDay() + 6) % 7;
  const yesterdayCalendarDay = yesterdayJsDay === 0 ? 1 : yesterdayJsDay + 1;
  const curMinutes = now.getHours() * 60 + now.getMinutes();

  // Find active schedule
  const activeSchedule = useMemo(() => {
    const enabledRules = schedules.filter((s) => s.isEnabled);
    return (
      enabledRules.find((rule) => {
        if (!rule.daysOfWeek || rule.daysOfWeek.length === 0) return false;
        const startMin = rule.startHour * 60 + rule.startMinute;
        const endMin = rule.endHour * 60 + rule.endMinute;

        if (startMin < endMin) {
          return rule.daysOfWeek.includes(currentCalendarDay) && curMinutes >= startMin && curMinutes < endMin;
        } else {
          // Crosses midnight
          return (
            (rule.daysOfWeek.includes(currentCalendarDay) && curMinutes >= startMin) ||
            (rule.daysOfWeek.includes(yesterdayCalendarDay) && curMinutes < endMin)
          );
        }
      }) || null
    );
  }, [schedules, currentCalendarDay, yesterdayCalendarDay, curMinutes]);

  // Active remaining minutes
  const activeRemainingMinutes = useMemo(() => {
    if (!activeSchedule) return null;
    const startMin = activeSchedule.startHour * 60 + activeSchedule.startMinute;
    const endMin = activeSchedule.endHour * 60 + activeSchedule.endMinute;
    if (startMin < endMin) {
      return Math.max(1, endMin - curMinutes);
    } else {
      return Math.max(1, (endMin + 1440 - curMinutes) % 1440);
    }
  }, [activeSchedule, curMinutes]);

  // Upcoming schedule starting today
  const upcomingSchedule = useMemo<{ rule: ScheduleRule; minutesLeft: number } | null>(() => {
    const enabledRules = schedules.filter((s) => s.isEnabled);
    let nearest: { rule: ScheduleRule; minutesLeft: number } | null = null;

    for (const rule of enabledRules) {
      if (!rule.daysOfWeek.includes(currentCalendarDay)) continue;
      const startMin = rule.startHour * 60 + rule.startMinute;
      const diff = startMin - curMinutes;
      if (diff > 0) {
        if (!nearest || diff < nearest.minutesLeft) {
          nearest = { rule, minutesLeft: diff };
        }
      }
    }
    return nearest;
  }, [schedules, currentCalendarDay, curMinutes]);

  // Pause eligibility: Active right now OR starting within 20 minutes
  const isPauseEligible = Boolean(
    activeSchedule || (upcomingSchedule && upcomingSchedule.minutesLeft <= 20)
  );

  // Active simulated ringer mode
  const currentSoundMode = useMemo<SoundMode>(() => {
    if (quickMuteUntilMillis !== null) return SoundMode.VIBRATE;
    if (pausedUntilMillis !== null) return SoundMode.NORMAL;
    if (activeSchedule) return activeSchedule.targetMode;
    return SoundMode.NORMAL;
  }, [quickMuteUntilMillis, pausedUntilMillis, activeSchedule]);

  // Quick Mute trigger
  const requestQuickMute = useCallback(
    (minutes: number) => {
      triggerHapticFeedback([40]);
      const isAlreadyVibrating = currentSoundMode === SoundMode.VIBRATE || currentSoundMode === SoundMode.SILENT;

      if (activeSchedule) {
        const remMin = activeRemainingMinutes || 0;
        const remStr = remMin >= 60 ? `${Math.floor(remMin / 60)}h ${remMin % 60}m` : `${remMin}m`;
        setQuickMuteConflictInfo({
          title: 'Schedule Already Active',
          message: `'${activeSchedule.title}' is already active with ${remStr} remaining (ends at ${formatTime24(activeSchedule.endHour, activeSchedule.endMinute)}).`,
          pendingMinutes: minutes,
        });
      } else if (isAlreadyVibrating) {
        const modeLabel = currentSoundMode === SoundMode.VIBRATE ? 'Vibrate' : 'Silent';
        setQuickMuteConflictInfo({
          title: `Phone Already in ${modeLabel}`,
          message: `Your phone is already in ${modeLabel} mode. Quick Mute will set a timer to restore ring in ${minutes} minutes.`,
          pendingMinutes: minutes,
        });
      } else {
        applyQuickMute(minutes);
      }
    },
    [activeSchedule, activeRemainingMinutes, currentSoundMode]
  );

  const applyQuickMute = useCallback((minutes: number) => {
    const end = Date.now() + minutes * 60 * 1000;
    setQuickMuteUntilMillis(end);
    setQuickMuteRemainingSeconds(minutes * 60);
    setQuickMuteConflictInfo(null);
    playAudioFeedback('vibe');
  }, []);

  const confirmQuickMuteOverride = useCallback(() => {
    if (!quickMuteConflictInfo) return;
    applyQuickMute(quickMuteConflictInfo.pendingMinutes);
  }, [quickMuteConflictInfo, applyQuickMute]);

  const dismissQuickMuteConflict = useCallback(() => {
    setQuickMuteConflictInfo(null);
  }, []);

  const cancelQuickMute = useCallback(() => {
    setQuickMuteUntilMillis(null);
    setQuickMuteRemainingSeconds(0);
    playAudioFeedback('ring');
  }, []);

  // Pause action (pauses until next :00)
  const pauseUntilNextOClock = useCallback(() => {
    const nextHour = new Date(now);
    nextHour.setHours(nextHour.getHours() + 1, 0, 0, 0);
    setPausedUntilMillis(nextHour.getTime());
    playAudioFeedback('ring');
  }, [now]);

  const cancelPause = useCallback(() => {
    setPausedUntilMillis(null);
    if (activeSchedule) {
      playAudioFeedback('vibe');
    }
  }, [activeSchedule]);

  // Schedule CRUD
  const addSchedule = useCallback(
    (rule: ScheduleRule) => {
      const updated = [...schedules, rule];
      saveSchedules(updated);
      playAudioFeedback('click');
    },
    [schedules, saveSchedules]
  );

  const updateSchedule = useCallback(
    (rule: ScheduleRule) => {
      const updated = schedules.map((s) => (s.id === rule.id ? rule : s));
      saveSchedules(updated);
      playAudioFeedback('click');
    },
    [schedules, saveSchedules]
  );

  const toggleSchedule = useCallback(
    (id: string, isEnabled: boolean) => {
      const updated = schedules.map((s) => (s.id === id ? { ...s, isEnabled } : s));
      saveSchedules(updated);
      playAudioFeedback('click');
    },
    [schedules, saveSchedules]
  );

  const deleteSchedule = useCallback(
    (id: string) => {
      const updated = schedules.filter((s) => s.id !== id);
      saveSchedules(updated);
      playAudioFeedback('click');
    },
    [schedules, saveSchedules]
  );

  return {
    schedules,
    settings,
    now,
    currentSoundMode,
    activeSchedule,
    activeRemainingMinutes,
    upcomingSchedule,
    isPauseEligible,
    pausedUntilMillis,
    quickMuteUntilMillis,
    quickMuteRemainingSeconds,
    quickMuteConflictInfo,
    requestQuickMute,
    confirmQuickMuteOverride,
    dismissQuickMuteConflict,
    cancelQuickMute,
    pauseUntilNextOClock,
    cancelPause,
    addSchedule,
    updateSchedule,
    toggleSchedule,
    deleteSchedule,
    updateSettings,
  };
}
