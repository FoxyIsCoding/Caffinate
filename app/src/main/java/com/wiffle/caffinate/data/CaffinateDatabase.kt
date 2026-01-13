package com.wiffle.caffinate.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope


@Database(
    entities = [Drink::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class CaffinateDatabase : RoomDatabase() {

    abstract fun drinkDao(): DrinkDao

    companion object {
        @Volatile
        private var INSTANCE: CaffinateDatabase? = null
        private const val TAG = "CaffinateDatabase"

        fun getDatabase(context: Context, scope: CoroutineScope? = null): CaffinateDatabase {
            return INSTANCE ?: synchronized(this) {
                val backupManager = DatabaseBackupManager(context.applicationContext)
                val dbFile = context.applicationContext.getDatabasePath("caffinate_database")
                if (dbFile.exists()) {
                    Log.i(TAG, "Creating automatic backup before migration...")
                    backupManager.createBackup()
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CaffinateDatabase::class.java,
                    "caffinate_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            Log.w(TAG, "Destructive migration occurred! Previous data was backed up.")
                            Log.i(TAG, "You can restore from backup using DatabaseBackupManager")
                        }

                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.i(TAG, "Database created for the first time")
                        }

                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d(TAG, "Database opened")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        fun getBackupManager(context: Context): DatabaseBackupManager {
            return DatabaseBackupManager(context.applicationContext)
        }

        fun closeDatabase() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
                Log.i(TAG, "Database closed")
            }
        }
    }
}
