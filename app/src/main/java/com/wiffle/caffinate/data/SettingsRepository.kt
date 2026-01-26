package com.wiffle.caffinate.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

data class Settings(
    val darkModeEnabled: Boolean = Defaults.DARK_MODE,
    val notificationsEnabled: Boolean = Defaults.NOTIFICATIONS_ENABLED,
    val dailyGoalMg: Int = Defaults.DAILY_GOAL_MG,
    val maxCaffeinePerDrinkMg: Int = Defaults.MAX_PER_DRINK_MG,
    val reminderHour: Int = Defaults.REMINDER_HOUR,
    val reminderMinute: Int = Defaults.REMINDER_MINUTE,
    val autoBackupEnabled: Boolean = Defaults.AUTO_BACKUP,
    val maxHistoryEntries: Int = Defaults.MAX_HISTORY_ENTRIES,
    val useExpressiveTheme: Boolean = Defaults.USE_EXPRESSIVE_THEME,
    val customUnits: String = Defaults.CUSTOM_UNITS
)

private object Keys {
    const val DARK_MODE = "settings_dark_mode"
    const val NOTIFICATIONS = "settings_notifications"
    const val DAILY_GOAL_MG = "settings_daily_goal_mg"
    const val MAX_PER_DRINK_MG = "settings_max_per_drink_mg"
    const val REMINDER_HOUR = "settings_reminder_hour"
    const val REMINDER_MINUTE = "settings_reminder_minute"
    const val AUTO_BACKUP = "settings_auto_backup"
    const val MAX_HISTORY_ENTRIES = "settings_max_history_entries"
    const val USE_EXPRESSIVE_THEME = "settings_use_expressive_theme"
    const val CUSTOM_UNITS = "settings_custom_units"
}

private object Defaults {
    const val DARK_MODE = true
    const val NOTIFICATIONS_ENABLED = true
    const val DAILY_GOAL_MG = 400
    const val MAX_PER_DRINK_MG = 200
    const val REMINDER_HOUR = 9
    const val REMINDER_MINUTE = 0
    const val AUTO_BACKUP = false
    const val MAX_HISTORY_ENTRIES = 1000
    const val USE_EXPRESSIVE_THEME = true
    const val CUSTOM_UNITS = "mg"
}

class SettingsRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("caffinate_settings", Context.MODE_PRIVATE)

    fun getSettings(): Settings {
        return Settings(
            darkModeEnabled = prefs.getBoolean(Keys.DARK_MODE, Defaults.DARK_MODE),
            notificationsEnabled = prefs.getBoolean(Keys.NOTIFICATIONS, Defaults.NOTIFICATIONS_ENABLED),
            dailyGoalMg = prefs.getInt(Keys.DAILY_GOAL_MG, Defaults.DAILY_GOAL_MG),
            maxCaffeinePerDrinkMg = prefs.getInt(Keys.MAX_PER_DRINK_MG, Defaults.MAX_PER_DRINK_MG),
            reminderHour = prefs.getInt(Keys.REMINDER_HOUR, Defaults.REMINDER_HOUR),
            reminderMinute = prefs.getInt(Keys.REMINDER_MINUTE, Defaults.REMINDER_MINUTE),
            autoBackupEnabled = prefs.getBoolean(Keys.AUTO_BACKUP, Defaults.AUTO_BACKUP),
            maxHistoryEntries = prefs.getInt(Keys.MAX_HISTORY_ENTRIES, Defaults.MAX_HISTORY_ENTRIES),
            useExpressiveTheme = prefs.getBoolean(Keys.USE_EXPRESSIVE_THEME, Defaults.USE_EXPRESSIVE_THEME),
            customUnits = prefs.getString(Keys.CUSTOM_UNITS, Defaults.CUSTOM_UNITS) ?: Defaults.CUSTOM_UNITS
        )
    }

    fun settingsFlow(): Flow<Settings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(getSettings())
        }
        trySend(getSettings())
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    val darkModeFlow: Flow<Boolean> = settingsFlow().map { it.darkModeEnabled }.distinctUntilChanged()
    val notificationsFlow: Flow<Boolean> = settingsFlow().map { it.notificationsEnabled }.distinctUntilChanged()
    val dailyGoalFlow: Flow<Int> = settingsFlow().map { it.dailyGoalMg }.distinctUntilChanged()
    val maxPerDrinkFlow: Flow<Int> = settingsFlow().map { it.maxCaffeinePerDrinkMg }.distinctUntilChanged()
    val reminderHourFlow: Flow<Int> = settingsFlow().map { it.reminderHour }.distinctUntilChanged()
    val reminderMinuteFlow: Flow<Int> = settingsFlow().map { it.reminderMinute }.distinctUntilChanged()
    val autoBackupFlow: Flow<Boolean> = settingsFlow().map { it.autoBackupEnabled }.distinctUntilChanged()
    val maxHistoryEntriesFlow: Flow<Int> = settingsFlow().map { it.maxHistoryEntries }.distinctUntilChanged()
    val useExpressiveThemeFlow: Flow<Boolean> = settingsFlow().map { it.useExpressiveTheme }.distinctUntilChanged()
    val customUnitsFlow: Flow<String> = settingsFlow().map { it.customUnits }.distinctUntilChanged()

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(Keys.DARK_MODE, enabled).apply()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(Keys.NOTIFICATIONS, enabled).apply()
    }

    fun setDailyGoalMg(mg: Int) {
        val value = mg.coerceIn(0, 20000)
        prefs.edit().putInt(Keys.DAILY_GOAL_MG, value).apply()
    }

    fun setMaxCaffeinePerDrinkMg(mg: Int) {
        val value = mg.coerceIn(0, 5000)
        prefs.edit().putInt(Keys.MAX_PER_DRINK_MG, value).apply()
    }

    fun setReminderTime(hour24: Int, minute: Int) {
        val h = hour24.coerceIn(0, 23)
        val m = minute.coerceIn(0, 59)
        prefs.edit().putInt(Keys.REMINDER_HOUR, h).putInt(Keys.REMINDER_MINUTE, m).apply()
    }

    fun setAutoBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(Keys.AUTO_BACKUP, enabled).apply()
    }

    fun setMaxHistoryEntries(maxEntries: Int) {
        val v = maxEntries.coerceIn(10, 100_000)
        prefs.edit().putInt(Keys.MAX_HISTORY_ENTRIES, v).apply()
    }

    fun setUseExpressiveTheme(enabled: Boolean) {
        prefs.edit().putBoolean(Keys.USE_EXPRESSIVE_THEME, enabled).apply()
    }

    fun setCustomUnits(units: String) {
        val normalized = units.trim().ifEmpty { Defaults.CUSTOM_UNITS }
        prefs.edit().putString(Keys.CUSTOM_UNITS, normalized).apply()
    }

    fun resetToDefaults() {
        prefs.edit()
            .putBoolean(Keys.DARK_MODE, Defaults.DARK_MODE)
            .putBoolean(Keys.NOTIFICATIONS, Defaults.NOTIFICATIONS_ENABLED)
            .putInt(Keys.DAILY_GOAL_MG, Defaults.DAILY_GOAL_MG)
            .putInt(Keys.MAX_PER_DRINK_MG, Defaults.MAX_PER_DRINK_MG)
            .putInt(Keys.REMINDER_HOUR, Defaults.REMINDER_HOUR)
            .putInt(Keys.REMINDER_MINUTE, Defaults.REMINDER_MINUTE)
            .putBoolean(Keys.AUTO_BACKUP, Defaults.AUTO_BACKUP)
            .putInt(Keys.MAX_HISTORY_ENTRIES, Defaults.MAX_HISTORY_ENTRIES)
            .putBoolean(Keys.USE_EXPRESSIVE_THEME, Defaults.USE_EXPRESSIVE_THEME)
            .putString(Keys.CUSTOM_UNITS, Defaults.CUSTOM_UNITS)
            .apply()
    }
}
