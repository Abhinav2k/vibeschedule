import React, { useState, useId } from 'react';
import { Timer, X, Clock, Plus, Minus, Check } from 'lucide-react';
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
  const customInputId = useId();
  const presets = [
    { minutes: 15, label: '15m' },
    { minutes: 30, label: '30m' },
    { minutes: 60, label: '1h' },
    { minutes: 120, label: '2h' },
  ];

  const [isCustomOpen, setIsCustomOpen] = useState(false);
  const [customMinutes, setCustomMinutes] = useState<number>(45);

  const isTimerActive = activeRemainingSeconds !== null && activeRemainingSeconds > 0;
  const remMinutes = isTimerActive ? Math.floor(activeRemainingSeconds / 60) : 0;
  const remSeconds = isTimerActive ? activeRemainingSeconds % 60 : 0;

  const quickCustomChips = [5, 10, 20, 45, 90, 180, 240];

  const adjustMinutes = (delta: number) => {
    setCustomMinutes((prev) => Math.min(1440, Math.max(1, prev + delta)));
  };

  const handleStartCustom = () => {
    const mins = Math.min(1440, Math.max(1, customMinutes));
    onQuickMute(mins);
    setIsCustomOpen(false);
  };

  const formatCustomDuration = (mins: number) => {
    if (mins < 60) return `${mins}m`;
    const h = Math.floor(mins / 60);
    const m = mins % 60;
    return m > 0 ? `${h}h ${m}m` : `${h}h`;
  };

  const getEstimatedEndTimeStr = (mins: number) => {
    const end = new Date(Date.now() + mins * 60 * 1000);
    return end.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <MD3Card variant="elevated" className={`p-3.5 sm:p-4 rounded-[24px] bg-[#1e1f23] ${className}`}>
      {/* Top Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2 text-[#bac3ff]">
          <Timer className="w-4 h-4" />
          <span className="text-xs font-semibold text-[#e3e2e6]">
            Quick Mute
          </span>
        </div>

        {isTimerActive && (
          <div className="flex items-center gap-1.5">
            <span className="text-[11px] font-mono font-medium text-[#bac3ff] bg-[#283b9f]/30 px-2.5 py-0.5 rounded-full border border-[#bac3ff]/30">
              {remMinutes}m {remSeconds.toString().padStart(2, '0')}s
            </span>
            <button
              type="button"
              onClick={onCancelQuickMute}
              className="flex items-center gap-1 h-6 px-2 rounded-full text-[11px] font-medium text-[#ffb4ab] bg-[#93000a]/20 hover:bg-[#93000a]/30 border border-[#ffb4ab]/30 transition-all cursor-pointer active:scale-95"
            >
              <X className="w-3 h-3" />
              Cancel
            </button>
          </div>
        )}
      </div>

      {/* Preset & Custom Timer Buttons (Shorter height) */}
      <div className="grid grid-cols-5 gap-1.5 mt-2.5">
        {presets.map(({ minutes, label }) => (
          <button
            key={minutes}
            id={`quick-mute-btn-${minutes}m`}
            type="button"
            onClick={() => {
              setIsCustomOpen(false);
              onQuickMute(minutes);
            }}
            className="h-7 sm:h-7.5 px-1.5 rounded-full bg-[#292a2d] hover:bg-[#333438] active:scale-95 text-[#e3e2e6] text-[11px] font-semibold transition-all text-center border border-[#45464f]/40 hover:border-[#bac3ff]/50 flex items-center justify-center cursor-pointer shadow-xs"
          >
            {label}
          </button>
        ))}

        {/* Custom Timer Button */}
        <button
          id="quick-mute-btn-custom"
          type="button"
          onClick={() => setIsCustomOpen(!isCustomOpen)}
          className={`h-7 sm:h-7.5 px-1.5 rounded-full transition-all text-center flex items-center justify-center gap-1 text-[11px] font-semibold cursor-pointer active:scale-95 border ${
            isCustomOpen
              ? 'bg-[#bac3ff] text-[#08218a] border-[#bac3ff] shadow-xs'
              : 'bg-[#283b9f]/25 hover:bg-[#283b9f]/40 text-[#bac3ff] border-[#bac3ff]/40 hover:border-[#bac3ff]/70'
          }`}
          title="Set custom timer duration"
        >
          <Clock className="w-3 h-3 stroke-[2.2]" />
          <span>Custom</span>
        </button>
      </div>

      {/* Expandable Custom Timer Panel */}
      {isCustomOpen && (
        <div className="mt-3 p-3 rounded-2xl bg-[#141518] border border-[#bac3ff]/30 space-y-2.5 animate-in fade-in-50 duration-200">
          <div className="flex items-center justify-between">
            <span className="text-xs font-semibold text-[#bac3ff] flex items-center gap-1.5">
              <Clock className="w-3.5 h-3.5" />
              Custom Duration
            </span>
            <span className="text-[11px] text-[#c6c5d0]">
              Ends ~{getEstimatedEndTimeStr(customMinutes)}
            </span>
          </div>

          {/* Stepper Display */}
          <div className="flex items-center justify-between bg-[#1e1f23] rounded-xl px-2.5 py-1.5 border border-[#45464f]/40">
            <div className="flex items-center gap-1">
              <button
                type="button"
                onClick={() => adjustMinutes(-15)}
                className="h-6 px-1.5 rounded-lg bg-[#292a2d] hover:bg-[#333438] text-[11px] text-[#c6c5d0] font-medium border border-[#45464f]/30 active:scale-95"
              >
                -15m
              </button>
              <button
                type="button"
                onClick={() => adjustMinutes(-5)}
                className="h-6 w-6 rounded-lg bg-[#292a2d] hover:bg-[#333438] text-[#c6c5d0] flex items-center justify-center border border-[#45464f]/30 active:scale-95"
              >
                <Minus className="w-3 h-3" />
              </button>
            </div>

            <div className="flex items-center gap-1.5">
              <input
                id={customInputId}
                type="number"
                min="1"
                max="1440"
                value={customMinutes}
                onChange={(e) => {
                  const val = parseInt(e.target.value, 10);
                  if (!isNaN(val)) setCustomMinutes(Math.min(1440, Math.max(1, val)));
                }}
                className="w-14 text-center font-mono font-bold text-sm text-white bg-[#141518] rounded-md py-0.5 border border-[#45464f] focus:outline-none focus:border-[#bac3ff]"
              />
              <span className="text-xs font-medium text-[#c6c5d0]">min</span>
              <span className="text-[11px] text-[#bac3ff] font-semibold ml-1">
                ({formatCustomDuration(customMinutes)})
              </span>
            </div>

            <div className="flex items-center gap-1">
              <button
                type="button"
                onClick={() => adjustMinutes(5)}
                className="h-6 w-6 rounded-lg bg-[#292a2d] hover:bg-[#333438] text-[#c6c5d0] flex items-center justify-center border border-[#45464f]/30 active:scale-95"
              >
                <Plus className="w-3 h-3" />
              </button>
              <button
                type="button"
                onClick={() => adjustMinutes(15)}
                className="h-6 px-1.5 rounded-lg bg-[#292a2d] hover:bg-[#333438] text-[11px] text-[#c6c5d0] font-medium border border-[#45464f]/30 active:scale-95"
              >
                +15m
              </button>
            </div>
          </div>

          {/* Quick Preset Chips */}
          <div className="flex items-center gap-1.5 flex-wrap">
            <span className="text-[10px] text-[#8f909a] font-medium uppercase tracking-wider mr-0.5">
              Presets:
            </span>
            {quickCustomChips.map((mins) => (
              <button
                key={mins}
                type="button"
                onClick={() => setCustomMinutes(mins)}
                className={`h-5.5 px-2 rounded-full text-[10px] font-semibold transition-all border ${
                  customMinutes === mins
                    ? 'bg-[#bac3ff]/30 text-[#bac3ff] border-[#bac3ff]'
                    : 'bg-[#1e1f23] text-[#c6c5d0] border-[#45464f]/40 hover:bg-[#292a2d]'
                }`}
              >
                {formatCustomDuration(mins)}
              </button>
            ))}
          </div>

          {/* Custom Action Buttons (Short & Sleek) */}
          <div className="flex items-center justify-end gap-2 pt-1">
            <button
              type="button"
              onClick={() => setIsCustomOpen(false)}
              className="h-7 px-3 rounded-full text-xs font-medium text-[#c6c5d0] hover:text-white hover:bg-white/10 transition-colors"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleStartCustom}
              className="h-7 px-3.5 rounded-full bg-[#bac3ff] text-[#08218a] hover:bg-[#c9d0ff] text-xs font-bold transition-all flex items-center gap-1 shadow-sm active:scale-95 cursor-pointer"
            >
              <Check className="w-3 h-3 stroke-[3]" />
              Start ({formatCustomDuration(customMinutes)})
            </button>
          </div>
        </div>
      )}
    </MD3Card>
  );
};
