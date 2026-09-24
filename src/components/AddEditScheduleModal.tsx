import React, { useState } from 'react';
import { Clock, X, Vibrate, BellOff, Check } from 'lucide-react';
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

    const resolvedTitle = title.trim() || 'Quiet Hours';
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
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-sm animate-in fade-in duration-150">
      <div
        className="w-full max-w-md rounded-[28px] bg-[#292a2d] border border-[#45464f] p-6 shadow-2xl relative max-h-[92vh] overflow-y-auto text-[#e3e2e6]"
        role="dialog"
        aria-modal="true"
      >
        <div className="flex items-center justify-between pb-3 border-b border-[#45464f]/40">
          <h2 className="text-lg font-bold tracking-tight text-[#e3e2e6]">
            {initialRule ? 'Edit schedule' : 'New schedule'}
          </h2>
          <button
            type="button"
            onClick={onDismiss}
            className="p-1.5 rounded-full text-[#c6c5d0] hover:text-white hover:bg-[#333438] transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="mt-4 space-y-4">
          <div>
            <label className="block text-xs font-semibold text-[#c6c5d0] uppercase tracking-wider mb-1.5">
              Name
            </label>
            <input
              id="schedule-title-input"
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="e.g., Work, Sleep"
              className="w-full px-4 py-2.5 rounded-2xl bg-[#1e1f23] border border-[#8f909a] text-white placeholder-[#8f909a] focus:outline-hidden focus:border-[#bac3ff] transition-all text-sm"
            />
          </div>

          {/* Time Picker Boxes */}
          <div className="grid grid-cols-2 gap-3">
            <div className="p-3 rounded-2xl bg-[#1e1f23] border border-[#45464f] flex flex-col items-center">
              <span className="text-xs text-[#c6c5d0]">Starts</span>
              <div className="flex items-center gap-2 mt-1">
                <Clock className="w-4 h-4 text-[#bac3ff]" />
                <input
                  id="schedule-start-time"
                  type="time"
                  value={formatTime24(startHour, startMinute)}
                  onChange={handleStartTimeChange}
                  className="bg-transparent text-lg font-bold text-white focus:outline-hidden cursor-pointer"
                />
              </div>
            </div>

            <div className="p-3 rounded-2xl bg-[#1e1f23] border border-[#45464f] flex flex-col items-center">
              <span className="text-xs text-[#c6c5d0]">Ends</span>
              <div className="flex items-center gap-2 mt-1">
                <Clock className="w-4 h-4 text-[#bac3ff]" />
                <input
                  id="schedule-end-time"
                  type="time"
                  value={formatTime24(endHour, endMinute)}
                  onChange={handleEndTimeChange}
                  className="bg-transparent text-lg font-bold text-white focus:outline-hidden cursor-pointer"
                />
              </div>
            </div>
          </div>

          {/* Repeat Presets */}
          <div>
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold text-[#c6c5d0] uppercase tracking-wider">
                Repeat
              </span>
              <div className="flex items-center gap-1.5">
                <button
                  type="button"
                  onClick={() => setSelectedDays(allDays)}
                  className={`text-[11px] px-2.5 py-0.5 rounded-full font-medium transition-all ${
                    selectedDays.length === 7
                      ? 'bg-[#bac3ff] text-[#08218a] font-bold'
                      : 'bg-[#1e1f23] text-[#c6c5d0] hover:bg-[#333438]'
                  }`}
                >
                  Daily
                </button>
                <button
                  type="button"
                  onClick={() => setSelectedDays(weekdays)}
                  className={`text-[11px] px-2.5 py-0.5 rounded-full font-medium transition-all ${
                    selectedDays.length === 5 && selectedDays.every((d) => weekdays.includes(d))
                      ? 'bg-[#bac3ff] text-[#08218a] font-bold'
                      : 'bg-[#1e1f23] text-[#c6c5d0] hover:bg-[#333438]'
                  }`}
                >
                  Weekdays
                </button>
                <button
                  type="button"
                  onClick={() => setSelectedDays(weekends)}
                  className={`text-[11px] px-2.5 py-0.5 rounded-full font-medium transition-all ${
                    selectedDays.length === 2 && selectedDays.every((d) => weekends.includes(d))
                      ? 'bg-[#bac3ff] text-[#08218a] font-bold'
                      : 'bg-[#1e1f23] text-[#c6c5d0] hover:bg-[#333438]'
                  }`}
                >
                  Weekends
                </button>
              </div>
            </div>

            {/* Individual Day Chips */}
            <div className="flex items-center justify-between gap-1.5 pt-1">
              {daysList.map(({ dayInt, label }) => {
                const isSelected = selectedDays.includes(dayInt);
                return (
                  <button
                    key={dayInt}
                    type="button"
                    onClick={() => handleToggleDay(dayInt)}
                    className={`
                      w-9 h-9 rounded-full flex items-center justify-center text-xs font-semibold transition-all
                      ${
                        isSelected
                          ? 'bg-[#bac3ff] text-[#08218a]'
                          : 'bg-[#1e1f23] text-[#c6c5d0] hover:bg-[#333438]'
                      }
                    `}
                  >
                    {label}
                  </button>
                );
              })}
            </div>
            {selectedDays.length === 0 && (
              <p className="text-xs text-[#ffb4ab] mt-1.5">Select at least one day</p>
            )}
          </div>

          {/* Sound Mode Selector */}
          <div>
            <label className="block text-xs font-semibold text-[#c6c5d0] uppercase tracking-wider mb-2">
              Sound Mode
            </label>
            <div className="grid grid-cols-2 gap-2 p-1 rounded-2xl bg-[#1e1f23] border border-[#45464f]">
              <button
                type="button"
                onClick={() => setTargetMode(SoundMode.VIBRATE)}
                className={`flex items-center justify-center gap-2 py-2 px-3 rounded-xl text-xs font-semibold transition-all ${
                  targetMode === SoundMode.VIBRATE
                    ? 'bg-[#bac3ff] text-[#08218a]'
                    : 'text-[#c6c5d0] hover:text-white'
                }`}
              >
                <Vibrate className="w-4 h-4" />
                <span>Vibrate</span>
                {targetMode === SoundMode.VIBRATE && <Check className="w-3.5 h-3.5 stroke-[3]" />}
              </button>

              <button
                type="button"
                onClick={() => setTargetMode(SoundMode.SILENT)}
                className={`flex items-center justify-center gap-2 py-2 px-3 rounded-xl text-xs font-semibold transition-all ${
                  targetMode === SoundMode.SILENT
                    ? 'bg-[#e5bad8] text-[#45263f]'
                    : 'text-[#c6c5d0] hover:text-white'
                }`}
              >
                <BellOff className="w-4 h-4" />
                <span>Silent</span>
                {targetMode === SoundMode.SILENT && <Check className="w-3.5 h-3.5 stroke-[3]" />}
              </button>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex items-center justify-end gap-3 pt-3 border-t border-[#45464f]/40">
            <button
              type="button"
              onClick={onDismiss}
              className="px-4 py-2 rounded-full text-xs font-medium text-[#c6c5d0] hover:text-white transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={selectedDays.length === 0}
              className="px-5 py-2 rounded-full bg-[#bac3ff] hover:bg-[#c9d0ff] text-[#08218a] text-xs font-bold transition-all active:scale-95 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              Save
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
