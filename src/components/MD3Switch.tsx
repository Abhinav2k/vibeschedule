import React from 'react';
import { Check } from 'lucide-react';

interface MD3SwitchProps {
  checked: boolean;
  onChange: (checked: boolean) => void;
  disabled?: boolean;
}

export const MD3Switch: React.FC<MD3SwitchProps> = ({
  checked,
  onChange,
  disabled = false,
}) => {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      disabled={disabled}
      onClick={() => onChange(!checked)}
      className={`relative inline-flex h-8 w-[52px] shrink-0 cursor-pointer rounded-full border-2 transition-colors duration-200 ease-in-out focus:outline-hidden ${
        checked
          ? 'bg-[#bac3ff] border-[#bac3ff]'
          : 'bg-[#1a1b1e] border-[#8f909a]'
      } ${disabled ? 'opacity-40 cursor-not-allowed' : ''}`}
    >
      <span
        className={`pointer-events-none flex items-center justify-center rounded-full transition-transform duration-200 ease-in-out ${
          checked
            ? 'translate-x-[20px] h-6 w-6 mt-[2px] bg-[#08218a] text-[#bac3ff]'
            : 'translate-x-[4px] h-4 w-4 mt-[6px] bg-[#8f909a]'
        }`}
      >
        {checked && <Check className="w-3.5 h-3.5 stroke-[3]" />}
      </span>
    </button>
  );
};
