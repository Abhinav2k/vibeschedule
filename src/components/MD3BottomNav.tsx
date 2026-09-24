import React from 'react';
import { Home, Calendar, Settings } from 'lucide-react';

interface MD3BottomNavProps {
  selectedTab: number;
  onTabSelected: (index: number) => void;
  className?: string;
}

export const MD3BottomNav: React.FC<MD3BottomNavProps> = ({
  selectedTab,
  onTabSelected,
  className = '',
}) => {
  const items = [
    { label: 'Home', icon: Home },
    { label: 'Schedules', icon: Calendar },
    { label: 'Settings', icon: Settings },
  ];

  return (
    <nav
      aria-label="Bottom Navigation"
      className={`relative inline-flex items-center p-1.5 rounded-full bg-[#121318]/75 backdrop-blur-2xl backdrop-saturate-200 border border-white/20 shadow-[inset_0_1px_1px_0_rgba(255,255,255,0.45),inset_0_-1px_2px_0_rgba(0,0,0,0.6),0_20px_40px_-8px_rgba(0,0,0,0.8),0_8px_16px_-4px_rgba(0,0,0,0.5)] transition-all duration-300 ${className}`}
    >
      {/* Specular Liquid Glass Top Rim Highlight */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-x-4 top-0 h-[1px] bg-gradient-to-r from-transparent via-white/60 to-transparent"
      />

      {/* Subtle Ambient Glass Gradient Sheen */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 rounded-full bg-gradient-to-b from-white/[0.12] via-transparent to-black/[0.25]"
      />

      <div className="relative z-10 flex items-center gap-1">
        {items.map((item, index) => {
          const isSelected = selectedTab === index;
          const Icon = item.icon;

          return (
            <button
              key={item.label}
              id={`nav-tab-${item.label.toLowerCase()}`}
              type="button"
              onClick={() => onTabSelected(index)}
              className={`
                relative flex items-center gap-2 py-2 px-4 rounded-full text-xs font-semibold
                transition-all duration-200 cursor-pointer select-none focus:outline-hidden
                active:scale-95
                ${
                  isSelected
                    ? 'bg-white text-[#0a0a0f] shadow-[0_2px_14px_rgba(255,255,255,0.4),inset_0_1px_1px_rgba(255,255,255,0.9)] scale-100'
                    : 'text-neutral-400 hover:text-white hover:bg-white/[0.08] active:bg-white/[0.14]'
                }
              `}
            >
              <Icon
                className={`w-4 h-4 transition-transform duration-200 ${
                  isSelected ? 'stroke-[2.5] scale-105' : 'stroke-[2]'
                }`}
              />
              <span
                className={`tracking-tight transition-all duration-200 ${
                  isSelected ? 'font-bold' : 'font-medium'
                }`}
              >
                {item.label}
              </span>
            </button>
          );
        })}
      </div>
    </nav>
  );
};
