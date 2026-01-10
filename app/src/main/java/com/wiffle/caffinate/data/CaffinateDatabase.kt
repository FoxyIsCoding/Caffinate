package com.wiffle.caffinate.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope


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
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }


    }
}
