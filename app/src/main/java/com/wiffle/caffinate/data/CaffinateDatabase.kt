package com.wiffle.caffinate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Drink::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CaffinateDatabase : RoomDatabase() {

    abstract fun drinkDao(): DrinkDao

    companion object {
        @Volatile
        private var INSTANCE: CaffinateDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope? = null): CaffinateDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CaffinateDatabase::class.java,
                    "caffinate_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope?
        ) : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope?.launch(Dispatchers.IO) {
                        populateDatabase(database.drinkDao())
                    }
                }
            }

            suspend fun populateDatabase(drinkDao: DrinkDao) {
                drinkDao.deleteAllDrinks()

                val sampleDrinks = listOf(
                    Drink(
                        name = "Mango Loco",
                        brand = "Monster Energy",
                        category = "Energy Drink",
                        caffeineContent = 160,
                        sugarContent = 54,
                        size = "16 fl oz (473ml)",
                        sizeInMl = 473,
                        location = "7-Eleven, Austin TX",
                        rating = 4.5f,
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCv7Jd6t5ydResPgGg_B8Jkor0y9aZwC1_G9r47fzzgisqEewJ634jpG1UVHHVskcWyEPT0jNJioM7EYSyNeP8UukEH21-x62PnAmLB_XLkNhOgNfx_-mzBULbLk6YmgSbNIcspC4-eWj4ooiWqSu2mDgv5q_EmqzJpQCVb5DuyDy_s7vPWSfYPK4ldRUqL1zboaW424ExMy1CONWcMTjJ8GmHzxdBZ_zzsAB4ma0U1XDco0iRICKHjO5ubInrXrQR0B-EFnAslOSVX",
                        consumedDate = System.currentTimeMillis() - (1000 * 60 * 60 * 3),
                        notes = "Juicy and refreshing! Perfect for afternoon energy boost.",
                        tags = listOf("Sweet", "Tropical", "Carbonated"),
                        isFavorite = true,
                        calories = 210,
                        timesConsumed = 2
                    ),
                    Drink(
                        name = "Original Green",
                        brand = "Monster Energy",
                        category = "Energy Drink",
                        caffeineContent = 160,
                        sugarContent = 54,
                        size = "16 fl oz (473ml)",
                        sizeInMl = 473,
                        location = "7-Eleven, Austin TX",
                        rating = 4.5f,
                        imageUrl = "https://lh3.googleusercontent.com/aida-public/AB6AXuCv7Jd6t5ydResPgGg_B8Jkor0y9aZwC1_G9r47fzzgisqEewJ634jpG1UVHHVskcWyEPT0jNJioM7EYSyNeP8UukEH21-x62PnAmLB_XLkNhOgNfx_-mzBULbLk6YmgSbNIcspC4-eWj4ooiWqSu2mDgv5q_EmqzJpQCVb5DuyDy_s7vPWSfYPK4ldRUqL1zboaW424ExMy1CONWcMTjJ8GmHzxdBZ_zzsAB4ma0U1XDco0iRICKHjO5ubInrXrQR0B-EFnAslOSVX",
                        consumedDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24),
                        notes = "Found this at the gas station on Route 66. It's the classic flavor that started it all. A bit syrupy compared to the Ultra line, but hits the spot when you need a real kick.",
                        tags = listOf("Sweet", "Citrus", "Carbonated"),
                        isFavorite = false,
                        calories = 210,
                        timesConsumed = 1
                    ),
                    Drink(
                        name = "Ultra White",
                        brand = "Monster Energy",
                        category = "Energy Drink",
                        caffeineContent = 150,
                        sugarContent = 0,
                        size = "16 fl oz (473ml)",
                        sizeInMl = 473,
                        location = "Target, Downtown",
                        rating = 4.8f,
                        imageUrl = "",
                        consumedDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 2),
                        notes = "Zero sugar and still tastes great! Light citrus flavor.",
                        tags = listOf("Zero Sugar", "Citrus", "Light"),
                        isFavorite = true,
                        calories = 10,
                        timesConsumed = 3
                    ),
                    Drink(
                        name = "Pipeline Punch",
                        brand = "Monster Energy",
                        category = "Energy Drink",
                        caffeineContent = 160,
                        sugarContent = 54,
                        size = "16 fl oz (473ml)",
                        sizeInMl = 473,
                        location = "Gas Station",
                        rating = 4.7f,
                        imageUrl = "",
                        consumedDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 3),
                        notes = "Passion fruit and tropical blend. Best Monster flavor!",
                        tags = listOf("Sweet", "Tropical", "Passion Fruit"),
                        isFavorite = true,
                        calories = 210,
                        timesConsumed = 1
                    ),
                    Drink(
                        name = "Java Monster Mean Bean",
                        brand = "Monster Energy",
                        category = "Coffee Energy Drink",
                        caffeineContent = 188,
                        sugarContent = 29,
                        size = "15 fl oz (443ml)",
                        sizeInMl = 443,
                        location = "Convenience Store",
                        rating = 4.2f,
                        imageUrl = "",
                        consumedDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 4),
                        notes = "Coffee and energy combined. Good for mornings.",
                        tags = listOf("Coffee", "Sweet", "Creamy"),
                        isFavorite = false,
                        calories = 210,
                        timesConsumed = 1
                    ),
                    Drink(
                        name = "Reign Orange Dreamsicle",
                        brand = "Reign",
                        category = "Performance Energy Drink",
                        caffeineContent = 300,
                        sugarContent = 0,
                        size = "16 fl oz (473ml)",
                        sizeInMl = 473,
                        location = "GNC",
                        rating = 4.6f,
                        imageUrl = "",
                        consumedDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 5),
                        notes = "High caffeine! Tastes like creamsicle. Perfect pre-workout.",
                        tags = listOf("Zero Sugar", "High Caffeine", "Orange"),
                        isFavorite = true,
                        calories = 10,
                        timesConsumed = 4
                    ),
                    Drink(
                        name = "Red Bull Original",
                        brand = "Red Bull",
                        category = "Energy Drink",
                        caffeineContent = 80,
                        sugarContent = 27,
                        size = "8.4 fl oz (250ml)",
                        sizeInMl = 250,
                        location = "Grocery Store",
                        rating = 4.0f,
                        imageUrl = "",
                        consumedDate = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 6),
                        notes = "Classic energy drink. Smaller size is perfect for quick boost.",
                        tags = listOf("Classic", "Sweet", "Carbonated"),
                        isFavorite = false,
                        calories = 110,
                        timesConsumed = 1
                    )
                )

                drinkDao.insertDrinks(sampleDrinks)
            }
        }
    }
}
