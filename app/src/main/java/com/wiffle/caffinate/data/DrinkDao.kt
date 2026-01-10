package com.wiffle.caffinate.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DrinkDao {

    @Query("SELECT * FROM drinks ORDER BY consumedDate DESC")
    fun getAllDrinks(): Flow<List<Drink>>

    @Query("SELECT * FROM drinks ORDER BY consumedDate DESC LIMIT :limit")
    fun getRecentDrinks(limit: Int = 10): Flow<List<Drink>>

    @Query("SELECT * FROM drinks WHERE id = :drinkId")
    suspend fun getDrinkById(drinkId: Long): Drink?

    @Query("SELECT * FROM drinks WHERE isFavorite = 1 ORDER BY consumedDate DESC")
    fun getFavoriteDrinks(): Flow<List<Drink>>

    @Query("SELECT COUNT(*) FROM drinks")
    fun getTotalDrinksCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM drinks WHERE isFavorite = 1")
    fun getFavoritesCount(): Flow<Int>

    @Query(
        """
        SELECT COUNT(*) FROM drinks
        WHERE consumedDate >= :startOfDay AND consumedDate < :endOfDay
    """
    )
    fun getDrinksCountForDay(startOfDay: Long, endOfDay: Long): Flow<Int>

    @Query(
        """
        SELECT SUM(caffeineContent * timesConsumed) FROM drinks
        WHERE consumedDate >= :startOfDay AND consumedDate < :endOfDay
    """
    )
    fun getTotalCaffeineForDay(startOfDay: Long, endOfDay: Long): Flow<Int?>

    @Query(
        """
        SELECT AVG(caffeineContent * timesConsumed) FROM drinks
        WHERE consumedDate >= :startDate
    """
    )
    fun getAverageDailyCaffeine(startDate: Long): Flow<Float?>

    @Query(
        """
        SELECT * FROM drinks
        WHERE consumedDate >= :startDate AND consumedDate < :endDate
        ORDER BY consumedDate DESC
    """
    )
    fun getDrinksInRange(startDate: Long, endDate: Long): Flow<List<Drink>>

    @Query(
        """
        SELECT COUNT(DISTINCT DATE(consumedDate / 1000, 'unixepoch')) as streak
        FROM drinks
        WHERE consumedDate >= :startDate
        ORDER BY consumedDate DESC
    """
    )
    fun getCurrentStreak(startDate: Long): Flow<Int>

    @Query("SELECT * FROM drinks WHERE name LIKE '%' || :query || '%' OR brand LIKE '%' || :query || '%'")
    fun searchDrinks(query: String): Flow<List<Drink>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrink(drink: Drink): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDrinks(drinks: List<Drink>)

    @Update
    suspend fun updateDrink(drink: Drink)

    @Delete
    suspend fun deleteDrink(drink: Drink)

    @Query("DELETE FROM drinks WHERE id = :drinkId")
    suspend fun deleteDrinkById(drinkId: Long)

    @Query("DELETE FROM drinks")
    suspend fun deleteAllDrinks()

    @Query("UPDATE drinks SET timesConsumed = timesConsumed + 1, updatedAt = :timestamp WHERE id = :drinkId")
    suspend fun incrementTimesConsumed(drinkId: Long, timestamp: Long = System.currentTimeMillis())
}
