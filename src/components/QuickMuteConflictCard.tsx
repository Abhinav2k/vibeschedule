import React from 'react';
import { AlertCircle } from 'lucide-react';
import { QuickMuteConflict } from '../types';
import { MD3Card } from './MD3Card';

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
    <MD3Card
      variant="elevated"
      className={`rounded-[24px] bg-[#5d3c56]/40 border border-[#e5bad8]/30 p-5 text-[#ffd7f3] ${className}`}
    >
      <div className="flex items-start gap-3.5">
        <AlertCircle className="w-5 h-5 text-[#e5bad8] shrink-0 mt-0.5" />
        <div className="flex-1 min-w-0">
          <h4 className="text-sm font-bold text-[#ffd7f3]">{conflict.title}</h4>
          <p className="text-xs text-[#ffd7f3]/85 mt-1 leading-relaxed">{conflict.message}</p>

          <div className="flex items-center gap-2.5 mt-4">
            <button
              type="button"
              onClick={onKeep}
              className="px-4 py-1.5 rounded-full text-xs font-semibold text-[#ffd7f3] hover:text-white bg-[#5d3c56] hover:bg-[#6f4867] transition-colors"
            >
              Keep Current
            </button>
            <button
              type="button"
              onClick={onOverride}
              className="px-4 py-1.5 rounded-full text-xs font-bold text-[#45263f] bg-[#e5bad8] hover:bg-[#f0c8e4] transition-all active:scale-95 shadow-xs"
            >
              Override ({conflict.pendingMinutes}m)
            </button>
          </div>
        </div>
      </div>
    </MD3Card>
  );
};
