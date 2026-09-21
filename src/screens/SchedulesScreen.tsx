import React from 'react';
import { Bell, ShieldCheck, Plus, BellOff } from 'lucide-react';
import { ScheduleRule, QuickMuteConflict } from '../types';
import { ScheduleCard } from '../components/ScheduleCard';
import { QuickMuteSection } from '../components/QuickMuteSection';
import { QuickMuteConflictCard } from '../components/QuickMuteConflictCard';

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
    <div className="w-full max-w-md mx-auto space-y-3.5 px-4 pb-24 pt-2">
      {/* Informative Permission Status Banner */}
      <div className="flex items-center justify-between px-4 py-2.5 rounded-2xl bg-white/[0.04] border border-white/[0.08] text-xs text-neutral-300 backdrop-blur-md">
        <div className="flex items-center gap-2">
          <ShieldCheck className="w-4 h-4 text-emerald-400 shrink-0" />
          <span>Automation Active • Exact Alarms Ready</span>
        </div>
        <span className="text-[10px] font-mono uppercase bg-emerald-500/20 text-emerald-300 px-2 py-0.5 rounded-full">
          Online
        </span>
      </div>

      {/* Conflict Dialog / Card */}
      {conflictInfo && (
        <QuickMuteConflictCard
          conflict={conflictInfo}
          onKeep={onKeepConflict}
          onOverride={onOverrideConflict}
        />
      )}

      {/* Quick Mute Glass Bar */}
      <QuickMuteSection
        activeRemainingSeconds={
          quickMuteUntilMillis !== null ? quickMuteRemainingSeconds : null
        }
        onQuickMute={onQuickMute}
        onCancelQuickMute={onCancelQuickMute}
      />

      {/* Schedule List Header */}
      <div className="flex items-center justify-between px-1 pt-2">
        <span className="text-xs font-bold text-neutral-400 tracking-wider uppercase">
          Your Schedules ({schedules.length})
        </span>
        <button
          type="button"
          onClick={onAddNewRule}
          className="inline-flex items-center gap-1 text-xs font-semibold text-white hover:text-neutral-300 transition-colors"
        >
          <Plus className="w-3.5 h-3.5" />
          Add New
        </button>
      </div>

      {/* Schedule Cards or Empty State */}
      {schedules.length === 0 ? (
        <div className="flex flex-col items-center justify-center py-16 px-4 text-center rounded-[24px] bg-white/[0.03] border border-white/[0.06]">
          <BellOff className="w-12 h-12 text-neutral-600 stroke-[1.5]" />
          <h3 className="text-base font-bold text-neutral-200 mt-3">No Schedules</h3>
          <p className="text-xs text-neutral-400 mt-1 max-w-xs">
            Tap the + button to create a quiet hours schedule for work, study, or sleep.
          </p>
          <button
            type="button"
            onClick={onAddNewRule}
            className="mt-4 px-4 py-2 rounded-full bg-white text-black text-xs font-bold hover:bg-neutral-200 transition-all active:scale-95"
          >
            Create First Schedule
          </button>
        </div>
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
