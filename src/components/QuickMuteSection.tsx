import React from 'react';
import { Timer, X } from 'lucide-react';
import { GlassCard } from './GlassCard';

interface QuickMuteSectionProps {
  activeRemainingSeconds: number | null;
  onQuickMute: (minutes: number) => void;
  onCancelQuickMute: () => void;
  className?: string;
}

export const QuickMuteSection: React.FC<QuickMuteSectionProps> = ({
  activeRemainingSeconds,
  onQuickMute,
  onCancelQuickMute,
  className = '',
}) => {
  const options = [
    { minutes: 15, label: '15m' },
    { minutes: 30, label: '30m' },
    { minutes: 60, label: '1h' },
    { minutes: 120, label: '2h' },
  ];

  const isTimerActive = activeRemainingSeconds !== null && activeRemainingSeconds > 0;
  const remMinutes = isTimerActive ? Math.floor(activeRemainingSeconds / 60) : 0;
  const remSeconds = isTimerActive ? activeRemainingSeconds % 60 : 0;

  return (
    <GlassCard className={`p-4 rounded-[20px] ${className}`}>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-1.5 text-neutral-400">
          <Timer className="w-4 h-4 text-neutral-400" />
          <span className="text-xs font-semibold tracking-wider uppercase text-neutral-300">
            Quick Mute
          </span>
        </div>

        {isTimerActive && (
          <div className="flex items-center gap-2">
            <span className="text-xs font-semibold text-white font-mono bg-white/10 px-2 py-0.5 rounded-md">
              {remMinutes}m {remSeconds.toString().padStart(2, '0')}s left
            </span>
            <button
              type="button"
              onClick={onCancelQuickMute}
              className="flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium text-neutral-300 hover:text-white bg-white/10 hover:bg-white/20 transition-all"
            >
              <X className="w-3 h-3" />
              Cancel
            </button>
          </div>
        )}
      </div>

      <div className="grid grid-cols-4 gap-2 mt-3">
        {options.map(({ minutes, label }) => (
          <button
            key={minutes}
            id={`quick-mute-btn-${minutes}m`}
            type="button"
            onClick={() => onQuickMute(minutes)}
            className="py-2.5 rounded-full bg-white/[0.08] hover:bg-white/[0.16] active:scale-95 border border-white/[0.12] text-white text-xs sm:text-sm font-semibold transition-all duration-150 text-center shadow-xs"
          >
            {label}
          </button>
        ))}
      </div>
    </GlassCard>
  );
};
