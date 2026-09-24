import React, { useState } from 'react';
import { ChevronDown, ChevronUp, Volume2, VolumeX, Smartphone } from 'lucide-react';
import { ScheduleRule } from '../types';

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
    <div className="w-full max-w-md mx-auto mb-16 px-4">
      <div className="rounded-[24px] bg-[#292a2d] border border-[#45464f] shadow-2xl overflow-hidden transition-all text-[#e3e2e6]">
        {/* Header bar */}
        <div className="flex items-center justify-between px-4 py-2.5 border-b border-[#45464f]/30 bg-[#1e1f23] text-xs text-[#c6c5d0]">
          <div className="flex items-center gap-2">
            <Smartphone className="w-3.5 h-3.5 text-[#bac3ff]" />
            <span className="font-semibold text-[#e3e2e6]">Android Notification Live Activity</span>
            {highPriority && (
              <span className="px-2 py-0.5 text-[9px] font-bold rounded-full bg-[#283b9f] text-[#dee0ff]">
                HIGH PRIORITY
              </span>
            )}
          </div>
          <button
            type="button"
            onClick={() => setExpanded(!expanded)}
            className="flex items-center gap-1 text-[#c6c5d0] hover:text-white transition-colors"
          >
            <span>{expanded ? 'Collapse' : 'Expand'}</span>
            {expanded ? <ChevronUp className="w-3 h-3" /> : <ChevronDown className="w-3 h-3" />}
          </button>
        </div>

        {/* Notification Content */}
        <div className="p-4">
          <div className="flex items-start justify-between gap-3">
            <div className="flex items-start gap-3 min-w-0">
              <div className="w-9 h-9 rounded-full bg-[#bac3ff]/20 text-[#bac3ff] flex items-center justify-center shrink-0 mt-0.5">
                {modeBadge === 'NORMAL' ? (
                  <Volume2 className="w-4 h-4 text-emerald-400" />
                ) : (
                  <VolumeX className="w-4 h-4 text-[#bac3ff]" />
                )}
              </div>
              <div className="min-w-0">
                <div className="flex items-center gap-2">
                  <h4 className="text-sm font-bold text-[#e3e2e6] truncate">{title}</h4>
                  <span className="px-2 py-0.5 rounded-full text-[10px] font-bold bg-[#434659] text-[#dfe1f9] tracking-wider">
                    {modeBadge}
                  </span>
                </div>
                <p className="text-xs text-[#c6c5d0] mt-0.5 font-medium">{timeContent}</p>
              </div>
            </div>
          </div>

          {/* Progress bar */}
          {isActive && (
            <div className="w-full bg-[#45464f]/40 rounded-full h-1.5 mt-3 overflow-hidden">
              <div
                className="bg-[#bac3ff] h-full rounded-full transition-all duration-500"
                style={{ width: `${progressPct}%` }}
              />
            </div>
          )}

          {/* Action buttons */}
          <div className="flex items-center justify-end gap-2 mt-3 pt-3 border-t border-[#45464f]/30">
            {canSkip && (
              <button
                type="button"
                onClick={onSkipPeriod}
                className="px-3.5 py-1.5 rounded-full text-xs font-semibold text-[#bac3ff] hover:bg-[#bac3ff]/15 transition-colors"
              >
                Skip until :00
              </button>
            )}
            <button
              type="button"
              onClick={onCancelActive}
              className="px-4 py-1.5 rounded-full text-xs font-bold bg-[#bac3ff] text-[#08218a] hover:bg-[#c9d0ff] transition-all active:scale-95 shadow-xs"
            >
              {isPaused ? 'Restore Schedule' : 'End Early'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
