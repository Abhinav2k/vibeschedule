import React from 'react';
import { Bell, Volume2, Smartphone, RotateCcw } from 'lucide-react';
import { GlassCard } from '../components/GlassCard';
import { AppSettings } from '../types';
import { playAudioFeedback, triggerHapticFeedback } from '../utils/audioVibe';

interface SettingsScreenProps {
  settings: AppSettings;
  onUpdateSettings: (settings: Partial<AppSettings>) => void;
  onResetDefaults: () => void;
  onOpenWidgetPreview: () => void;
}

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  settings,
  onUpdateSettings,
  onResetDefaults,
  onOpenWidgetPreview,
}) => {
  return (
    <div className="w-full max-w-md mx-auto space-y-4 px-4 pb-24 pt-2">
      <div>
        <span className="text-[11px] font-bold text-neutral-400 tracking-wider uppercase px-1">
          Notifications
        </span>
      </div>

      <GlassCard className="rounded-[22px] p-5">
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-start gap-3 flex-1 min-w-0">
            <Bell className="w-5 h-5 text-neutral-300 shrink-0 mt-0.5" />
            <div className="min-w-0">
              <h3 className="text-sm font-semibold text-white">
                High Priority Notification
              </h3>
              <p className="text-xs text-neutral-400 mt-0.5 leading-relaxed">
                Always show on lock screen, even when quiet notifications are hidden
              </p>
            </div>
          </div>

          <button
            type="button"
            role="switch"
            aria-checked={settings.notifHighPriority}
            onClick={() => {
              onUpdateSettings({ notifHighPriority: !settings.notifHighPriority });
              triggerHapticFeedback([40]);
            }}
            className={`
              relative inline-flex h-7 w-12 shrink-0 cursor-pointer rounded-full p-0.5 transition-colors duration-200 ease-in-out
              ${settings.notifHighPriority ? 'bg-white' : 'bg-white/20'}
            `}
          >
            <span
              className={`
                pointer-events-none inline-block h-6 w-6 transform rounded-full shadow-md transition duration-200 ease-in-out
                ${settings.notifHighPriority ? 'translate-x-5 bg-black' : 'translate-x-0 bg-neutral-300'}
              `}
            />
          </button>
        </div>
      </GlassCard>

      <p className="text-[11px] text-neutral-400 px-1 leading-relaxed">
        When enabled, the active schedule notification is shown as high-priority and will appear on your lock screen regardless of system notification settings. Disable for a quieter experience.
      </p>

      {/* Auxiliary settings / interactive features */}
      <div className="pt-2">
        <span className="text-[11px] font-bold text-neutral-400 tracking-wider uppercase px-1">
          Device & Integration Previews
        </span>
      </div>

      <GlassCard className="rounded-[22px] p-4 space-y-3">
        <button
          type="button"
          onClick={onOpenWidgetPreview}
          className="w-full flex items-center justify-between py-2 text-left hover:opacity-80 transition-opacity"
        >
          <div className="flex items-center gap-3">
            <Smartphone className="w-4 h-4 text-neutral-300" />
            <div>
              <p className="text-xs font-semibold text-white">Preview Android Home Widget</p>
              <p className="text-[11px] text-neutral-400">View and test the 2×1 widget simulation</p>
            </div>
          </div>
          <span className="text-xs text-neutral-400">Open →</span>
        </button>

        <div className="border-t border-white/[0.08]" />

        <button
          type="button"
          onClick={() => {
            playAudioFeedback('vibe');
            triggerHapticFeedback([100, 50, 100]);
          }}
          className="w-full flex items-center justify-between py-2 text-left hover:opacity-80 transition-opacity"
        >
          <div className="flex items-center gap-3">
            <Volume2 className="w-4 h-4 text-neutral-300" />
            <div>
              <p className="text-xs font-semibold text-white">Test Vibration / Sound Pulse</p>
              <p className="text-[11px] text-neutral-400">Trigger simulated haptic audio</p>
            </div>
          </div>
          <span className="text-xs text-neutral-400">Play</span>
        </button>

        <div className="border-t border-white/[0.08]" />

        <button
          type="button"
          onClick={onResetDefaults}
          className="w-full flex items-center justify-between py-2 text-left text-neutral-400 hover:text-rose-400 transition-colors"
        >
          <div className="flex items-center gap-3">
            <RotateCcw className="w-4 h-4" />
            <div>
              <p className="text-xs font-semibold text-inherit">Reset to Default Rules</p>
              <p className="text-[11px] text-neutral-500">Restore factory sample schedules</p>
            </div>
          </div>
          <span className="text-xs">Reset</span>
        </button>
      </GlassCard>

      <div className="pt-4 text-center">
        <p className="text-[11px] text-neutral-400">
          VibeSchedule v1.4.15 • Ported to React & Tailwind CSS
        </p>
      </div>
    </div>
  );
};
