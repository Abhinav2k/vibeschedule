import React, { useState, useRef, useEffect } from 'react';
import { Vibrate, BellOff, MoreHorizontal, Edit2, Trash2 } from 'lucide-react';
import { ScheduleRule, SoundMode } from '../types';
import { formatTime24 } from '../hooks/useVibeSchedule';

interface ScheduleCardProps {
  rule: ScheduleRule;
  onToggle: (isEnabled: boolean) => void;
  onEdit: () => void;
  onDelete: () => void;
  className?: string;
}

export const ScheduleCard: React.FC<ScheduleCardProps> = ({
  rule,
  onToggle,
  onEdit,
  onDelete,
  className = '',
}) => {
  const [menuOpen, setMenuOpen] = useState(false);
  const menuRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        setMenuOpen(false);
      }
    }
    if (menuOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [menuOpen]);

  // Calendar days: 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat, 1=Sun
  const dayItems = [
    { dayInt: 2, label: 'M' },
    { dayInt: 3, label: 'T' },
    { dayInt: 4, label: 'W' },
    { dayInt: 5, label: 'T' },
    { dayInt: 6, label: 'F' },
    { dayInt: 7, label: 'S' },
    { dayInt: 1, label: 'S' },
  ];

  return (
    <div
      id={`schedule-card-${rule.id}`}
      className={`
        relative rounded-[22px] p-5 backdrop-blur-xl transition-all duration-200
        ${rule.isEnabled
          ? 'bg-white/[0.08] border border-white/[0.14] shadow-sm'
          : 'bg-white/[0.03] border border-white/[0.06] opacity-60'
        }
        ${className}
      `}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <h3
            className={`text-base font-semibold truncate ${
              rule.isEnabled ? 'text-neutral-100' : 'text-neutral-400'
            }`}
          >
            {rule.title}
          </h3>
          <p
            className={`text-2xl font-bold tracking-tight mt-0.5 ${
              rule.isEnabled ? 'text-white' : 'text-neutral-500'
            }`}
          >
            {formatTime24(rule.startHour, rule.startMinute)} — {formatTime24(rule.endHour, rule.endMinute)}
          </p>
        </div>

        <div className="flex items-center gap-1.5">
          {/* iOS / Jetpack Compose style Switch */}
          <button
            type="button"
            role="switch"
            aria-checked={rule.isEnabled}
            onClick={() => onToggle(!rule.isEnabled)}
            className={`
              relative inline-flex h-7 w-12 shrink-0 cursor-pointer rounded-full p-0.5 transition-colors duration-200 ease-in-out
              ${rule.isEnabled ? 'bg-white' : 'bg-white/20'}
            `}
          >
            <span
              className={`
                pointer-events-none inline-block h-6 w-6 transform rounded-full shadow-md transition duration-200 ease-in-out
                ${rule.isEnabled ? 'translate-x-5 bg-black' : 'translate-x-0 bg-neutral-300'}
              `}
            />
          </button>

          {/* More menu dropdown */}
          <div className="relative" ref={menuRef}>
            <button
              type="button"
              onClick={() => setMenuOpen(!menuOpen)}
              className="p-1.5 rounded-full text-neutral-400 hover:text-white hover:bg-white/10 transition-colors"
              aria-label="Options"
            >
              <MoreHorizontal className="w-5 h-5" />
            </button>

            {menuOpen && (
              <div className="absolute right-0 mt-1 w-32 rounded-xl bg-neutral-900 border border-white/20 shadow-2xl py-1 z-30 backdrop-blur-xl">
                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    onEdit();
                  }}
                  className="w-full flex items-center gap-2 px-3 py-2 text-xs font-medium text-neutral-200 hover:bg-white/10 text-left transition-colors"
                >
                  <Edit2 className="w-3.5 h-3.5" />
                  Edit
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    onDelete();
                  }}
                  className="w-full flex items-center gap-2 px-3 py-2 text-xs font-medium text-rose-400 hover:bg-rose-500/20 text-left transition-colors"
                >
                  <Trash2 className="w-3.5 h-3.5" />
                  Delete
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="flex items-center justify-between mt-4 pt-2 border-t border-white/[0.06]">
        {/* Mode pill */}
        <div className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-white/[0.08] border border-white/[0.14] text-neutral-300 text-xs font-medium">
          {rule.targetMode === SoundMode.VIBRATE ? (
            <Vibrate className="w-3.5 h-3.5" />
          ) : (
            <BellOff className="w-3.5 h-3.5" />
          )}
          <span>{rule.targetMode === SoundMode.VIBRATE ? 'Vibrate' : 'Silent (DND)'}</span>
        </div>

        {/* Days of week dots */}
        <div className="flex items-center gap-1">
          {dayItems.map(({ dayInt, label }, i) => {
            const isSelected = rule.daysOfWeek.includes(dayInt);
            return (
              <span
                key={i}
                className={`
                  w-5 h-5 rounded-full flex items-center justify-center text-[10px] font-bold transition-all
                  ${
                    !rule.isEnabled
                      ? isSelected
                        ? 'bg-white/10 text-neutral-500'
                        : 'text-neutral-600'
                      : isSelected
                      ? 'bg-white text-black shadow-xs'
                      : 'bg-white/[0.05] text-neutral-500'
                  }
                `}
              >
                {label}
              </span>
            );
          })}
        </div>
      </div>
    </div>
  );
};
