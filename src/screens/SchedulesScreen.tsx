import React from 'react';
import { Plus, BellOff } from 'lucide-react';
import { ScheduleRule, QuickMuteConflict } from '../types';
import { ScheduleCard } from '../components/ScheduleCard';
import { QuickMuteSection } from '../components/QuickMuteSection';
import { QuickMuteConflictCard } from '../components/QuickMuteConflictCard';
import { MD3Card } from '../components/MD3Card';

interface SchedulesScreenProps {
  schedules: ScheduleRule[];
  conflictInfo: QuickMuteConflict | null;
  quickMuteUntilMillis: number | null;
  quickMuteRemainingSeconds: number;
  onQuickMute: (minutes: number) => void;
  onCancelQuickMute: () => void;
  onKeepConflict: () => void;
  onOverrideConflict: () => void;
  onToggleRule: (id: string, isEnabled: boolean) => void;
  onEditRule: (rule: ScheduleRule) => void;
  onDeleteRule: (id: string) => void;
  onAddNewRule: () => void;
}

export const SchedulesScreen: React.FC<SchedulesScreenProps> = ({
  schedules,
  conflictInfo,
  quickMuteUntilMillis,
  quickMuteRemainingSeconds,
  onQuickMute,
  onCancelQuickMute,
  onKeepConflict,
  onOverrideConflict,
  onToggleRule,
  onEditRule,
  onDeleteRule,
  onAddNewRule,
}) => {
  return (
    <div className="w-full max-w-md mx-auto space-y-4 px-4 pb-20 pt-2">
      {/* Conflict Dialog / Card */}
      {conflictInfo && (
        <QuickMuteConflictCard
          conflict={conflictInfo}
          onKeep={onKeepConflict}
          onOverride={onOverrideConflict}
        />
      )}

      {/* Quick Mute */}
      <QuickMuteSection
        activeRemainingSeconds={
          quickMuteUntilMillis !== null ? quickMuteRemainingSeconds : null
        }
        onQuickMute={onQuickMute}
        onCancelQuickMute={onCancelQuickMute}
      />

      {/* Schedule List Header */}
      <div className="flex items-center justify-between px-1 pt-1">
        <span className="text-xs font-semibold text-[#c6c5d0] uppercase tracking-wider">
          Schedules
        </span>
        <button
          type="button"
          onClick={onAddNewRule}
          className="inline-flex items-center gap-1 px-3 py-1 rounded-full bg-[#bac3ff] text-[#08218a] text-xs font-semibold hover:bg-[#c9d0ff] transition-all active:scale-95"
        >
          <Plus className="w-3.5 h-3.5 stroke-[2.5]" />
          Add
        </button>
      </div>

      {/* Schedule Cards or Empty State */}
      {schedules.length === 0 ? (
        <MD3Card
          variant="outlined"
          className="flex flex-col items-center justify-center py-12 px-6 text-center rounded-[28px] bg-[#1e1f23] border-[#45464f]"
        >
          <div className="w-12 h-12 rounded-full bg-[#292a2d] flex items-center justify-center mb-2">
            <BellOff className="w-6 h-6 text-[#8f909a]" />
          </div>
          <h3 className="text-base font-semibold text-[#e3e2e6] mt-2">No schedules</h3>
          <p className="text-xs text-[#c6c5d0] mt-1 max-w-xs">
            Add a schedule to set automated quiet hours.
          </p>
          <button
            type="button"
            onClick={onAddNewRule}
            className="mt-4 px-4 py-2 rounded-full bg-[#bac3ff] text-[#08218a] text-xs font-semibold hover:bg-[#c9d0ff] transition-all active:scale-95"
          >
            Create schedule
          </button>
        </MD3Card>
      ) : (
        <div className="space-y-3">
          {schedules.map((rule) => (
            <ScheduleCard
              key={rule.id}
              rule={rule}
              onToggle={(isEnabled) => onToggleRule(rule.id, isEnabled)}
              onEdit={() => onEditRule(rule)}
              onDelete={() => onDeleteRule(rule.id)}
            />
          ))}
        </div>
      )}
    </div>
  );
};
