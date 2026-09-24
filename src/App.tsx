import React, { useState } from 'react';
import { Vibrate, Plus, Volume2, VolumeX } from 'lucide-react';
import { useVibeSchedule } from './hooks/useVibeSchedule';
import { ScheduleRule, SoundMode } from './types';
import { HomeScreen } from './screens/HomeScreen';
import { SchedulesScreen } from './screens/SchedulesScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { AddEditScheduleModal } from './components/AddEditScheduleModal';
import { WidgetSimulatorModal } from './components/WidgetSimulatorModal';
import { MD3BottomNav } from './components/MD3BottomNav';

export function App() {
  const {
    schedules,
    settings,
    currentSoundMode,
    activeSchedule,
    activeRemainingMinutes,
    upcomingSchedule,
    isPauseEligible,
    pausedUntilMillis,
    quickMuteUntilMillis,
    quickMuteRemainingSeconds,
    quickMuteConflictInfo,
    requestQuickMute,
    confirmQuickMuteOverride,
    dismissQuickMuteConflict,
    cancelQuickMute,
    pauseUntilNextOClock,
    cancelPause,
    addSchedule,
    updateSchedule,
    toggleSchedule,
    deleteSchedule,
    updateSettings,
  } = useVibeSchedule();

  const [selectedTab, setSelectedTab] = useState<number>(0);
  const [showAddEditModal, setShowAddEditModal] = useState<boolean>(false);
  const [editingRule, setEditingRule] = useState<ScheduleRule | null>(null);
  const [showWidgetModal, setShowWidgetModal] = useState<boolean>(false);
  const [toastMessage, setToastMessage] = useState<string | null>(null);

  const triggerToast = (msg: string) => {
    setToastMessage(msg);
    setTimeout(() => {
      setToastMessage((current) => (current === msg ? null : current));
    }, 2400);
  };

  const handleOpenAdd = () => {
    setEditingRule(null);
    setShowAddEditModal(true);
  };

  const handleOpenEdit = (rule: ScheduleRule) => {
    setEditingRule(rule);
    setShowAddEditModal(true);
  };

  const handleSaveRule = (rule: ScheduleRule) => {
    if (editingRule) {
      updateSchedule(rule);
      triggerToast('Schedule saved');
    } else {
      addSchedule(rule);
      triggerToast('Schedule created');
    }
    setShowAddEditModal(false);
  };

  const handleDeleteRule = (id: string) => {
    deleteSchedule(id);
    triggerToast('Schedule removed');
  };

  const handleResetDefaults = () => {
    localStorage.removeItem('vibe_schedules_prefs');
    window.location.reload();
  };

  return (
    <div className="min-h-screen bg-[#121316] text-[#e3e2e6] flex flex-col selection:bg-[#bac3ff]/30 selection:text-[#bac3ff] relative font-sans">
      {/* App Shell Container */}
      <div className="relative z-10 flex-1 flex flex-col w-full max-w-lg mx-auto pb-16">
        {/* Clean Material 3 Top App Bar */}
        <header className="sticky top-0 z-30 pt-3 pb-3 px-4 bg-[#121316]/95 backdrop-blur-md border-b border-[#45464f]/30">
          <div className="flex items-center justify-between gap-3">
            {/* Left: App Brand & Icon */}
            <div className="flex items-center gap-2.5">
              <div
                className="w-10 h-10 rounded-full bg-[#283b9f] text-[#dee0ff] flex items-center justify-center shadow-xs"
              >
                <Vibrate className="w-5 h-5" />
              </div>
              <h1 className="text-base font-bold text-[#e3e2e6] tracking-tight">
                VibeSchedule
              </h1>
            </div>

            {/* Right: Sound Mode Chip */}
            <div
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-xs font-semibold ${
                currentSoundMode === SoundMode.NORMAL
                  ? 'bg-[#1e1f23] text-emerald-400 border border-emerald-500/30'
                  : 'bg-[#434659] text-[#dfe1f9] border border-[#bac3ff]/40 shadow-xs'
              }`}
            >
              {currentSoundMode === SoundMode.NORMAL ? (
                <Volume2 className="w-3.5 h-3.5" />
              ) : (
                <VolumeX className="w-3.5 h-3.5 text-[#bac3ff]" />
              )}
              <span>
                {currentSoundMode === SoundMode.VIBRATE
                  ? 'Vibrate'
                  : currentSoundMode === SoundMode.SILENT
                  ? 'Silent'
                  : 'Normal'}
              </span>
            </div>
          </div>
        </header>

        {/* Main Content Area */}
        <main className="flex-1 w-full pt-3">
          {selectedTab === 0 && (
            <HomeScreen
              activeSchedule={activeSchedule}
              activeRemainingMinutes={activeRemainingMinutes}
              upcomingSchedule={upcomingSchedule}
              isPauseEligible={isPauseEligible}
              pausedUntilMillis={pausedUntilMillis}
              quickMuteUntilMillis={quickMuteUntilMillis}
              quickMuteRemainingSeconds={quickMuteRemainingSeconds}
              onCancelQuickMute={cancelQuickMute}
              onPauseUntilNextOClock={pauseUntilNextOClock}
              onCancelPause={cancelPause}
            />
          )}

          {selectedTab === 1 && (
            <SchedulesScreen
              schedules={schedules}
              conflictInfo={quickMuteConflictInfo}
              quickMuteUntilMillis={quickMuteUntilMillis}
              quickMuteRemainingSeconds={quickMuteRemainingSeconds}
              onQuickMute={requestQuickMute}
              onCancelQuickMute={cancelQuickMute}
              onKeepConflict={dismissQuickMuteConflict}
              onOverrideConflict={confirmQuickMuteOverride}
              onToggleRule={toggleSchedule}
              onEditRule={handleOpenEdit}
              onDeleteRule={handleDeleteRule}
              onAddNewRule={handleOpenAdd}
            />
          )}

          {selectedTab === 2 && (
            <SettingsScreen
              settings={settings}
              onUpdateSettings={updateSettings}
              onResetDefaults={handleResetDefaults}
              onOpenWidgetPreview={() => setShowWidgetModal(true)}
            />
          )}
        </main>

        {/* Floating Action Button (FAB) on Schedules tab */}
        {selectedTab === 1 && (
          <button
            id="fab-add-schedule"
            type="button"
            onClick={handleOpenAdd}
            aria-label="Add Schedule"
            className="fixed bottom-20 right-6 z-40 w-14 h-14 rounded-2xl bg-[#bac3ff] text-[#08218a] flex items-center justify-center shadow-lg hover:bg-[#c9d0ff] active:scale-95 transition-all duration-150 cursor-pointer"
          >
            <Plus className="w-7 h-7 stroke-[2.5]" />
          </button>
        )}

        {/* Material 3 Bottom Navigation Bar */}
        <div className="fixed bottom-0 left-0 right-0 z-30">
          <div className="max-w-lg mx-auto">
            <MD3BottomNav
              selectedTab={selectedTab}
              onTabSelected={(idx) => setSelectedTab(idx)}
            />
          </div>
        </div>

        {/* Toast Feedback */}
        {toastMessage && (
          <div className="fixed top-16 left-1/2 -translate-x-1/2 z-50 px-4 py-2 rounded-full bg-[#292a2d] border border-[#45464f] text-xs font-semibold text-[#e3e2e6] shadow-xl animate-in fade-in duration-150">
            {toastMessage}
          </div>
        )}

        {/* Add/Edit Schedule Dialog */}
        {showAddEditModal && (
          <AddEditScheduleModal
            initialRule={editingRule}
            onDismiss={() => setShowAddEditModal(false)}
            onSave={handleSaveRule}
          />
        )}

        {/* Widget Preview Modal */}
        {showWidgetModal && (
          <WidgetSimulatorModal
            isOpen={showWidgetModal}
            onClose={() => setShowWidgetModal(false)}
            activeSchedule={activeSchedule}
            quickMuteUntilMillis={quickMuteUntilMillis}
            pausedUntilMillis={pausedUntilMillis}
            onCancelActive={() => {
              if (quickMuteUntilMillis !== null) cancelQuickMute();
              else if (pausedUntilMillis !== null) cancelPause();
              else if (activeSchedule) pauseUntilNextOClock();
              triggerToast('Normal ring restored');
            }}
            onSkipPeriod={() => {
              pauseUntilNextOClock();
              triggerToast('Period skipped');
            }}
          />
        )}
      </div>
    </div>
  );
}

export default App;
