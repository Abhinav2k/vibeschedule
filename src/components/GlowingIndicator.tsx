import React from 'react';

interface GlowingIndicatorProps {
  isActive: boolean;
  className?: string;
}

export const GlowingIndicator: React.FC<GlowingIndicatorProps> = ({ isActive, className = '' }) => {
  return (
    <div className={`relative flex items-center justify-center w-3.5 h-3.5 ${className}`}>
      {isActive ? (
        <>
          <span className="absolute w-3.5 h-3.5 rounded-full bg-white/40 animate-ping" />
          <span className="relative w-2 h-2 rounded-full bg-white shadow-[0_0_8px_rgba(255,255,255,0.8)]" />
        </>
      ) : (
        <span className="w-2 h-2 rounded-full bg-white/30" />
      )}
    </div>
  );
};
