import React, { useState } from 'react';
import { Vibrate, Plus, Volume2, VolumeX, Smartphone } from 'lucide-react';
import { useVibeSchedule } from './hooks/useVibeSchedule';
import { ScheduleRule, SoundMode } from './types';
import { LiquidTabSwitcher } from './components/LiquidTabSwitcher';
import { HomeScreen } from './screens/HomeScreen';
import { SchedulesScreen } from './screens/SchedulesScreen';
import { SettingsScreen } from './screens/SettingsScreen';
import { AddEditScheduleModal } from './components/AddEditScheduleModal';
import { NotificationSimulator } from './components/NotificationSimulator';
import { WidgetSimulatorModal } from './components/WidgetSimulatorModal';
import { DEFAULT_SCHEDULES } from './data/initialData';

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
    }, 2800);
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
      triggerToast(`Updated '${rule.title}'`);
    } else {
      addSchedule(rule);
      triggerToast(`Added '${rule.title}'`);
    }
    setShowAddEditModal(false);
  };

  const handleDeleteRule = (id: string) => {
    const target = schedules.find((s) => s.id === id);
    deleteSchedule(id);
    if (target) {
      triggerToast(`Deleted '${target.title}'`);
    }
  };

  const handleResetDefaults = () => {
    localStorage.removeItem('vibe_schedules_prefs');
    window.location.reload();
  };

  return (
    <div className="min-h-screen bg-black text-neutral-100 flex flex-col selection:bg-white/20 selection:text-white relative font-sans">
      {/* Background Obsidian Mesh Gradients */}
      <div className="fixed inset-0 pointer-events-none z-0">
        <div className="absolute top-[-10%] left-[20%] w-[60vw] h-[50vh] rounded-full bg-white/[0.025] blur-[120px]" />
        <div className="absolute bottom-[-10%] right-[10%] w-[50vw] h-[50vh] rounded-full bg-white/[0.015] blur-[140px]" />
      </div>

      {/* App Shell Container */}
      <div className="relative z-10 flex-1 flex flex-col w-full max-w-lg mx-auto">
        {/* Top App Bar */}
        <header className="sticky top-0 z-30 pt-3 pb-2 px-4 backdrop-blur-xl bg-black/70 border-b border-white/[0.06]">
          <div className="flex items-center justify-between gap-2">
            {/* Left: App Logo Badge */}
            <div className="flex items-center gap-2">
              <div
                className="w-9 h-9 rounded-full bg-white/[0.1] border border-white/[0.2] flex items-center justify-center shadow-xs"
                title="VibeSchedule"
              >
                <Vibrate className="w-4 h-4 text-white" />
              </div>
              <span className="hidden sm:inline text-sm font-bold text-white tracking-tight">
                VibeSchedule
              </span>
            </div>

            {/* Center: Liquid Tab Switcher */}
            <div className="flex justify-center flex-1 sm:flex-none">
              <LiquidTabSwitcher
                selectedTab={selectedTab}
                onTabSelected={(idx) => setSelectedTab(idx)}
                tabs={['Home', 'Schedules', 'Settings']}
              />
            </div>

            {/* Right: Sound Mode & Widget simulator shortcut */}
            <div className="flex items-center gap-1.5">
              <button
                type="button"
                onClick={() => setShowWidgetModal(true)}
                className="p-1.5 rounded-full bg-white/[0.06] hover:bg-white/[0.12] border border-white/10 text-neutral-300 hover:text-white transition-all text-xs flex items-center gap-1"
                title="Open Android Widget Preview"
              >
                <Smartphone className="w-3.5 h-3.5" />
                <span className="hidden md:inline text-[11px] font-medium pr-1">Widget</span>
              </button>

              <div
                className="flex items-center gap-1 px-2 py-1 rounded-full bg-white/[0.08] border border-white/15 text-[10px] font-bold text-neutral-200 uppercase tracking-wider"
                title={`Current ringer status: ${currentSoundMode}`}
              >
                {currentSoundMode === SoundMode.NORMAL ? (
                  <Volume2 className="w-3 h-3 text-emerald-400" />
                ) : (
                  <VolumeX className="w-3 h-3 text-white" />
                )}
                <span className="hidden xs:inline">
                  {currentSoundMode === SoundMode.VIBRATE
                    ? 'VIBE'
                    : currentSoundMode === SoundMode.SILENT
                    ? 'SILENT'
                    : 'RING'}
                </span>
              </div>
            </div>
          </div>
        </header>

        {/* Main Content Area */}
        <main className="flex-1 w-full pt-2">
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
              onNavigateToSchedules={() => setSelectedTab(1)}
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

        {/* Android Lock-Screen Notification Simulator (Active status banner) */}
        <footer className="fixed bottom-0 left-0 right-0 z-20 pointer-events-none">
          <div className="pointer-events-auto">
            <NotificationSimulator
              activeSchedule={activeSchedule}
              activeRemainingMinutes={activeRemainingMinutes}
              quickMuteRemainingSeconds={quickMuteRemainingSeconds}
              quickMuteUntilMillis={quickMuteUntilMillis}
              pausedUntilMillis={pausedUntilMillis}
              onCancelActive={() => {
                if (quickMuteUntilMillis !== null) cancelQuickMute();
                else if (pausedUntilMillis !== null) cancelPause();
                else if (activeSchedule) pauseUntilNextOClock();
                triggerToast('Reverted to Normal Ring');
              }}
              onSkipPeriod={() => {
                pauseUntilNextOClock();
                triggerToast('Skipped until next :00');
              }}
              highPriority={settings.notifHighPriority}
            />
          </div>
        </footer>

        {/* Floating Action Button (FAB) on Schedules tab */}
        {selectedTab === 1 && (
          <button
            id="fab-add-schedule"
            type="button"
            onClick={handleOpenAdd}
            aria-label="Add Schedule"
            className="fixed bottom-6 right-6 z-30 w-14 h-14 rounded-full bg-white text-black flex items-center justify-center shadow-[0_8px_30px_rgba(255,255,255,0.25)] hover:bg-neutral-200 active:scale-95 transition-all duration-150"
          >
            <Plus className="w-7 h-7 stroke-[2.5]" />
          </button>
        )}

        {/* Toast feedback */}
        {toastMessage && (
          <div className="fixed top-16 left-1/2 -translate-x-1/2 z-50 px-4 py-2 rounded-full bg-neutral-900/95 border border-white/20 text-xs font-semibold text-white shadow-2xl backdrop-blur-xl animate-in fade-in slide-in-from-top-3 duration-200">
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

        {/* Widget preview modal */}
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
            triggerToast('Schedule Cancelled • Normal Ring');
          }}
          onSkipPeriod={() => {
            pauseUntilNextOClock();
            triggerToast('Skipped schedule until next :00');
          }}
        />
      </div>
    </div>
  );
}
export default App;
