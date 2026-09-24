import React, { useState, useRef, useEffect } from 'react';
import { Vibrate, BellOff, MoreVertical, Edit2, Trash2 } from 'lucide-react';
import { ScheduleRule, SoundMode } from '../types';
import { formatTime24 } from '../hooks/useVibeSchedule';
import { MD3Switch } from './MD3Switch';
import { MD3Card } from './MD3Card';

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
    <MD3Card
      variant={rule.isEnabled ? 'elevated' : 'outlined'}
      className={`p-5 transition-all duration-200 ${
        rule.isEnabled
          ? 'bg-[#1e1f23]'
          : 'bg-[#121316] opacity-65'
      } ${className}`}
    >
      <div className="flex items-start justify-between gap-3">
        <div className="flex-1 min-w-0">
          <h3
            className={`text-sm font-semibold truncate ${
              rule.isEnabled ? 'text-[#e3e2e6]' : 'text-[#8f909a]'
            }`}
          >
            {rule.title}
          </h3>
          <p
            className={`text-2xl font-bold tracking-tight mt-0.5 ${
              rule.isEnabled ? 'text-white' : 'text-[#8f909a]'
            }`}
          >
            {formatTime24(rule.startHour, rule.startMinute)} — {formatTime24(rule.endHour, rule.endMinute)}
          </p>
        </div>

        <div className="flex items-center gap-1.5">
          {/* Official MD3 Switch */}
          <MD3Switch checked={rule.isEnabled} onChange={onToggle} />

          {/* MD3 More Menu */}
          <div className="relative" ref={menuRef}>
            <button
              type="button"
              onClick={() => setMenuOpen(!menuOpen)}
              className="p-1.5 rounded-full text-[#c6c5d0] hover:text-white hover:bg-[#292a2d] transition-colors"
              aria-label="Options"
            >
              <MoreVertical className="w-5 h-5" />
            </button>

            {menuOpen && (
              <div className="absolute right-0 mt-1 w-36 rounded-2xl bg-[#292a2d] border border-[#45464f] shadow-2xl py-1.5 z-30">
                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    onEdit();
                  }}
                  className="w-full flex items-center gap-2.5 px-3.5 py-2 text-xs font-medium text-[#e3e2e6] hover:bg-[#333438] text-left transition-colors"
                >
                  <Edit2 className="w-4 h-4 text-[#bac3ff]" />
                  Edit
                </button>
                <button
                  type="button"
                  onClick={() => {
                    setMenuOpen(false);
                    onDelete();
                  }}
                  className="w-full flex items-center gap-2.5 px-3.5 py-2 text-xs font-medium text-[#ffb4ab] hover:bg-[#93000a]/20 text-left transition-colors"
                >
                  <Trash2 className="w-4 h-4 text-[#ffb4ab]" />
                  Delete
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="flex items-center justify-between mt-4 pt-3 border-t border-[#45464f]/40">
        {/* MD3 AssistChip for Target Mode */}
        <button
          type="button"
          onClick={onEdit}
          className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-[#292a2d] border border-[#45464f]/50 text-[#c6c5d0] text-xs font-medium hover:bg-[#333438] transition-colors"
        >
          {rule.targetMode === SoundMode.VIBRATE ? (
            <Vibrate className="w-3.5 h-3.5 text-[#bac3ff]" />
          ) : (
            <BellOff className="w-3.5 h-3.5 text-[#e5bad8]" />
          )}
          <span>{rule.targetMode === SoundMode.VIBRATE ? 'Vibrate' : 'Silent (DND)'}</span>
        </button>

        {/* MD3 Days of week pills */}
        <div className="flex items-center gap-1.5">
          {dayItems.map(({ dayInt, label }, i) => {
            const isSelected = rule.daysOfWeek.includes(dayInt);
            return (
              <span
                key={i}
                className={`
                  w-6 h-6 rounded-full flex items-center justify-center text-[11px] font-bold transition-all
                  ${
                    !rule.isEnabled
                      ? isSelected
                        ? 'bg-[#333438] text-[#8f909a]'
                        : 'bg-transparent text-[#45464f]'
                      : isSelected
                      ? 'bg-[#bac3ff] text-[#08218a] shadow-xs'
                      : 'bg-[#292a2d] text-[#8f909a]'
                  }
                `}
              >
                {label}
              </span>
            );
          })}
        </div>
      </div>
    </MD3Card>
  );
};
