import React from 'react';
import { Timer, X } from 'lucide-react';
import { MD3Card } from './MD3Card';

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
    <MD3Card variant="elevated" className={`p-4 rounded-[24px] bg-[#1e1f23] ${className}`}>
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 text-[#bac3ff]">
          <Timer className="w-4 h-4" />
          <span className="text-xs font-semibold text-[#e3e2e6]">
            Quick Mute
          </span>
        </div>

        {isTimerActive && (
          <div className="flex items-center gap-2">
            <span className="text-xs font-mono font-medium text-[#bac3ff] bg-[#283b9f]/30 px-2.5 py-0.5 rounded-full border border-[#bac3ff]/30">
              {remMinutes}m {remSeconds.toString().padStart(2, '0')}s
            </span>
            <button
              type="button"
              onClick={onCancelQuickMute}
              className="flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium text-[#ffb4ab] hover:bg-[#93000a]/20 transition-all"
            >
              <X className="w-3.5 h-3.5" />
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
            className="py-2 px-3 rounded-full bg-[#292a2d] hover:bg-[#333438] active:scale-95 text-[#e3e2e6] text-xs font-medium transition-all text-center border border-[#45464f]/40 hover:border-[#bac3ff]/50"
          >
            {label}
          </button>
        ))}
      </div>
    </MD3Card>
  );
};
