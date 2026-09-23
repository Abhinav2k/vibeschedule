import React from 'react';
import { Timer, X, SkipForward, RotateCcw, Lock } from 'lucide-react';
import { ScheduleRule } from '../types';
import { GlassCard } from '../components/GlassCard';
import { GlowingIndicator } from '../components/GlowingIndicator';
import { formatTime24 } from '../hooks/useVibeSchedule';

interface HomeScreenProps {
  activeSchedule: ScheduleRule | null;
  activeRemainingMinutes: number | null;
  upcomingSchedule: { rule: ScheduleRule; minutesLeft: number } | null;
  isPauseEligible: boolean;
  pausedUntilMillis: number | null;
  quickMuteUntilMillis: number | null;
  quickMuteRemainingSeconds: number;
  onCancelQuickMute: () => void;
  onPauseUntilNextOClock: () => void;
  onCancelPause: () => void;
  onNavigateToSchedules: () => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  activeSchedule,
  activeRemainingMinutes,
  upcomingSchedule,
  isPauseEligible,
  pausedUntilMillis,
  quickMuteUntilMillis,
  quickMuteRemainingSeconds,
  onCancelQuickMute,
  onPauseUntilNextOClock,
  onCancelPause,
  onNavigateToSchedules,
}) => {
  const isPaused = pausedUntilMillis !== null;
  const isQuickMuteActive = quickMuteUntilMillis !== null && quickMuteRemainingSeconds > 0;

  const qmMinutes = Math.floor(quickMuteRemainingSeconds / 60);
  const qmSeconds = quickMuteRemainingSeconds % 60;
  const qmTimeStr = `${qmMinutes.toString().padStart(2, '0')}:${qmSeconds.toString().padStart(2, '0')}`;
  const qmEndClockStr = quickMuteUntilMillis
    ? new Date(quickMuteUntilMillis).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    : '';

  const pausedEndClockStr = pausedUntilMillis
    ? new Date(pausedUntilMillis).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
    : '';

  return (
    <div className="w-full max-w-md mx-auto space-y-3.5 px-4 pb-20 pt-2">
      {/* 1. Quick Mute Active Card */}
      {isQuickMuteActive && (
        <GlassCard
          isActive={true}
          className="rounded-[26px] bg-white/[0.12] border-white/40 shadow-[0_8px_32px_rgba(255,255,255,0.08)] animate-in fade-in slide-in-from-top-2 duration-300"
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Timer className="w-4 h-4 text-white" />
              <span className="text-[11px] font-bold tracking-widest text-white uppercase">
                Quick Mute Active
              </span>
            </div>
            <button
              type="button"
              onClick={onCancelQuickMute}
              className="p-1 rounded-full text-neutral-400 hover:text-white transition-colors"
              aria-label="Cancel quick mute"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          <div className="flex items-end justify-between mt-3">
            <div>
              <div className="text-4xl sm:text-5xl font-extrabold text-white tracking-tight font-mono">
                {qmTimeStr}
              </div>
              <p className="text-xs sm:text-sm text-neutral-300 mt-0.5">
                Ends at {qmEndClockStr}
              </p>
            </div>
            <button
              type="button"
              onClick={onCancelQuickMute}
              className="px-4 py-1.5 rounded-full bg-white/20 hover:bg-white/30 text-white text-xs font-semibold backdrop-blur-md transition-all active:scale-95"
            >
              Cancel
            </button>
          </div>
        </GlassCard>
      )}

      {/* 2. Primary Status Card — Hero */}
      <GlassCard
        isActive={activeSchedule !== null && !isPaused}
        className="rounded-[28px] p-6 text-left"
      >
        {activeSchedule && !isPaused ? (
          <div>
            <div className="flex items-center gap-2.5">
              <GlowingIndicator isActive={true} />
              <span className="text-sm font-medium text-neutral-300">
                {activeSchedule.title}
              </span>
            </div>

            <div className="mt-4">
              <div className="text-5xl sm:text-6xl font-black text-white tracking-tighter">
                {activeRemainingMinutes && activeRemainingMinutes >= 60
                  ? `${Math.floor(activeRemainingMinutes / 60)}h ${activeRemainingMinutes % 60}m`
                  : `${activeRemainingMinutes || 0}m`}
              </div>
              <p className="text-xs sm:text-sm font-medium text-neutral-400 mt-0.5">
                remaining
              </p>
            </div>

            <div className="mt-3 text-sm font-semibold text-neutral-300">
              ends {formatTime24(activeSchedule.endHour, activeSchedule.endMinute)}
            </div>
          </div>
        ) : upcomingSchedule ? (
          <div>
            <div className="flex items-center gap-2">
              <GlowingIndicator isActive={false} />
              <span className="text-[11px] font-bold tracking-widest text-neutral-400 uppercase">
                Upcoming
              </span>
            </div>

            <div className="mt-3">
              <h2 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
                {upcomingSchedule.rule.title}
              </h2>
              <p className="text-sm font-medium text-neutral-300 mt-1">
                {upcomingSchedule.minutesLeft <= 20
                  ? `Starts in ${upcomingSchedule.minutesLeft}m • ${formatTime24(
                      upcomingSchedule.rule.startHour,
                      upcomingSchedule.rule.startMinute
                    )}`
                  : `${formatTime24(
                      upcomingSchedule.rule.startHour,
                      upcomingSchedule.rule.startMinute
                    )} • In ${Math.floor(upcomingSchedule.minutesLeft / 60)}h ${
                      upcomingSchedule.minutesLeft % 60
                    }m`}
              </p>
            </div>
          </div>
        ) : (
          <div>
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2">
                <GlowingIndicator isActive={false} />
                <span className="text-[11px] font-bold tracking-widest text-neutral-400 uppercase">
                  {isPaused ? 'Skipped' : 'Standby'}
                </span>
              </div>
              {isPaused && (
                <button
                  type="button"
                  onClick={onCancelPause}
                  className="flex items-center gap-1 px-2.5 py-1 rounded-full bg-white/15 hover:bg-white/25 text-white font-medium text-[11px] transition-all cursor-pointer active:scale-95"
                >
                  <RotateCcw className="w-3 h-3 text-neutral-300" />
                  Restore
                </button>
              )}
            </div>

            <div className="mt-3">
              <h2 className="text-2xl sm:text-3xl font-bold text-white tracking-tight">
                {isPaused ? 'Period Skipped' : 'No Active Schedule'}
              </h2>
              <p className="text-sm font-medium text-neutral-300 mt-1">
                {isPaused ? 'Ring on until next :00' : 'Normal Ring'}
              </p>
            </div>
          </div>
        )}
      </GlassCard>

      {/* 3. Skip Action Card */}
      <GlassCard
        className={`rounded-[24px] p-5 ${
          isPaused ? 'bg-white/[0.1] border-white/25' : 'bg-white/[0.05]'
        }`}
      >
        <div className="flex items-center justify-between gap-3">
          <div className="flex-1 min-w-0">
            <h3
              className={`text-sm sm:text-base font-semibold ${
                isPauseEligible || isPaused ? 'text-white' : 'text-neutral-500'
              }`}
            >
              {isPaused ? 'Period Skipped' : 'Skip Period'}
            </h3>
            <p
              className={`text-xs mt-0.5 ${
                isPaused || isPauseEligible ? 'text-neutral-300' : 'text-neutral-500'
              }`}
            >
              {isPaused
                ? `Until ${pausedEndClockStr}`
                : isPauseEligible
                ? 'Skip until next :00'
                : 'Available during schedule'}
            </p>
          </div>

          <div>
            {isPaused ? (
              <button
                type="button"
                onClick={onCancelPause}
                className="flex items-center gap-1.5 px-3.5 py-1.5 rounded-full bg-white text-black font-semibold text-xs hover:bg-neutral-200 transition-all active:scale-95 shadow-md cursor-pointer"
              >
                <RotateCcw className="w-3.5 h-3.5 text-black" />
                Restore
              </button>
            ) : (
              <button
                type="button"
                onClick={onPauseUntilNextOClock}
                disabled={!isPauseEligible}
                className={`flex items-center gap-1.5 px-4 py-2 rounded-full text-xs font-semibold transition-all shadow-xs ${
                  isPauseEligible
                    ? 'bg-white text-black hover:bg-neutral-200 active:scale-95 cursor-pointer'
                    : 'bg-white/10 text-neutral-500 cursor-not-allowed border border-white/5'
                }`}
              >
                {!isPauseEligible ? (
                  <Lock className="w-3.5 h-3.5 text-neutral-500" />
                ) : (
                  <SkipForward className="w-3.5 h-3.5 text-black" />
                )}
                Skip
              </button>
            )}
          </div>
        </div>
      </GlassCard>

      {/* Direct link to schedules if empty or to configure */}
      <div className="pt-2 text-center">
        <button
          type="button"
          onClick={onNavigateToSchedules}
          className="text-xs text-neutral-400 hover:text-white transition-colors underline-offset-4 hover:underline"
        >
          View all scheduled rules →
        </button>
      </div>
    </div>
  );
};
