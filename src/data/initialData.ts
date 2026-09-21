import { ScheduleRule, SoundMode } from '../types';

export const DEFAULT_SCHEDULES: ScheduleRule[] = [
  {
    id: 'default-work-college',
    title: 'Work / College',
    startHour: 9,
    startMinute: 0,
    endHour: 17,
    endMinute: 0,
    // Monday (2) to Friday (6) in Calendar
    daysOfWeek: [2, 3, 4, 5, 6],
    targetMode: SoundMode.VIBRATE,
    revertMode: SoundMode.NORMAL,
    isEnabled: true,
  },
  {
    id: 'default-night-rest',
    title: 'Night Sleep',
    startHour: 23,
    startMinute: 0,
    endHour: 7,
    endMinute: 0,
    daysOfWeek: [1, 2, 3, 4, 5, 6, 7],
    targetMode: SoundMode.SILENT,
    revertMode: SoundMode.NORMAL,
    isEnabled: true,
  },
];
