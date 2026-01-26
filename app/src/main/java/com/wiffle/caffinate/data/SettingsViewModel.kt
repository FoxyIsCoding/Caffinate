package com.wiffle.caffinate.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsState(
    val isDarkMode: Boolean = true,
    val notificationsEnabled: Boolean = true,
    val dailyGoalMg: Int = 400,
    val maxDailyCaffeineMg: Int = 800,
    val maxCaffeinePerDrinkMg: Int = 400,
    val reminderTime: String = "09:00",
    val autoBackupEnabled: Boolean = false,
    val useExpressiveTheme: Boolean = true,
    val maxHistoryEntries: Int = 1000,
    val customUnits: String = "mg"
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs: SharedPreferences =
        application.applicationContext.getSharedPreferences("caffinate_settings", Context.MODE_PRIVATE)

    private object Keys {
        const val DARK_MODE = "dark_mode"
        const val NOTIFICATIONS_ENABLED = "notifications_enabled"
        const val DAILY_GOAL_MG = "daily_goal_mg"
        const val MAX_DAILY_CAFFEINE_MG = "max_daily_caffeine_mg"
        const val MAX_CAFFEINE_PER_DRINK_MG = "max_caffeine_per_drink_mg"
        const val REMINDER_TIME = "reminder_time"
        const val AUTO_BACKUP = "auto_backup"
        const val USE_EXPRESSIVE_THEME = "use_expressive_theme"
        const val MAX_HISTORY_ENTRIES = "max_history_entries"
        const val CUSTOM_UNITS = "custom_units"
    }

    private object Defaults {
        const val DARK_MODE = true
        const val NOTIFICATIONS = true
        const val DAILY_GOAL_MG = 400
        const val MAX_DAILY_CAFFEINE_MG = 800
        const val MAX_CAFFEINE_PER_DRINK_MG = 400
        const val REMINDER_TIME = "09:00"
        const val AUTO_BACKUP = false
        const val USE_EXPRESSIVE_THEME = true
        const val MAX_HISTORY_ENTRIES = 1000
        const val CUSTOM_UNITS = "mg"
    }

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean(Keys.DARK_MODE, Defaults.DARK_MODE))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _notificationsEnabled =
        MutableStateFlow(prefs.getBoolean(Keys.NOTIFICATIONS_ENABLED, Defaults.NOTIFICATIONS))
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _dailyGoalMg = MutableStateFlow(prefs.getInt(Keys.DAILY_GOAL_MG, Defaults.DAILY_GOAL_MG))
    val dailyGoalMg: StateFlow<Int> = _dailyGoalMg.asStateFlow()

    private val _maxDailyCaffeineMg =
        MutableStateFlow(prefs.getInt(Keys.MAX_DAILY_CAFFEINE_MG, Defaults.MAX_DAILY_CAFFEINE_MG))
    val maxDailyCaffeineMg: StateFlow<Int> = _maxDailyCaffeineMg.asStateFlow()

    private val _maxCaffeinePerDrinkMg =
        MutableStateFlow(prefs.getInt(Keys.MAX_CAFFEINE_PER_DRINK_MG, Defaults.MAX_CAFFEINE_PER_DRINK_MG))
    val maxCaffeinePerDrinkMg: StateFlow<Int> = _maxCaffeinePerDrinkMg.asStateFlow()

    private val _reminderTime =
        MutableStateFlow(prefs.getString(Keys.REMINDER_TIME, Defaults.REMINDER_TIME) ?: Defaults.REMINDER_TIME)
    val reminderTime: StateFlow<String> = _reminderTime.asStateFlow()

    private val _autoBackupEnabled = MutableStateFlow(prefs.getBoolean(Keys.AUTO_BACKUP, Defaults.AUTO_BACKUP))
    val autoBackupEnabled: StateFlow<Boolean> = _autoBackupEnabled.asStateFlow()

    private val _useExpressiveTheme =
        MutableStateFlow(prefs.getBoolean(Keys.USE_EXPRESSIVE_THEME, Defaults.USE_EXPRESSIVE_THEME))
    val useExpressiveTheme: StateFlow<Boolean> = _useExpressiveTheme.asStateFlow()

    private val _maxHistoryEntries =
        MutableStateFlow(prefs.getInt(Keys.MAX_HISTORY_ENTRIES, Defaults.MAX_HISTORY_ENTRIES))
    val maxHistoryEntries: StateFlow<Int> = _maxHistoryEntries.asStateFlow()

    private val _customUnits =
        MutableStateFlow(prefs.getString(Keys.CUSTOM_UNITS, Defaults.CUSTOM_UNITS) ?: Defaults.CUSTOM_UNITS)
    val customUnits: StateFlow<String> = _customUnits.asStateFlow()

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
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        when (key) {
            Keys.DARK_MODE -> _isDarkMode.value = prefs.getBoolean(Keys.DARK_MODE, Defaults.DARK_MODE)
            Keys.NOTIFICATIONS_ENABLED -> _notificationsEnabled.value =
                prefs.getBoolean(Keys.NOTIFICATIONS_ENABLED, Defaults.NOTIFICATIONS)

            Keys.DAILY_GOAL_MG -> _dailyGoalMg.value = prefs.getInt(Keys.DAILY_GOAL_MG, Defaults.DAILY_GOAL_MG)
            Keys.MAX_DAILY_CAFFEINE_MG -> _maxDailyCaffeineMg.value =
                prefs.getInt(Keys.MAX_DAILY_CAFFEINE_MG, Defaults.MAX_DAILY_CAFFEINE_MG)

            Keys.MAX_CAFFEINE_PER_DRINK_MG -> _maxCaffeinePerDrinkMg.value =
                prefs.getInt(Keys.MAX_CAFFEINE_PER_DRINK_MG, Defaults.MAX_CAFFEINE_PER_DRINK_MG)

            Keys.REMINDER_TIME -> _reminderTime.value =
                prefs.getString(Keys.REMINDER_TIME, Defaults.REMINDER_TIME) ?: Defaults.REMINDER_TIME

            Keys.AUTO_BACKUP -> _autoBackupEnabled.value = prefs.getBoolean(Keys.AUTO_BACKUP, Defaults.AUTO_BACKUP)
            Keys.USE_EXPRESSIVE_THEME -> _useExpressiveTheme.value =
                prefs.getBoolean(Keys.USE_EXPRESSIVE_THEME, Defaults.USE_EXPRESSIVE_THEME)

            Keys.MAX_HISTORY_ENTRIES -> _maxHistoryEntries.value =
                prefs.getInt(Keys.MAX_HISTORY_ENTRIES, Defaults.MAX_HISTORY_ENTRIES)

            Keys.CUSTOM_UNITS -> _customUnits.value =
                prefs.getString(Keys.CUSTOM_UNITS, Defaults.CUSTOM_UNITS) ?: Defaults.CUSTOM_UNITS
        }
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

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            prefs.edit().putBoolean(Keys.DARK_MODE, enabled).apply()
            _isDarkMode.value = enabled
            _settingsState.value = _settingsState.value.copy(isDarkMode = enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.edit().putBoolean(Keys.NOTIFICATIONS_ENABLED, enabled).apply()
            _notificationsEnabled.value = enabled
            _settingsState.value = _settingsState.value.copy(notificationsEnabled = enabled)
        }
    }

    fun setDailyGoalMg(mg: Int) {
        viewModelScope.launch {
            val value = mg.coerceIn(0, 20000)
            prefs.edit().putInt(Keys.DAILY_GOAL_MG, value).apply()
            _dailyGoalMg.value = value
            _settingsState.value = _settingsState.value.copy(dailyGoalMg = value)
        }
    }

    fun setMaxDailyCaffeineMg(mg: Int) {
        viewModelScope.launch {
            val value = mg.coerceAtLeast(0)
            prefs.edit().putInt(Keys.MAX_DAILY_CAFFEINE_MG, value).apply()
            _maxDailyCaffeineMg.value = value
            _settingsState.value = _settingsState.value.copy(maxDailyCaffeineMg = value)
        }
    }

    fun setMaxCaffeinePerDrinkMg(mg: Int) {
        viewModelScope.launch {
            val value = mg.coerceAtLeast(0)
            prefs.edit().putInt(Keys.MAX_CAFFEINE_PER_DRINK_MG, value).apply()
            _maxCaffeinePerDrinkMg.value = value
            _settingsState.value = _settingsState.value.copy(maxCaffeinePerDrinkMg = value)
        }
    }

    fun setReminderTime(hhmm: String) {
        viewModelScope.launch {
            val value = if (hhmm.matches(Regex("^\\d{1,2}:\\d{2}$"))) hhmm else Defaults.REMINDER_TIME
            prefs.edit().putString(Keys.REMINDER_TIME, value).apply()
            _reminderTime.value = value
            _settingsState.value = _settingsState.value.copy(reminderTime = value)
        }
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.edit().putBoolean(Keys.AUTO_BACKUP, enabled).apply()
            _autoBackupEnabled.value = enabled
            _settingsState.value = _settingsState.value.copy(autoBackupEnabled = enabled)
        }
    }

    fun setUseExpressiveTheme(enabled: Boolean) {
        viewModelScope.launch {
            prefs.edit().putBoolean(Keys.USE_EXPRESSIVE_THEME, enabled).apply()
            _useExpressiveTheme.value = enabled
            _settingsState.value = _settingsState.value.copy(useExpressiveTheme = enabled)
        }
    }

    fun setMaxHistoryEntries(entries: Int) {
        viewModelScope.launch {
            val v = entries.coerceIn(10, 100_000)
            prefs.edit().putInt(Keys.MAX_HISTORY_ENTRIES, v).apply()
            _maxHistoryEntries.value = v
            _settingsState.value = _settingsState.value.copy(maxHistoryEntries = v)
        }
    }

    fun setCustomUnits(units: String) {
        viewModelScope.launch {
            val normalized = units.trim().ifEmpty { Defaults.CUSTOM_UNITS }
            prefs.edit().putString(Keys.CUSTOM_UNITS, normalized).apply()
            _customUnits.value = normalized
            _settingsState.value = _settingsState.value.copy(customUnits = normalized)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            prefs.edit()
                .putBoolean(Keys.DARK_MODE, Defaults.DARK_MODE)
                .putBoolean(Keys.NOTIFICATIONS_ENABLED, Defaults.NOTIFICATIONS)
                .putInt(Keys.DAILY_GOAL_MG, Defaults.DAILY_GOAL_MG)
                .putInt(Keys.MAX_DAILY_CAFFEINE_MG, Defaults.MAX_DAILY_CAFFEINE_MG)
                .putInt(Keys.MAX_CAFFEINE_PER_DRINK_MG, Defaults.MAX_CAFFEINE_PER_DRINK_MG)
                .putString(Keys.REMINDER_TIME, Defaults.REMINDER_TIME)
                .putBoolean(Keys.AUTO_BACKUP, Defaults.AUTO_BACKUP)
                .putBoolean(Keys.USE_EXPRESSIVE_THEME, Defaults.USE_EXPRESSIVE_THEME)
                .putInt(Keys.MAX_HISTORY_ENTRIES, Defaults.MAX_HISTORY_ENTRIES)
                .putString(Keys.CUSTOM_UNITS, Defaults.CUSTOM_UNITS)
                .apply()
            _isDarkMode.value = Defaults.DARK_MODE
            _notificationsEnabled.value = Defaults.NOTIFICATIONS
            _dailyGoalMg.value = Defaults.DAILY_GOAL_MG
            _maxDailyCaffeineMg.value = Defaults.MAX_DAILY_CAFFEINE_MG
            _maxCaffeinePerDrinkMg.value = Defaults.MAX_CAFFEINE_PER_DRINK_MG
            _reminderTime.value = Defaults.REMINDER_TIME
            _autoBackupEnabled.value = Defaults.AUTO_BACKUP
            _useExpressiveTheme.value = Defaults.USE_EXPRESSIVE_THEME
            _maxHistoryEntries.value = Defaults.MAX_HISTORY_ENTRIES
            _customUnits.value = Defaults.CUSTOM_UNITS
            _settingsState.value = SettingsState()
        }
    }

    override fun onCleared() {
        prefs.unregisterOnSharedPreferenceChangeListener(listener)
        super.onCleared()
    }
}
