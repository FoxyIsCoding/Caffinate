package com.wiffle.caffinate.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseTestHelper {

    private const val TAG = "DatabaseTestHelper"

    data class BackupInfo(
        val name: String,
        val formattedDate: String,
        val sizeKB: Long,
        val file: java.io.File
    )

    fun createBackup(context: Context): Boolean {
        return try {
            val backupManager = DatabaseBackupManager(context.applicationContext)
            val success = backupManager.createBackup()

            if (success) {
                Log.i(TAG, "✅ Backup created successfully")
                Log.i(TAG, "Total backups: ${backupManager.getBackupFiles().size}")
                Log.i(TAG, "Total size: ${backupManager.getFormattedBackupSize()}")
            } else {
                Log.e(TAG, "❌ Failed to create backup")
            }

            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error creating backup", e)
            false
        }
    }

    suspend fun restoreLatestBackup(context: Context, onComplete: (Boolean) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val backupManager = DatabaseBackupManager(context.applicationContext)
                val backups = backupManager.getBackupFiles()

                if (backups.isEmpty()) {
                    Log.w(TAG, "⚠️ No backups found to restore")
                    withContext(Dispatchers.Main) {
                        onComplete(false)
                    }
                    return@withContext
                }

                Log.i(TAG, "🔄 Closing database...")
                CaffinateDatabase.closeDatabase()

                Log.i(TAG, "🔄 Restoring from ${backups.first().name}...")
                val success = backupManager.restoreMostRecentBackup()

                if (success) {
                    Log.i(TAG, "✅ Database restored successfully!")
                } else {
                    Log.e(TAG, "❌ Failed to restore database")
                }

                withContext(Dispatchers.Main) {
                    onComplete(success)
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error restoring backup", e)
                withContext(Dispatchers.Main) {
                    onComplete(false)
                }
            }
        }
    }

    fun listBackups(context: Context): List<BackupInfo> {
        return try {
            val backupManager = DatabaseBackupManager(context.applicationContext)
            val backups = backupManager.getBackupFiles()

            Log.i(TAG, "📋 Found ${backups.size} backup(s):")

            backups.map { file ->
                val date = extractDateFromBackupName(file.name)
                val sizeKB = file.length() / 1024

                Log.i(TAG, "  • ${file.name}")
                Log.i(TAG, "    Date: $date")
                Log.i(TAG, "    Size: $sizeKB KB")

                BackupInfo(
                    name = file.name,
                    formattedDate = date,
                    sizeKB = sizeKB,
                    file = file
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error listing backups", e)
            emptyList()
        }
    }

    fun deleteBackup(context: Context, backupInfo: BackupInfo): Boolean {
        return try {
            val deleted = backupInfo.file.delete()
            if (deleted) {
                Log.i(TAG, "🗑️ Deleted backup: ${backupInfo.name}")
            } else {
                Log.e(TAG, "❌ Failed to delete backup: ${backupInfo.name}")
            }
            deleted
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting backup", e)
            false
        }
    }

    fun deleteAllBackups(context: Context): Boolean {
        return try {
            val backupManager = DatabaseBackupManager(context.applicationContext)
            val success = backupManager.deleteAllBackups()
            if (success) {
                Log.i(TAG, "🗑️ All backups deleted")
            } else {
                Log.e(TAG, "❌ Failed to delete all backups")
            }
            success
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error deleting all backups", e)
            false
        }
    }

    fun getBackupSize(context: Context): String {
        return try {
            val backupManager = DatabaseBackupManager(context.applicationContext)
            backupManager.getFormattedBackupSize()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error getting backup size", e)
            "Unknown"
        }
    }

    private fun extractDateFromBackupName(filename: String): String {
        return try {
            val parts = filename.split("_backup_")
            if (parts.size == 2) {
                parts[1].replace(".db", "").replace("_", " ").replace("-", ":")
            } else {
                "Unknown"
            }
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
