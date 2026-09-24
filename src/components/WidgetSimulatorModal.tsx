import React, { useState } from 'react';
import { X, Smartphone, SkipForward, Ban, Sparkles, Volume2, VolumeX, Vibrate } from 'lucide-react';
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
  const [activeTab, setActiveTab] = useState<'widget' | 'island'>('island');
  const [isIslandExpanded, setIsIslandExpanded] = useState(true);

  if (!isOpen) return null;

  let statusText = '○ Nothing running';
  let modeName = 'Normal';
  let isRunning = false;
  let timeDetail = 'Ready';

  if (quickMuteUntilMillis !== null) {
    const remainingMins = Math.max(1, Math.round((quickMuteUntilMillis - Date.now()) / 60000));
    statusText = '● Timer running (Quick Mute)';
    modeName = 'Vibrate';
    isRunning = true;
    timeDetail = `${remainingMins}m remaining`;
  } else if (pausedUntilMillis !== null) {
    statusText = '● Schedule paused until :00';
    modeName = 'Normal';
    isRunning = true;
    timeDetail = 'Skipped until top of hour';
  } else if (activeSchedule !== null) {
    statusText = `● Schedule running: ${activeSchedule.title}`;
    modeName = activeSchedule.targetMode.toUpperCase();
    isRunning = true;
    timeDetail = `Active until ${activeSchedule.endHour.toString().padStart(2, '0')}:${activeSchedule.endMinute.toString().padStart(2, '0')}`;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md animate-in fade-in duration-150">
      <div className="w-full max-w-sm rounded-3xl bg-neutral-900 border border-white/20 p-6 shadow-2xl relative">
        <div className="flex items-center justify-between pb-3 border-b border-white/10">
          <div className="flex items-center gap-2">
            <Smartphone className="w-4 h-4 text-white" />
            <h3 className="text-sm font-bold text-white">System Integration Preview</h3>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="p-1 rounded-full text-neutral-400 hover:text-white transition-colors"
          >
            <X className="w-4 h-4" />
          </button>
        </div>

        {/* Tab switcher */}
        <div className="flex rounded-xl bg-black/40 p-1 mt-4 border border-white/10">
          <button
            type="button"
            onClick={() => setActiveTab('island')}
            className={`flex-1 py-1.5 text-xs font-semibold rounded-lg flex items-center justify-center gap-1.5 transition-all ${
              activeTab === 'island'
                ? 'bg-white text-black shadow-sm'
                : 'text-neutral-400 hover:text-white'
            }`}
          >
            <Sparkles className="w-3.5 h-3.5" />
            Origin Island
          </button>
          <button
            type="button"
            onClick={() => setActiveTab('widget')}
            className={`flex-1 py-1.5 text-xs font-semibold rounded-lg flex items-center justify-center gap-1.5 transition-all ${
              activeTab === 'widget'
                ? 'bg-white text-black shadow-sm'
                : 'text-neutral-400 hover:text-white'
            }`}
          >
            <Smartphone className="w-3.5 h-3.5" />
            2×1 Widget
          </button>
        </div>

        <div className="mt-4">
          {activeTab === 'island' ? (
            <div>
              <p className="text-xs text-neutral-400 mb-2">
                OriginOS (vivo / iQOO) Origin Island (原子岛) & status bar live capsule:
              </p>

              {/* Simulated Phone Top Punch-Hole Screen */}
              <div className="rounded-2xl bg-neutral-950 border border-white/20 p-4 pt-3 backdrop-blur-2xl shadow-xl flex flex-col items-center">
                {/* Punch-hole Camera */}
                <div className="w-3 h-3 rounded-full bg-neutral-900 border border-neutral-700 mb-2 shadow-inner" />

                {/* Compact Island Pill (Tap to toggle expanded) */}
                <div
                  onClick={() => setIsIslandExpanded(!isIslandExpanded)}
                  className="cursor-pointer transition-all duration-300 w-full"
                >
                  {!isIslandExpanded ? (
                    <div className="mx-auto max-w-[210px] h-8 rounded-full bg-black border border-white/25 px-3 flex items-center justify-between text-xs shadow-lg hover:border-white/50">
                      <div className="flex items-center gap-1.5 text-neutral-300">
                        {modeName === 'Vibrate' ? (
                          <Vibrate className="w-3.5 h-3.5 text-amber-400" />
                        ) : modeName === 'Silent' ? (
                          <VolumeX className="w-3.5 h-3.5 text-red-400" />
                        ) : (
                          <Volume2 className="w-3.5 h-3.5 text-emerald-400" />
                        )}
                        <span className="font-semibold">{modeName}</span>
                      </div>
                      <span className="text-[11px] font-mono text-neutral-400">
                        {isRunning ? timeDetail.split(' ')[0] : 'Idle'}
                      </span>
                    </div>
                  ) : (
                    /* Expanded Origin Island Card */
                    <div className="rounded-2xl bg-black/90 border border-white/30 p-3.5 shadow-2xl animate-in zoom-in-95 duration-200">
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-2">
                          <div className="p-1 rounded-lg bg-white/10 text-white">
                            {modeName === 'Vibrate' ? (
                              <Vibrate className="w-4 h-4 text-amber-400" />
                            ) : modeName === 'Silent' ? (
                              <VolumeX className="w-4 h-4 text-red-400" />
                            ) : (
                              <Volume2 className="w-4 h-4 text-emerald-400" />
                            )}
                          </div>
                          <div>
                            <div className="text-xs font-bold text-white leading-tight">
                              {activeSchedule?.title || (quickMuteUntilMillis ? 'Quick Mute' : 'VibeSchedule')}
                            </div>
                            <div className="text-[10px] text-neutral-400">
                              {timeDetail}
                            </div>
                          </div>
                        </div>
                        <span className="text-[10px] px-2 py-0.5 rounded-full bg-white/15 text-white font-mono font-semibold uppercase tracking-wider">
                          {modeName}
                        </span>
                      </div>

                      {/* Island interactive actions */}
                      <div className="grid grid-cols-2 gap-2 mt-3 pt-2 border-t border-white/10">
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            onSkipPeriod();
                          }}
                          disabled={!isRunning}
                          className="flex items-center justify-center gap-1 py-1.5 px-2 rounded-xl bg-white/10 hover:bg-white/20 text-white text-[11px] font-semibold border border-white/15 transition-all disabled:opacity-30 disabled:pointer-events-none active:scale-95"
                        >
                          <SkipForward className="w-3 h-3" />
                          Skip :00
                        </button>
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation();
                            onCancelActive();
                          }}
                          disabled={!isRunning}
                          className="flex items-center justify-center gap-1 py-1.5 px-2 rounded-xl bg-white text-black text-[11px] font-bold hover:bg-neutral-200 transition-all disabled:opacity-30 disabled:pointer-events-none active:scale-95"
                        >
                          <Ban className="w-3 h-3" />
                          End Now
                        </button>
                      </div>
                    </div>
                  )}
                </div>

                <p className="text-[11px] text-neutral-500 mt-2.5">
                  Tap capsule to {isIslandExpanded ? 'collapse' : 'expand'}
                </p>
              </div>
            </div>
          ) : (
            <div>
              <p className="text-xs text-neutral-400 mb-3">
                Android home screen widget (VibeWidgetProvider 2×1):
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
          )}
        </div>

        <div className="mt-4 pt-3 border-t border-white/10 text-center">
          <button
            type="button"
            onClick={onClose}
            className="text-xs text-neutral-400 hover:text-white font-medium transition-colors"
          >
            Close Preview
          </button>
        </div>
      </div>
    </div>
  );
};
