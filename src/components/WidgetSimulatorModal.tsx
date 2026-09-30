import React, { useState } from 'react';
import { X, Smartphone, SkipForward, Ban, Sparkles, Volume2, VolumeX, Vibrate, Bell } from 'lucide-react';
import { ScheduleRule } from '../types';

interface WidgetSimulatorModalProps {
  isOpen: boolean;
  onClose: () => void;
  activeSchedule: ScheduleRule | null;
  quickMuteUntilMillis: number | null;
  pausedUntilMillis: number | null;
  onCancelActive: () => void;
  onSkipPeriod: () => void;
  onQuickMute?: (minutes: number) => void;
}

export const WidgetSimulatorModal: React.FC<WidgetSimulatorModalProps> = ({
  isOpen,
  onClose,
  activeSchedule,
  quickMuteUntilMillis,
  pausedUntilMillis,
  onCancelActive,
  onSkipPeriod,
  onQuickMute,
}) => {
  const [activeTab, setActiveTab] = useState<'widget' | 'island'>('widget');
  const [isIslandExpanded, setIsIslandExpanded] = useState(true);
  const [showQuickTimerPopup, setShowQuickTimerPopup] = useState(false);

  if (!isOpen) return null;

  let modeName = 'Normal';
  let isRunning = false;
  let timeDetail = 'Ready';

  if (quickMuteUntilMillis !== null) {
    const remainingMins = Math.max(1, Math.round((quickMuteUntilMillis - Date.now()) / 60000));
    modeName = 'Vibrate';
    isRunning = true;
    timeDetail = `${remainingMins}m remaining`;
  } else if (pausedUntilMillis !== null) {
    modeName = 'Normal';
    isRunning = true;
    timeDetail = 'Skipped until top of hour';
  } else if (activeSchedule !== null) {
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
        </div>

        <div className="mt-4">
          {activeTab === 'widget' ? (
            <div>
              <p className="text-xs text-neutral-400 mb-3">
                {quickMuteUntilMillis !== null
                  ? 'Timer is running (Red X button to cancel — no skip button):'
                  : isRunning
                  ? 'Active schedule running (Default view with indicator removed):'
                  : 'Idle state (Only a ring symbol — tap to choose quick timer):'}
              </p>

              {quickMuteUntilMillis !== null ? (
                /* Timer running state: Red X button centered (no skip button) */
                <div className="rounded-2xl bg-black/80 border border-red-500/30 p-4 backdrop-blur-2xl shadow-xl flex flex-col items-center justify-center h-28">
                  <button
                    type="button"
                    onClick={onCancelActive}
                    className="w-14 h-14 rounded-full bg-red-950/70 hover:bg-red-900 border border-red-500/60 flex items-center justify-center text-red-400 hover:text-white transition-all active:scale-95 shadow-lg group"
                    title="Cancel quick timer"
                  >
                    <X className="w-7 h-7 text-red-500 group-hover:scale-110 transition-transform stroke-[2.5]" />
                  </button>
                  <span className="text-[10px] text-red-400 mt-2 font-mono font-medium">
                    Tap Red X to cancel timer
                  </span>
                </div>
              ) : isRunning ? (
                /* Schedule active state: Default two big buttons edge-to-edge (indicator removed) */
                <div className="rounded-2xl bg-black/80 border border-white/25 p-3 backdrop-blur-2xl shadow-xl">
                  <div className="grid grid-cols-2 gap-2.5 h-16">
                    <button
                      type="button"
                      onClick={onSkipPeriod}
                      className="flex flex-col items-center justify-center gap-1.5 rounded-xl bg-white/10 hover:bg-white/20 text-white text-xs font-semibold border border-white/15 transition-all active:scale-95"
                    >
                      <SkipForward className="w-5 h-5 text-white" />
                      <span className="text-[10px] text-neutral-300 font-medium">Skip :00</span>
                    </button>
                    <button
                      type="button"
                      onClick={onCancelActive}
                      className="flex flex-col items-center justify-center gap-1.5 rounded-xl bg-white text-black text-xs font-bold hover:bg-neutral-200 transition-all active:scale-95 shadow-xs"
                    >
                      <Ban className="w-5 h-5 text-black" />
                      <span className="text-[10px] text-neutral-900 font-bold">End Now</span>
                    </button>
                  </div>
                </div>
              ) : (
                /* Idle state: Only a ring symbol centered */
                <div className="rounded-2xl bg-black/80 border border-white/25 p-4 backdrop-blur-2xl shadow-xl flex flex-col items-center justify-center h-28">
                  <button
                    type="button"
                    onClick={() => setShowQuickTimerPopup(true)}
                    className="w-14 h-14 rounded-full bg-white/10 hover:bg-white/20 border border-white/30 flex items-center justify-center text-white transition-all active:scale-95 shadow-lg group"
                    title="Tap to select quick timer"
                  >
                    <Bell className="w-6 h-6 text-white group-hover:scale-110 transition-transform" />
                  </button>
                  <span className="text-[10px] text-neutral-400 mt-2 font-mono">
                    Tap ring to set quick timer
                  </span>
                </div>
              )}

              {/* Popup dialog simulator when user taps the ring symbol */}
              {showQuickTimerPopup && (
                <div className="mt-3 p-3.5 rounded-2xl bg-neutral-800/95 border border-white/20 shadow-2xl animate-in zoom-in-95 duration-150">
                  <div className="flex items-center justify-between mb-2 pb-1.5 border-b border-white/10">
                    <div className="flex items-center gap-1.5">
                      <Bell className="w-3.5 h-3.5 text-white" />
                      <span className="text-xs font-bold text-white">Select Quick Timer</span>
                    </div>
                    <button
                      type="button"
                      onClick={() => setShowQuickTimerPopup(false)}
                      className="text-neutral-400 hover:text-white p-0.5"
                    >
                      <X className="w-3.5 h-3.5" />
                    </button>
                  </div>
                  <div className="grid grid-cols-3 gap-1.5 mt-2">
                    {[15, 30, 45, 60, 120, 240].map((min) => (
                      <button
                        key={min}
                        type="button"
                        onClick={() => {
                          onQuickMute?.(min);
                          setShowQuickTimerPopup(false);
                        }}
                        className="py-2 px-1 text-center rounded-xl bg-white/10 hover:bg-white text-white hover:text-black text-xs font-bold transition-all active:scale-95 border border-white/10"
                      >
                        {min >= 60 ? `${min / 60}h` : `${min}m`}
                      </button>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div>
              <p className="text-xs text-neutral-400 mb-2">
                Vivo OriginOS dynamic notch capsule / island:
              </p>

              {/* Vivo Origin Island Mock */}
              <div className="p-4 rounded-2xl bg-neutral-950 border border-white/10 text-center">
                <div
                  onClick={() => setIsIslandExpanded(!isIslandExpanded)}
                  className="inline-block cursor-pointer select-none transition-all duration-300"
                >
                  {!isIslandExpanded ? (
                    /* Collapsed Origin Island Capsule */
                    <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-black/90 border border-white/20 shadow-lg hover:border-white/40 transition-colors">
                      <div className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
                      <span className="text-xs font-bold text-white tracking-wide">
                        {activeSchedule?.title || (quickMuteUntilMillis ? 'Quick Mute' : 'Normal')}
                      </span>
                      <span className="text-[10px] text-neutral-400">
                        {modeName}
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
