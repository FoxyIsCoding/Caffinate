package com.wiffle.caffinate.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar

class DrinkRepository(private val drinkDao: DrinkDao) {

    fun getAllDrinks(): Flow<List<Drink>> = drinkDao.getAllDrinks()

    fun getRecentDrinks(limit: Int = 10): Flow<List<Drink>> = drinkDao.getRecentDrinks(limit)

    suspend fun getDrinkById(drinkId: Long): Drink? = drinkDao.getDrinkById(drinkId)

    fun getFavoriteDrinks(): Flow<List<Drink>> = drinkDao.getFavoriteDrinks()

    fun getTotalDrinksCount(): Flow<Int> = drinkDao.getTotalDrinksCount()

    fun getFavoritesCount(): Flow<Int> = drinkDao.getFavoritesCount()

    fun getTodaysCaffeineIntake(): Flow<Int> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis

        return getAllDrinks().map { drinks ->
            drinks.sumOf { drink ->
                val consumptionsToday = drink.consumptionDates.count { timestamp ->
                    timestamp in startOfDay until endOfDay
                }
                drink.caffeineContent * consumptionsToday
            }
        }
    }


    fun getTodaysDrinkCount(): Flow<Int> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis

        return getAllDrinks().map { drinks ->
            drinks.sumOf { drink ->
                drink.consumptionDates.count { timestamp ->
                    timestamp in startOfDay until endOfDay
                }
            }
        }
    }


    fun getAverageDailyCaffeine(): Flow<Float?> {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        return drinkDao.getAverageDailyCaffeine(thirtyDaysAgo)
    }


    fun getCurrentStreak(): Flow<Int> {
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        return drinkDao.getCurrentStreak(thirtyDaysAgo)
    }


    fun getDrinksInRange(startDate: Long, endDate: Long): Flow<List<Drink>> =
        drinkDao.getDrinksInRange(startDate, endDate)


    fun searchDrinks(query: String): Flow<List<Drink>> = drinkDao.searchDrinks(query)


    suspend fun insertDrink(drink: Drink): Long = drinkDao.insertDrink(drink)


    suspend fun insertDrinks(drinks: List<Drink>) = drinkDao.insertDrinks(drinks)


    suspend fun updateDrink(drink: Drink) = drinkDao.updateDrink(drink)


    suspend fun deleteDrink(drink: Drink) = drinkDao.deleteDrink(drink)


    suspend fun deleteDrinkById(drinkId: Long) = drinkDao.deleteDrinkById(drinkId)


    suspend fun deleteAllDrinks() = drinkDao.deleteAllDrinks()


    suspend fun toggleFavorite(drinkId: Long) {
        val drink = getDrinkById(drinkId)
        drink?.let {
            updateDrink(it.copy(isFavorite = !it.isFavorite, updatedAt = System.currentTimeMillis()))
        }
    }


    suspend fun incrementTimesConsumed(drinkId: Long) {
        val originalDrink = getDrinkById(drinkId)

        originalDrink?.let { drink ->
            val updatedConsumptionDates = drink.consumptionDates.toMutableList().apply {
                add(System.currentTimeMillis())
            }

            val updatedDrink = drink.copy(
                consumptionDates = updatedConsumptionDates,
                timesConsumed = drink.timesConsumed + 1,
                consumedDate = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            updateDrink(updatedDrink)
        }
    }
}
