import React from 'react';
import { Home, Calendar, Settings } from 'lucide-react';

interface MD3BottomNavProps {
  selectedTab: number;
  onTabSelected: (index: number) => void;
}

export const MD3BottomNav: React.FC<MD3BottomNavProps> = ({
  selectedTab,
  onTabSelected,
}) => {
  const items = [
    { label: 'Home', icon: Home },
    { label: 'Schedules', icon: Calendar },
    { label: 'Settings', icon: Settings },
  ];

  return (
    <nav className="w-full bg-[#1e1f23] border-t border-[#45464f]/30 px-6 py-2 flex items-center justify-around z-30 shadow-[0_-2px_8px_rgba(0,0,0,0.3)]">
      {items.map((item, index) => {
        const isSelected = selectedTab === index;
        const Icon = item.icon;

        return (
          <button
            key={item.label}
            type="button"
            onClick={() => onTabSelected(index)}
            className="flex flex-col items-center gap-1 group py-1 px-4 cursor-pointer focus:outline-hidden"
          >
            <div
              className={`flex items-center justify-center px-5 py-1 rounded-full transition-all duration-200 ${
                isSelected
                  ? 'bg-[#bac3ff] text-[#08218a]'
                  : 'bg-transparent text-[#c6c5d0] group-hover:text-white group-hover:bg-[#292a2d]'
              }`}
            >
              <Icon className="w-5 h-5 stroke-[2.2]" />
            </div>
            <span
              className={`text-[11px] font-bold tracking-tight transition-colors ${
                isSelected ? 'text-[#bac3ff]' : 'text-[#8f909a]'
              }`}
            >
              {item.label}
            </span>
          </button>
        );
      })}
    </nav>
  );
};
