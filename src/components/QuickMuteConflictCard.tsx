import React from 'react';
import { AlertCircle } from 'lucide-react';
import { QuickMuteConflict } from '../types';

interface QuickMuteConflictCardProps {
  conflict: QuickMuteConflict;
  onKeep: () => void;
  onOverride: () => void;
  className?: string;
}

export const QuickMuteConflictCard: React.FC<QuickMuteConflictCardProps> = ({
  conflict,
  onKeep,
  onOverride,
  className = '',
}) => {
  return (
    <div
      className={`rounded-[22px] bg-amber-500/10 border border-amber-500/30 p-4.5 backdrop-blur-xl transition-all ${className}`}
    >
      <div className="flex items-start gap-3">
        <AlertCircle className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
        <div className="flex-1 min-w-0">
          <h4 className="text-sm font-semibold text-neutral-100">{conflict.title}</h4>
          <p className="text-xs text-neutral-300 mt-1 leading-relaxed">{conflict.message}</p>

          <div className="flex items-center gap-2 mt-3.5">
            <button
              type="button"
              onClick={onKeep}
              className="px-3.5 py-1.5 rounded-full text-xs font-medium text-neutral-300 hover:text-white bg-white/10 hover:bg-white/15 transition-colors"
            >
              Keep Current
            </button>
            <button
              type="button"
              onClick={onOverride}
              className="px-4 py-1.5 rounded-full text-xs font-bold text-black bg-white hover:bg-neutral-200 transition-all active:scale-95 shadow-xs"
            >
              Override ({conflict.pendingMinutes}m)
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
