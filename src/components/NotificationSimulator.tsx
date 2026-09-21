import React, { useState } from 'react';
import { Bell, ChevronDown, ChevronUp, Clock, Volume2, VolumeX, Smartphone } from 'lucide-react';
import { SoundMode, ScheduleRule } from '../types';

interface NotificationSimulatorProps {
  activeSchedule: ScheduleRule | null;
  activeRemainingMinutes: number | null;
  quickMuteRemainingSeconds: number;
  quickMuteUntilMillis: number | null;
  pausedUntilMillis: number | null;
  onCancelActive: () => void;
  onSkipPeriod: () => void;
  highPriority: boolean;
}

export const NotificationSimulator: React.FC<NotificationSimulatorProps> = ({
  activeSchedule,
  activeRemainingMinutes,
  quickMuteRemainingSeconds,
  quickMuteUntilMillis,
  pausedUntilMillis,
  onCancelActive,
  onSkipPeriod,
  highPriority,
}) => {
  const [expanded, setExpanded] = useState(false);

  const isQuickMute = quickMuteUntilMillis !== null && quickMuteRemainingSeconds > 0;
  const isPaused = pausedUntilMillis !== null;
  const isActive = activeSchedule !== null && !isPaused;

  if (!isQuickMute && !isPaused && !isActive) {
    return null;
  }

  let title = 'VibeSchedule Active';
  let modeBadge = 'NORMAL';
  let timeContent = '';
  let progressPct = 0;
  let canSkip = false;

  if (isQuickMute) {
    title = 'Quick Mute';
    modeBadge = 'VIBRATE';
    const mins = Math.floor(quickMuteRemainingSeconds / 60);
    const secs = quickMuteRemainingSeconds % 60;
    const endStr = quickMuteUntilMillis
      ? new Date(quickMuteUntilMillis).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      : '';
    timeContent = `${mins}m ${secs.toString().padStart(2, '0')}s left • Ends at ${endStr}`;
    canSkip = false;
  } else if (isPaused) {
    const label = activeSchedule?.title || 'Schedule';
    title = `${label} Paused`;
    modeBadge = 'NORMAL';
    const endStr = pausedUntilMillis
      ? new Date(pausedUntilMillis).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
      : '';
    timeContent = `Ring restored until ${endStr}`;
    canSkip = false;
  } else if (activeSchedule) {
    title = activeSchedule.title;
    modeBadge = activeSchedule.targetMode;
    const remMin = activeRemainingMinutes || 0;
    const hrs = Math.floor(remMin / 60);
    const mins = remMin % 60;
    const remStr = hrs > 0 ? `${hrs}h ${mins}m left` : `${mins}m left`;
    const endHourStr = `${activeSchedule.endHour.toString().padStart(2, '0')}:${activeSchedule.endMinute.toString().padStart(2, '0')}`;
    timeContent = `${remStr} • Ends at ${endHourStr}`;
    canSkip = true;

    // Calculate progress
    const startMin = activeSchedule.startHour * 60 + activeSchedule.startMinute;
    const endMin = activeSchedule.endHour * 60 + activeSchedule.endMinute;
    const totalMin = startMin < endMin ? endMin - startMin : endMin + 1440 - startMin;
    const elapsed = Math.max(0, totalMin - remMin);
    progressPct = totalMin > 0 ? Math.min(100, Math.round((elapsed / totalMin) * 100)) : 0;
  }

  return (
    <div className="w-full max-w-md mx-auto mb-3 px-4">
      <div className="rounded-2xl bg-neutral-900/90 border border-white/20 backdrop-blur-xl shadow-2xl overflow-hidden transition-all">
        {/* Header bar */}
        <div className="flex items-center justify-between px-3.5 py-2 border-b border-white/10 bg-white/[0.04] text-[11px] text-neutral-400">
          <div className="flex items-center gap-1.5">
            <Smartphone className="w-3.5 h-3.5 text-neutral-300" />
            <span className="font-semibold text-neutral-200">Android Status Notification</span>
            {highPriority && (
              <span className="px-1.5 py-0.2 text-[9px] rounded-full bg-white/10 text-white font-mono">
                HIGH PRIORITY
              </span>
            )}
          </div>
          <button
            type="button"
            onClick={() => setExpanded(!expanded)}
            className="flex items-center gap-1 text-neutral-400 hover:text-white transition-colors"
          >
            <span>{expanded ? 'Collapse' : 'Expand'}</span>
            {expanded ? <ChevronUp className="w-3 h-3" /> : <ChevronDown className="w-3 h-3" />}
          </button>
        </div>

        {/* Notification Content */}
        <div className="p-3.5">
          <div className="flex items-start justify-between gap-3">
            <div className="flex items-start gap-2.5 min-w-0">
              <div className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center shrink-0 mt-0.5">
                {modeBadge === 'NORMAL' ? (
                  <Volume2 className="w-4 h-4 text-white" />
                ) : (
                  <VolumeX className="w-4 h-4 text-white" />
                )}
              </div>
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <h4 className="text-xs font-bold text-white truncate">{title}</h4>
                  <span className="px-1.5 py-0.5 rounded text-[10px] font-bold bg-white/15 text-neutral-200 tracking-wider">
                    {modeBadge}
                  </span>
                </div>
                <p className="text-xs text-neutral-300 mt-0.5 font-medium">{timeContent}</p>
              </div>
            </div>
          </div>

          {/* Progress bar */}
          {isActive && (
            <div className="w-full bg-white/10 rounded-full h-1.5 mt-3 overflow-hidden">
              <div
                className="bg-white h-full rounded-full transition-all duration-500"
                style={{ width: `${progressPct}%` }}
              />
            </div>
          )}

          {/* Action buttons */}
          <div className="flex items-center justify-end gap-2 mt-3 pt-2 border-t border-white/[0.08]">
            {canSkip && (
              <button
                type="button"
                onClick={onSkipPeriod}
                className="px-3 py-1 rounded-full text-xs font-medium text-neutral-300 hover:text-white bg-white/10 hover:bg-white/20 transition-all"
              >
                Skip to :00
              </button>
            )}
            <button
              type="button"
              onClick={onCancelActive}
              className="px-3 py-1 rounded-full text-xs font-semibold text-black bg-white hover:bg-neutral-200 active:scale-95 transition-all shadow-xs"
            >
              End Now
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
