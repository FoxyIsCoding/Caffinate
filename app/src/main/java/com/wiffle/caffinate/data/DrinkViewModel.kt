package com.wiffle.caffinate.data

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DrinkViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DrinkRepository
    private val backupManager: DatabaseBackupManager

    companion object {
        private const val TAG = "DrinkViewModel"
    }

    init {
        val drinkDao = CaffinateDatabase.getDatabase(application, viewModelScope).drinkDao()
        repository = DrinkRepository(drinkDao)
        backupManager = CaffinateDatabase.getBackupManager(application)
    }

    val recentDrinks: StateFlow<List<Drink>> = repository.getRecentDrinks(10)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allDrinks: StateFlow<List<Drink>> = repository.getAllDrinks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val favoriteDrinks: StateFlow<List<Drink>> = repository.getFavoriteDrinks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalDrinksCount: StateFlow<Int> = repository.getTotalDrinksCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val favoritesCount: StateFlow<Int> = repository.getFavoritesCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val todaysCaffeineIntake: StateFlow<Int> = repository.getTodaysCaffeineIntake()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
        .combine(MutableStateFlow(0)) { caffeine, _ ->
            caffeine ?: 0
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val todaysDrinkCount: StateFlow<Int> = repository.getTodaysDrinkCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val averageDailyCaffeine: StateFlow<Int> = repository.getAverageDailyCaffeine()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
        .combine(MutableStateFlow(0)) { avg, _ ->
            avg?.toInt() ?: 0
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val currentStreak: StateFlow<Int> = repository.getCurrentStreak()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    private val _selectedDrink = MutableStateFlow<Drink?>(null)
    val selectedDrink: StateFlow<Drink?> = _selectedDrink.asStateFlow()

    fun insertDrink(drink: Drink) {
        viewModelScope.launch {
            repository.insertDrink(drink)
        }
    }

    fun insertDrinks(drinks: List<Drink>) {
        viewModelScope.launch {
            repository.insertDrinks(drinks)
        }
    }

    fun updateDrink(drink: Drink) {
        viewModelScope.launch {
            repository.updateDrink(drink)
        }
    }

    fun deleteDrink(drink: Drink) {
        viewModelScope.launch {
            repository.deleteDrink(drink)
        }
    }

    fun deleteDrinkById(drinkId: Long) {
        viewModelScope.launch {
            repository.deleteDrinkById(drinkId)
        }
    }

    fun toggleFavorite(drinkId: Long, bool: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(drinkId)
        }
    }

    fun loadDrink(drinkId: Long) {
        viewModelScope.launch {
            _selectedDrink.value = repository.getDrinkById(drinkId)
        }
    }

    fun clearSelectedDrink() {
        _selectedDrink.value = null
    }

    fun searchDrinks(query: String): Flow<List<Drink>> = repository.searchDrinks(query)

    fun getDrinksInRange(startDate: Long, endDate: Long): Flow<List<Drink>> =
        repository.getDrinksInRange(startDate, endDate)

    fun incrementTimesConsumed(drinkId: Long) {
        viewModelScope.launch {
            repository.incrementTimesConsumed(drinkId)
            loadDrink(drinkId)
        }
    }

    fun createManualBackup(): Boolean {
        return try {
            val success = backupManager.createBackup()
            if (success) {
                Log.i(TAG, "Manual backup created successfully")
            } else {
                Log.w(TAG, "Failed to create manual backup")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "Error creating manual backup", e)
            false
        }
    }

    fun restoreMostRecentBackup(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                CaffinateDatabase.closeDatabase()
                val success = backupManager.restoreMostRecentBackup()

                if (success) {
                    Log.i(TAG, "Database restored successfully from most recent backup")

                    withContext(Dispatchers.Main) {
                        val drinkDao = CaffinateDatabase.getDatabase(getApplication(), viewModelScope).drinkDao()
                        Log.i(TAG, "Database reopened after restore")
                    }
                } else {
                    Log.w(TAG, "Failed to restore from backup")
                }

                withContext(Dispatchers.Main) {
                    onComplete(success)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring from backup", e)
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    fun restoreFromBackup(backupFile: File, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                CaffinateDatabase.closeDatabase()
                val success = backupManager.restoreBackup(backupFile)

                if (success) {
                    Log.i(TAG, "Database restored successfully from ${backupFile.name}")

                    withContext(Dispatchers.Main) {
                        CaffinateDatabase.getDatabase(getApplication(), viewModelScope)
                        Log.i(TAG, "Database reopened after restore")
                    }
                } else {
                    Log.w(TAG, "Failed to restore from ${backupFile.name}")
                }

                withContext(Dispatchers.Main) {
                    onComplete(success)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring from backup file", e)
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    fun getBackupFiles(): List<File> {
        return backupManager.getBackupFiles()
    }

    fun getBackupSize(): String {
        return backupManager.getFormattedBackupSize()
    }

    fun deleteAllBackups(): Boolean {
        return backupManager.deleteAllBackups()
    }
}
