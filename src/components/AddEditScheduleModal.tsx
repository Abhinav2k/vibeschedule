import React, { useState } from 'react';
import { Clock, X } from 'lucide-react';
import { ScheduleRule, SoundMode } from '../types';
import { formatTime24 } from '../hooks/useVibeSchedule';

interface AddEditScheduleModalProps {
  initialRule: ScheduleRule | null;
  onDismiss: () => void;
  onSave: (rule: ScheduleRule) => void;
}

export const AddEditScheduleModal: React.FC<AddEditScheduleModalProps> = ({
  initialRule,
  onDismiss,
  onSave,
}) => {
  const [title, setTitle] = useState(initialRule?.title || '');
  const [startHour, setStartHour] = useState(initialRule?.startHour ?? 9);
  const [startMinute, setStartMinute] = useState(initialRule?.startMinute ?? 0);
  const [endHour, setEndHour] = useState(initialRule?.endHour ?? 17);
  const [endMinute, setEndMinute] = useState(initialRule?.endMinute ?? 0);
  const [selectedDays, setSelectedDays] = useState<number[]>(
    initialRule?.daysOfWeek || [2, 3, 4, 5, 6]
  );
  const [targetMode, setTargetMode] = useState<SoundMode>(
    initialRule?.targetMode || SoundMode.VIBRATE
  );

  const allDays = [1, 2, 3, 4, 5, 6, 7];
  const weekdays = [2, 3, 4, 5, 6];
  const weekends = [1, 7];

  const daysList = [
    { dayInt: 2, label: 'M' },
    { dayInt: 3, label: 'T' },
    { dayInt: 4, label: 'W' },
    { dayInt: 5, label: 'T' },
    { dayInt: 6, label: 'F' },
    { dayInt: 7, label: 'S' },
    { dayInt: 1, label: 'S' },
  ];

  const handleStartTimeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const [h, m] = e.target.value.split(':').map(Number);
    if (!isNaN(h) && !isNaN(m)) {
      setStartHour(h);
      setStartMinute(m);
    }
  };

  const handleEndTimeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const [h, m] = e.target.value.split(':').map(Number);
    if (!isNaN(h) && !isNaN(m)) {
      setEndHour(h);
      setEndMinute(m);
    }
  };

  const handleToggleDay = (dayInt: number) => {
    if (selectedDays.includes(dayInt)) {
      setSelectedDays(selectedDays.filter((d) => d !== dayInt));
    } else {
      setSelectedDays([...selectedDays, dayInt]);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (selectedDays.length === 0) return;

    const resolvedTitle = title.trim() || 'Scheduled Mode';
    const rule: ScheduleRule = {
      id: initialRule?.id || `rule-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`,
      title: resolvedTitle,
      startHour,
      startMinute,
      endHour,
      endMinute,
      daysOfWeek: selectedDays,
      targetMode,
      revertMode: initialRule?.revertMode || SoundMode.NORMAL,
      isEnabled: initialRule?.isEnabled ?? true,
    };
    onSave(rule);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-md animate-in fade-in duration-200">
      <div
        className="w-full max-w-md rounded-[26px] bg-[#12141a] border border-white/20 p-6 shadow-2xl relative max-h-[92vh] overflow-y-auto"
        role="dialog"
        aria-modal="true"
      >
        <div className="flex items-center justify-between pb-3 border-b border-white/10">
          <h2 className="text-xl font-bold text-white tracking-tight">
            {initialRule ? 'Edit Schedule' : 'New Schedule'}
          </h2>
          <button
            type="button"
            onClick={onDismiss}
            className="p-1 rounded-full text-neutral-400 hover:text-white hover:bg-white/10 transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-4 space-y-5">
          {/* Title */}
          <div>
            <label className="block text-xs font-semibold text-neutral-300 uppercase tracking-wider mb-1.5">
              Label
            </label>
            <input
              id="schedule-title-input"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="Label (e.g. Work, Lecture)"
              className="w-full px-4 py-2.5 rounded-xl bg-white/[0.08] border border-white/20 text-white placeholder-neutral-500 focus:outline-none focus:border-white transition-colors"
            />
          </div>

          {/* Time Picker boxes */}
          <div className="grid grid-cols-2 gap-3">
            <div className="p-3.5 rounded-xl bg-white/[0.06] border border-white/10 flex flex-col items-center">
              <span className="text-xs text-neutral-400 font-medium">Starts</span>
              <div className="flex items-center gap-2 mt-1">
                <Clock className="w-4 h-4 text-white" />
                <input
                  id="schedule-start-time"
                  type="time"
                  value={formatTime24(startHour, startMinute)}
                  onChange={handleStartTimeChange}
                  className="bg-transparent text-lg font-bold text-white focus:outline-none cursor-pointer"
                />
              </div>
            </div>

            <div className="p-3.5 rounded-xl bg-white/[0.06] border border-white/10 flex flex-col items-center">
              <span className="text-xs text-neutral-400 font-medium">Ends</span>
              <div className="flex items-center gap-2 mt-1">
                <Clock className="w-4 h-4 text-white" />
                <input
                  id="schedule-end-time"
                  type="time"
                  value={formatTime24(endHour, endMinute)}
                  onChange={handleEndTimeChange}
                  className="bg-transparent text-lg font-bold text-white focus:outline-none cursor-pointer"
                />
              </div>
            </div>
          </div>

          {/* Repeat Header & Presets */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-neutral-300 uppercase tracking-wider">
                Repeat
              </span>
            </div>

            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => setSelectedDays(allDays)}
                className={`px-3 py-1 rounded-full text-xs font-medium transition-all ${
                  selectedDays.length === 7
                    ? 'bg-white/25 border border-white/50 text-white font-semibold'
                    : 'bg-white/[0.08] text-neutral-300 hover:text-white'
                }`}
              >
                Daily
              </button>
              <button
                type="button"
                onClick={() => setSelectedDays(weekdays)}
                className={`px-3 py-1 rounded-full text-xs font-medium transition-all ${
                  selectedDays.length === 5 && weekdays.every((d) => selectedDays.includes(d))
                    ? 'bg-white/25 border border-white/50 text-white font-semibold'
                    : 'bg-white/[0.08] text-neutral-300 hover:text-white'
                }`}
              >
                Weekdays
              </button>
              <button
                type="button"
                onClick={() => setSelectedDays(weekends)}
                className={`px-3 py-1 rounded-full text-xs font-medium transition-all ${
                  selectedDays.length === 2 && weekends.every((d) => selectedDays.includes(d))
                    ? 'bg-white/25 border border-white/50 text-white font-semibold'
                    : 'bg-white/[0.08] text-neutral-300 hover:text-white'
                }`}
              >
                Weekends
              </button>
            </div>

            {/* Individual Day Circles */}
            <div className="flex justify-between items-center mt-3">
              {daysList.map(({ dayInt, label }) => {
                const isSelected = selectedDays.includes(dayInt);
                return (
                  <button
                    key={dayInt}
                    type="button"
                    onClick={() => handleToggleDay(dayInt)}
                    className={`w-9 h-9 rounded-full flex items-center justify-center text-xs font-bold transition-all ${
                      isSelected
                        ? 'bg-white text-black shadow-sm'
                        : 'bg-white/[0.08] text-neutral-400 hover:text-white'
                    }`}
                  >
                    {label}
                  </button>
                );
              })}
            </div>
            {selectedDays.length === 0 && (
              <p className="text-xs text-rose-400 mt-1.5">Select at least one day</p>
            )}
          </div>

          {/* Sound Mode */}
          <div>
            <label className="block text-xs font-semibold text-neutral-300 uppercase tracking-wider mb-2">
              Mode
            </label>
            <div className="flex gap-3">
              <button
                type="button"
                onClick={() => setTargetMode(SoundMode.VIBRATE)}
                className={`px-4 py-2 rounded-full text-xs font-semibold transition-all ${
                  targetMode === SoundMode.VIBRATE
                    ? 'bg-white text-black shadow-md'
                    : 'bg-white/[0.08] border border-white/10 text-neutral-300 hover:text-white'
                }`}
              >
                Vibrate
              </button>
              <button
                type="button"
                onClick={() => setTargetMode(SoundMode.SILENT)}
                className={`px-4 py-2 rounded-full text-xs font-semibold transition-all ${
                  targetMode === SoundMode.SILENT
                    ? 'bg-white text-black shadow-md'
                    : 'bg-white/[0.08] border border-white/10 text-neutral-300 hover:text-white'
                }`}
              >
                Silent (DND)
              </button>
            </div>
          </div>

          {/* Actions */}
          <div className="flex items-center justify-end gap-3 pt-4 border-t border-white/10">
            <button
              type="button"
              onClick={onDismiss}
              className="px-4 py-2 rounded-full text-xs font-medium text-neutral-400 hover:text-white transition-colors"
            >
              Cancel
            </button>
            <button
              id="save-schedule-btn"
              type="submit"
              disabled={selectedDays.length === 0}
              className="px-6 py-2 rounded-full bg-white text-black text-xs font-bold hover:bg-neutral-200 active:scale-95 transition-all disabled:opacity-40 disabled:pointer-events-none"
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
