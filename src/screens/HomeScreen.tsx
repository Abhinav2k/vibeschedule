import React from 'react';
import { Timer, X, SkipForward, RotateCcw, Lock, Volume2, BellOff } from 'lucide-react';
import { ScheduleRule } from '../types';
import { MD3Card } from '../components/MD3Card';
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
    <div className="w-full max-w-md mx-auto space-y-4 px-4 pb-20 pt-2">
      {/* 1. Quick Mute Card */}
      {isQuickMuteActive && (
        <MD3Card
          variant="elevated"
          className="rounded-[28px] bg-[#5d3c56] text-[#ffd7f3] p-5 shadow-md border border-[#e5bad8]/30"
        >
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Timer className="w-4 h-4 text-[#ffd7f3]" />
              <span className="text-xs font-semibold text-[#ffd7f3]">
                Quick Mute
              </span>
            </div>
            <button
              type="button"
              onClick={onCancelQuickMute}
              className="p-1 rounded-full text-[#ffd7f3]/70 hover:text-white transition-colors"
              aria-label="Cancel quick mute"
            >
              <X className="w-4 h-4" />
            </button>
          </div>

          <div className="flex items-end justify-between mt-3">
            <div>
              <div className="text-4xl font-bold tracking-tight font-mono text-[#ffd7f3]">
                {qmTimeStr}
              </div>
              <p className="text-xs text-[#ffd7f3]/80 mt-1">
                Ends at {qmEndClockStr}
              </p>
            </div>
            <button
              type="button"
              onClick={onCancelQuickMute}
              className="h-7 px-3.5 rounded-full bg-[#e5bad8] text-[#45263f] text-xs font-semibold hover:bg-[#f0c8e4] transition-all active:scale-95 flex items-center justify-center cursor-pointer shadow-xs"
            >
              Cancel
            </button>
          </div>
        </MD3Card>
      )}

      {/* 2. Primary Status Card */}
      <MD3Card
        variant="elevated"
        isActive={activeSchedule !== null && !isPaused}
        className={`rounded-[28px] p-6 text-left ${
          activeSchedule && !isPaused
            ? 'bg-[#283b9f]/30 border border-[#bac3ff]/40'
            : 'bg-[#1e1f23] border border-[#45464f]/30'
        }`}
      >
        {activeSchedule && !isPaused ? (
          <div>
            <div className="flex items-center justify-between">
              <div className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-[#bac3ff]/20 text-xs font-semibold text-[#bac3ff]">
                <BellOff className="w-3.5 h-3.5" />
                <span>{activeSchedule.title}</span>
              </div>
              <button
                type="button"
                onClick={onPauseUntilNextOClock}
                className="text-xs font-semibold px-3 py-1 rounded-full bg-[#1e1f23] hover:bg-[#292a2d] text-[#e3e2e6] border border-[#45464f] transition-all"
              >
                Skip
              </button>
            </div>

            <div className="mt-5">
              <div className="text-5xl font-black text-white tracking-tight">
                {activeRemainingMinutes && activeRemainingMinutes >= 60
                  ? `${Math.floor(activeRemainingMinutes / 60)}h ${activeRemainingMinutes % 60}m`
                  : `${activeRemainingMinutes || 0}m`}
              </div>
              <p className="text-xs text-[#bac3ff]/80 mt-1">
                remaining
              </p>
            </div>

            {/* Linear Progress Bar */}
            <div className="w-full bg-[#45464f]/40 h-2 rounded-full overflow-hidden mt-4">
              <div
                className="bg-[#bac3ff] h-full rounded-full transition-all duration-300"
                style={{
                  width: `${Math.min(100, Math.max(10, ((activeRemainingMinutes || 30) / 120) * 100))}%`,
                }}
              />
            </div>

            <div className="mt-4 text-xs font-medium text-[#c6c5d0]">
              Ends at{' '}
              <span className="font-semibold text-[#e3e2e6]">
                {formatTime24(activeSchedule.endHour, activeSchedule.endMinute)}
              </span>
            </div>
          </div>
        ) : upcomingSchedule && !isPaused ? (
          <div>
            <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#434659] text-[#dfe1f9] text-xs font-medium">
              <span>Upcoming</span>
            </div>

            <div className="mt-4">
              <h2 className="text-2xl font-bold text-white tracking-tight">
                {upcomingSchedule.rule.title}
              </h2>
              <p className="text-sm font-medium text-[#c6c5d0] mt-1.5">
                {upcomingSchedule.minutesLeft <= 20
                  ? `Starts in ${upcomingSchedule.minutesLeft}m (${formatTime24(
                      upcomingSchedule.rule.startHour,
                      upcomingSchedule.rule.startMinute
                    )})`
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
              <div className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#292a2d] text-[#c6c5d0] text-xs font-medium">
                <Volume2 className="w-3.5 h-3.5 text-[#bac3ff]" />
                <span>{isPaused ? 'Skipped' : 'Standby'}</span>
              </div>

              {isPaused && (
                <button
                  type="button"
                  onClick={onCancelPause}
                  className="flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#bac3ff] text-[#08218a] font-semibold text-xs hover:bg-[#c9d0ff] transition-all cursor-pointer"
                >
                  <RotateCcw className="w-3 h-3" />
                  Restore
                </button>
              )}
            </div>

            <div className="mt-4">
              <h2 className="text-2xl font-bold text-white tracking-tight">
                {isPaused ? 'Schedule Skipped' : 'No Active Schedule'}
              </h2>
              <p className="text-sm text-[#c6c5d0] mt-1">
                {isPaused
                  ? `Normal ring on until ${pausedEndClockStr}`
                  : 'Normal ring'}
              </p>
            </div>
          </div>
        )}
      </MD3Card>

      {/* 3. Skip Period Card */}
      <MD3Card
        variant="outlined"
        className={`rounded-[24px] p-5 ${
          isPaused ? 'bg-[#1e1f23] border-[#bac3ff]/40' : 'bg-[#1a1b1e] border-[#45464f]'
        }`}
      >
        <div className="flex items-center justify-between gap-3">
          <div className="flex-1 min-w-0">
            <h3
              className={`text-sm font-semibold ${
                isPauseEligible || isPaused ? 'text-[#e3e2e6]' : 'text-[#8f909a]'
              }`}
            >
              {isPaused ? 'Period Skipped' : 'Skip Period'}
            </h3>
            <p
              className={`text-xs mt-0.5 ${
                isPaused || isPauseEligible ? 'text-[#c6c5d0]' : 'text-[#8f909a]/60'
              }`}
            >
              {isPaused
                ? `Ring on until ${pausedEndClockStr}`
                : isPauseEligible
                ? 'Unmute until next hour'
                : 'Available during active schedule'}
            </p>
          </div>

          <div>
            {isPaused ? (
              <button
                type="button"
                onClick={onCancelPause}
                className="flex items-center gap-1.5 h-8 px-3.5 rounded-full bg-[#bac3ff] text-[#08218a] font-semibold text-xs hover:bg-[#c9d0ff] transition-all active:scale-95 shadow-xs"
              >
                <RotateCcw className="w-3.5 h-3.5" />
                Restore
              </button>
            ) : (
              <button
                type="button"
                onClick={onPauseUntilNextOClock}
                disabled={!isPauseEligible}
                className={`flex items-center gap-1.5 h-8 px-3.5 rounded-full text-xs font-semibold transition-all ${
                  isPauseEligible
                    ? 'bg-[#bac3ff] text-[#08218a] hover:bg-[#c9d0ff] active:scale-95 cursor-pointer shadow-xs'
                    : 'bg-[#292a2d] text-[#8f909a] cursor-not-allowed border border-[#45464f]/30'
                }`}
              >
                {!isPauseEligible ? (
                  <Lock className="w-3.5 h-3.5 text-[#8f909a]" />
                ) : (
                  <SkipForward className="w-3.5 h-3.5" />
                )}
                Skip
              </button>
            )}
          </div>
        </div>
      </MD3Card>
    </div>
  );
};
