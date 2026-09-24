import React from 'react';

interface LiquidTabSwitcherProps {
  selectedTab: number;
  onTabSelected: (index: number) => void;
  tabs: string[];
  className?: string;
}

export const LiquidTabSwitcher: React.FC<LiquidTabSwitcherProps> = ({
  selectedTab,
  onTabSelected,
  tabs,
  className = '',
}) => {
  return (
    <div
      className={`relative inline-flex items-center rounded-full p-1.5 bg-[#121318]/75 backdrop-blur-2xl backdrop-saturate-200 border border-white/20 shadow-[inset_0_1px_1px_0_rgba(255,255,255,0.45),inset_0_-1px_2px_0_rgba(0,0,0,0.6),0_16px_36px_-6px_rgba(0,0,0,0.75)] transition-all duration-300 ${className}`}
    >
      {/* Specular Rim Light */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-x-3 top-0 h-[1px] bg-gradient-to-r from-transparent via-white/50 to-transparent"
      />

      <div className="relative z-10 flex items-center gap-1">
        {tabs.map((tab, idx) => {
          const isSelected = selectedTab === idx;
          return (
            <button
              key={tab}
              id={`tab-button-${tab.toLowerCase()}`}
              type="button"
              onClick={() => onTabSelected(idx)}
              className={`
                relative px-5 py-2 rounded-full text-xs font-semibold transition-all duration-200 cursor-pointer select-none active:scale-95
                ${
                  isSelected
                    ? 'bg-white text-[#0a0a0f] shadow-[0_2px_14px_rgba(255,255,255,0.4),inset_0_1px_1px_rgba(255,255,255,0.9)] scale-100'
                    : 'text-neutral-400 hover:text-white hover:bg-white/[0.08]'
                }
              `}
            >
              {tab}
            </button>
          );
        })}
      </div>
    </div>
  );
};
