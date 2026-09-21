import React from 'react';
import { X, Smartphone, SkipForward, Ban } from 'lucide-react';
import { ScheduleRule } from '../types';

interface WidgetSimulatorModalProps {
  isOpen: boolean;
  onClose: () => void;
  activeSchedule: ScheduleRule | null;
  quickMuteUntilMillis: number | null;
  pausedUntilMillis: number | null;
  onCancelActive: () => void;
  onSkipPeriod: () => void;
}

export const WidgetSimulatorModal: React.FC<WidgetSimulatorModalProps> = ({
  isOpen,
  onClose,
  activeSchedule,
  quickMuteUntilMillis,
  pausedUntilMillis,
  onCancelActive,
  onSkipPeriod,
}) => {
  if (!isOpen) return null;

  let statusText = '○ Nothing running';
  let isRunning = false;

  if (quickMuteUntilMillis !== null) {
    statusText = '● Timer running (Quick Mute)';
    isRunning = true;
  } else if (pausedUntilMillis !== null) {
    statusText = '● Schedule paused until :00';
    isRunning = true;
  } else if (activeSchedule !== null) {
    statusText = `● Schedule running: ${activeSchedule.title}`;
    isRunning = true;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in duration-150">
      <div className="w-full max-w-sm rounded-3xl bg-neutral-900 border border-white/20 p-6 shadow-2xl relative">
        <div className="flex items-center justify-between pb-3 border-b border-white/10">
          <div className="flex items-center gap-2">
            <Smartphone className="w-4 h-4 text-white" />
            <h3 className="text-sm font-bold text-white">Android 2×1 Home Widget</h3>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded-full text-neutral-400 hover:text-white transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        <div className="mt-5">
          <p className="text-xs text-neutral-400 mb-3">
            Interactive preview of the Android home screen widget (VibeWidgetProvider):
          </p>

          {/* 2x1 Widget representation */}
          <div className="rounded-2xl bg-black/80 border border-white/25 p-4 backdrop-blur-2xl shadow-xl">
            <div className="flex items-center justify-between mb-3">
              <span className="text-[11px] font-mono tracking-wider font-semibold text-neutral-200">
                {statusText}
              </span>
              <span className="w-2 h-2 rounded-full bg-white animate-pulse" />
            </div>

            <div className="grid grid-cols-2 gap-2 mt-2">
              <button
                type="button"
                onClick={onSkipPeriod}
                disabled={!isRunning}
                className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl bg-white/10 hover:bg-white/20 text-white text-xs font-semibold border border-white/15 transition-all disabled:opacity-30 disabled:pointer-events-none active:scale-95"
              >
                <SkipForward className="w-3.5 h-3.5" />
                Skip to :00
              </button>
              <button
                type="button"
                onClick={onCancelActive}
                disabled={!isRunning}
                className="flex items-center justify-center gap-1.5 py-2 px-3 rounded-xl bg-white text-black text-xs font-bold hover:bg-neutral-200 transition-all disabled:opacity-30 disabled:pointer-events-none active:scale-95 shadow-xs"
              >
                <Ban className="w-3.5 h-3.5" />
                End Now
              </button>
            </div>
          </div>
        </div>

        <div className="mt-4 pt-3 border-t border-white/10 text-center">
          <button
            type="button"
            onClick={onClose}
            className="text-xs text-neutral-400 hover:text-white font-medium transition-colors"
          >
            Close Widget Preview
          </button>
        </div>
      </div>
    </div>
  );
};
