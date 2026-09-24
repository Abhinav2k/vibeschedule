import React from 'react';

export interface MD3CardProps {
  children: React.ReactNode;
  variant?: 'elevated' | 'filled' | 'outlined';
  className?: string;
  onClick?: () => void;
  isActive?: boolean;
}

export const MD3Card: React.FC<MD3CardProps> = ({
  children,
  variant = 'elevated',
  className = '',
  onClick,
  isActive = false,
}) => {
  let baseStyle = 'transition-all duration-200 overflow-hidden ';

  if (isActive) {
    baseStyle += 'bg-[#283b9f]/30 text-[#dee0ff] border border-[#bac3ff]/30 shadow-md ';
  } else if (variant === 'elevated') {
    baseStyle += 'bg-[#1e1f23] text-[#e3e2e6] shadow-[0_1px_3px_1px_rgba(0,0,0,0.25)] border border-[#45464f]/30 ';
  } else if (variant === 'outlined') {
    baseStyle += 'bg-[#1a1b1e] text-[#e3e2e6] border border-[#45464f] ';
  } else {
    // filled
    baseStyle += 'bg-[#292a2d] text-[#e3e2e6] ';
  }

  return (
    <div
      onClick={onClick}
      className={`${baseStyle} rounded-[28px] ${className}`}
    >
      {children}
    </div>
  );
};
