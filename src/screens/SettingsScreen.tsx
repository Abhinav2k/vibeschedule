import React from 'react';
import { Bell, Smartphone, RotateCcw } from 'lucide-react';
import { MD3Card } from '../components/MD3Card';
import { MD3Switch } from '../components/MD3Switch';
import { AppSettings } from '../types';
import { triggerHapticFeedback } from '../utils/audioVibe';

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
    <div className="w-full max-w-md mx-auto space-y-4 px-4 pb-20 pt-2">
      <div>
        <span className="text-xs font-semibold text-[#bac3ff] tracking-wider uppercase px-1">
          Notifications
        </span>
      </div>

      <MD3Card variant="elevated" className="rounded-[24px] p-5 bg-[#1e1f23] border border-[#45464f]/30">
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-start gap-3 flex-1 min-w-0">
            <Bell className="w-5 h-5 text-[#bac3ff] shrink-0 mt-0.5" />
            <div className="min-w-0">
              <h3 className="text-sm font-semibold text-[#e3e2e6]">
                Lock screen notification
              </h3>
              <p className="text-xs text-[#c6c5d0] mt-0.5">
                Show status and quick skip controls on lock screen
              </p>
            </div>
          </div>

          <MD3Switch
            checked={settings.notifHighPriority}
            onChange={(val) => {
              onUpdateSettings({ notifHighPriority: val });
              triggerHapticFeedback([40]);
            }}
          />
        </div>
      </MD3Card>

      <div className="pt-2">
        <span className="text-xs font-semibold text-[#bac3ff] tracking-wider uppercase px-1">
          General
        </span>
      </div>

      <MD3Card variant="elevated" className="rounded-[24px] p-2 bg-[#1e1f23] border border-[#45464f]/30 divide-y divide-[#45464f]/30">
        <button
          type="button"
          onClick={onOpenWidgetPreview}
          className="w-full flex items-center justify-between p-3.5 rounded-2xl hover:bg-[#292a2d] transition-colors text-left"
        >
          <div className="flex items-center gap-3">
            <Smartphone className="w-4 h-4 text-[#bac3ff]" />
            <div>
              <p className="text-xs font-semibold text-[#e3e2e6]">Widget preview</p>
              <p className="text-[11px] text-[#c6c5d0]">Preview Android home screen widget</p>
            </div>
          </div>
          <span className="text-xs text-[#bac3ff]">Open</span>
        </button>

        <button
          type="button"
          onClick={onResetDefaults}
          className="w-full flex items-center justify-between p-3.5 rounded-2xl hover:bg-[#93000a]/20 transition-colors text-left text-[#ffb4ab]"
        >
          <div className="flex items-center gap-3">
            <RotateCcw className="w-4 h-4" />
            <div>
              <p className="text-xs font-semibold text-inherit">Reset schedules</p>
              <p className="text-[11px] text-[#8f909a]">Restore default quiet hours</p>
            </div>
          </div>
          <span className="text-xs font-semibold">Reset</span>
        </button>
      </MD3Card>

      <div className="pt-4 text-center">
        <p className="text-xs text-[#8f909a]">
          VibeSchedule • Version 1.4.18
        </p>
      </div>
    </div>
  );
};
