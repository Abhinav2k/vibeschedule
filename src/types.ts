export enum SoundMode {
  VIBRATE = 'VIBRATE',
  SILENT = 'SILENT',
  NORMAL = 'NORMAL',
}

export const SOUND_MODE_LABELS: Record<SoundMode, string> = {
  [SoundMode.VIBRATE]: 'Vibrate',
  [SoundMode.SILENT]: 'Silent (DND)',
  [SoundMode.NORMAL]: 'Normal (Ring)',
};

export interface ScheduleRule {
  id: string;
  title: string;
  startHour: number;
  startMinute: number;
  endHour: number;
  endMinute: number;
  daysOfWeek: number[]; // 1=Sun, 2=Mon, 3=Tue, 4=Wed, 5=Thu, 6=Fri, 7=Sat
  targetMode: SoundMode;
  revertMode: SoundMode;
  isEnabled: boolean;
}

export interface QuickMuteConflict {
  title: string;
  message: string;
  pendingMinutes: number;
}

export interface ActiveStatusNotification {
  id: number;
  title: string;
  targetMode: SoundMode;
  remainingMinutes?: number;
  totalMinutes?: number;
  endMillis?: number;
  canSkip: boolean;
}

export interface AppSettings {
  notifHighPriority: boolean;
}
