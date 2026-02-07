package com.wiffle.caffinate.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SettingsState(
    val isDarkMode: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val dailyGoalMg: Int = 400,
    val maxDailyCaffeineMg: Int = 800,
    val maxCaffeinePerDrinkMg: Int = 400,
    val reminderTime: String = "09:00",
    val autoBackupEnabled: Boolean = true,
    val useExpressiveTheme: Boolean = true,
    val maxHistoryEntries: Int = 1000,
    val customUnits: String = "mg"
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SettingsDbRepository(application)

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled

    private val _dailyGoalMg = MutableStateFlow(400)
    val dailyGoalMg: StateFlow<Int> = _dailyGoalMg

    private val _maxDailyCaffeineMg = MutableStateFlow(800)
    val maxDailyCaffeineMg: StateFlow<Int> = _maxDailyCaffeineMg

    private val _maxCaffeinePerDrinkMg = MutableStateFlow(400)
    val maxCaffeinePerDrinkMg: StateFlow<Int> = _maxCaffeinePerDrinkMg

    private val _reminderTime = MutableStateFlow("09:00")
    val reminderTime: StateFlow<String> = _reminderTime

    private val _autoBackupEnabled = MutableStateFlow(true)
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled

    private val _useExpressiveTheme = MutableStateFlow(true)
    val useExpressiveTheme: StateFlow<Boolean> = _useExpressiveTheme

    private val _maxHistoryEntries = MutableStateFlow(1000)
    val maxHistoryEntries: StateFlow<Int> = _maxHistoryEntries

    private val _customUnits = MutableStateFlow("mg")
    val customUnits: StateFlow<String> = _customUnits

    private val _settingsState = MutableStateFlow(
        SettingsState(
            isDarkMode = _isDarkMode.value,
            notificationsEnabled = _notificationsEnabled.value,
            dailyGoalMg = _dailyGoalMg.value,
            maxDailyCaffeineMg = _maxDailyCaffeineMg.value,
            maxCaffeinePerDrinkMg = _maxCaffeinePerDrinkMg.value,
            reminderTime = _reminderTime.value,
            autoBackupEnabled = _autoBackupEnabled.value,
            useExpressiveTheme = _useExpressiveTheme.value,
            maxHistoryEntries = _maxHistoryEntries.value,
            customUnits = _customUnits.value
        )
    )
    val settingsState: StateFlow<SettingsState> = _settingsState

    init {
        viewModelScope.launch {
            repository.migrateFromPreferencesIfNeeded()
            repository.observeSettings().collect { entity ->
                _isDarkMode.value = entity.darkModeEnabled
                _notificationsEnabled.value = entity.notificationsEnabled
                _dailyGoalMg.value = entity.dailyGoalMg
                _maxDailyCaffeineMg.value = entity.maxDailyCaffeineMg
                _maxCaffeinePerDrinkMg.value = entity.maxCaffeinePerDrinkMg
                _reminderTime.value = "%02d:%02d".format(entity.reminderHour, entity.reminderMinute)
                _autoBackupEnabled.value = entity.autoBackupEnabled
                _useExpressiveTheme.value = entity.useExpressiveTheme
                _maxHistoryEntries.value = entity.maxHistoryEntries
                _customUnits.value = entity.customUnits
                _settingsState.value = SettingsState(
                    isDarkMode = _isDarkMode.value,
                    notificationsEnabled = _notificationsEnabled.value,
                    dailyGoalMg = _dailyGoalMg.value,
                    maxDailyCaffeineMg = _maxDailyCaffeineMg.value,
                    maxCaffeinePerDrinkMg = _maxCaffeinePerDrinkMg.value,
                    reminderTime = _reminderTime.value,
                    autoBackupEnabled = _autoBackupEnabled.value,
                    useExpressiveTheme = _useExpressiveTheme.value,
                    maxHistoryEntries = _maxHistoryEntries.value,
                    customUnits = _customUnits.value
                )
            }
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch { repository.setDarkMode(enabled) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    fun setDailyGoalMg(mg: Int) {
        viewModelScope.launch { repository.setDailyGoalMg(mg) }
    }

    fun setMaxDailyCaffeineMg(mg: Int) {
        viewModelScope.launch { repository.setMaxDailyCaffeineMg(mg) }
    }

    fun setMaxCaffeinePerDrinkMg(mg: Int) {
        viewModelScope.launch { repository.setMaxCaffeinePerDrinkMg(mg) }
    }

    fun setReminderTime(hhmm: String) {
        viewModelScope.launch {
            val match = hhmm.split(":")
            val hour = match.getOrNull(0)?.toIntOrNull() ?: 9
            val minute = match.getOrNull(1)?.toIntOrNull() ?: 0
            repository.setReminderTime(hour, minute)
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setAutoBackupEnabled(enabled) }
    }

    fun setUseExpressiveTheme(enabled: Boolean) {
        viewModelScope.launch { repository.setUseExpressiveTheme(enabled) }
    }

    fun setMaxHistoryEntries(entries: Int) {
        viewModelScope.launch { repository.setMaxHistoryEntries(entries) }
    }

    fun setCustomUnits(units: String) {
        viewModelScope.launch { repository.setCustomUnits(units) }
    }

    fun resetToDefaults() {
        viewModelScope.launch { repository.resetToDefaults() }
    }
}
