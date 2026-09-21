import React from 'react';

interface GlassCardProps {
  children: React.ReactNode;
  className?: string;
  isActive?: boolean;
  onClick?: () => void;
  id?: string;
}

export const GlassCard: React.FC<GlassCardProps> = ({
  children,
  className = '',
  isActive = false,
  onClick,
  id,
}) => {
  return (
    <div
      id={id}
      onClick={onClick}
      className={`
        rounded-[24px] transition-all duration-200
        ${isActive
          ? 'bg-white/[0.11] border border-white/30 shadow-[0_4px_24px_rgba(255,255,255,0.06)]'
          : 'bg-white/[0.06] border border-white/[0.12] hover:border-white/[0.18]'
        }
        backdrop-blur-xl p-5
        ${className}
      `}
    >
      {children}
    </div>
  );
};
