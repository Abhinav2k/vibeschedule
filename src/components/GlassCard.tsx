import React from 'react';
import { MD3Card, MD3CardProps } from './MD3Card';

export interface GlassCardProps extends MD3CardProps {}

export const GlassCard: React.FC<GlassCardProps> = (props) => {
  return <MD3Card {...props} />;
};
