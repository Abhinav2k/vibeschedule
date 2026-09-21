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
      className={`inline-flex items-center rounded-full bg-white/[0.08] border border-white/[0.12] p-1 backdrop-blur-md ${className}`}
    >
      {tabs.map((tab, idx) => {
        const isSelected = selectedTab === idx;
        return (
          <button
            key={tab}
            id={`tab-button-${tab.toLowerCase()}`}
            type="button"
            onClick={() => onTabSelected(idx)}
            className={`
              relative px-5 py-1.5 rounded-full text-xs sm:text-sm font-medium transition-all duration-200
              ${
                isSelected
                  ? 'bg-white/20 text-white shadow-sm border border-white/30 font-semibold'
                  : 'text-neutral-400 hover:text-neutral-200 hover:bg-white/[0.04]'
              }
            `}
          >
            {tab}
          </button>
        );
      })}
    </div>
  );
};
