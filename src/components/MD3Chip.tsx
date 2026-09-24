import React from 'react';
import { Check } from 'lucide-react';

interface MD3ChipProps {
  label: string;
  selected?: boolean;
  onClick?: () => void;
  icon?: React.ReactNode;
  disabled?: boolean;
  className?: string;
}

export const MD3Chip: React.FC<MD3ChipProps> = ({
  label,
  selected = false,
  onClick,
  icon,
  disabled = false,
  className = '',
}) => {
  return (
    <button
      type="button"
      disabled={disabled}
      onClick={onClick}
      className={`inline-flex items-center justify-center gap-1.5 px-3.5 py-1.5 rounded-full text-xs font-medium transition-all duration-150 active:scale-95 ${
        selected
          ? 'bg-[#434659] text-[#dfe1f9] border border-[#bac3ff]/40 shadow-xs'
          : 'bg-[#292a2d] text-[#c6c5d0] hover:bg-[#333438] border border-transparent'
      } ${disabled ? 'opacity-40 cursor-not-allowed' : ''} ${className}`}
    >
      {selected ? (
        <Check className="w-3.5 h-3.5 text-[#bac3ff]" />
      ) : (
        icon && <span className="w-3.5 h-3.5 flex items-center justify-center">{icon}</span>
      )}
      <span>{label}</span>
    </button>
  );
};
