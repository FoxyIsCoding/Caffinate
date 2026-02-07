package com.wiffle.caffinate.data

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DatabaseBackupManager(private val context: Context) {

    companion object {
        private const val TAG = "DatabaseBackupManager"
        private const val DATABASE_NAME = "caffinate_database"
        private const val BACKUP_DIR = "database_backups"
        private const val MAX_BACKUPS = 5
    }

    private val backupDirectory: File
        get() {
            val dir = File(context.filesDir, BACKUP_DIR)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            return dir
        }

    fun createBackup(): Boolean {
        return try {
            val dbFile = context.getDatabasePath(DATABASE_NAME)

            if (!dbFile.exists()) {
                Log.w(TAG, "Database file does not exist, skipping backup")
                return false
            }

            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                .format(Date())
            val backupFile = File(backupDirectory, "${DATABASE_NAME}_backup_$timestamp.db")

            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            val walFile = File(dbFile.path + "-wal")
            val shmFile = File(dbFile.path + "-shm")

            if (walFile.exists()) {
                val walBackup = File(backupDirectory, "${DATABASE_NAME}_backup_$timestamp.db-wal")
                FileInputStream(walFile).use { input ->
                    FileOutputStream(walBackup).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            if (shmFile.exists()) {
                val shmBackup = File(backupDirectory, "${DATABASE_NAME}_backup_$timestamp.db-shm")
                FileInputStream(shmFile).use { input ->
                    FileOutputStream(shmBackup).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            Log.i(TAG, "Database backup created: ${backupFile.name}")

            cleanupOldBackups()

            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create database backup", e)
            false
        }
    }

    fun restoreMostRecentBackup(): Boolean {
        val backups = getBackupFiles()
        if (backups.isEmpty()) {
            Log.w(TAG, "No backups found to restore")
            return false
        }

        val mostRecent = backups.first()
        return restoreBackup(mostRecent)
    }

    fun restoreBackup(backupFile: File): Boolean {
        return try {
            if (!backupFile.exists()) {
                Log.e(TAG, "Backup file does not exist: ${backupFile.name}")
                return false
            }

            val dbFile = context.getDatabasePath(DATABASE_NAME)

            FileInputStream(backupFile).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }

            val timestamp = backupFile.name.replace("${DATABASE_NAME}_backup_", "").replace(".db", "")
            val walBackup = File(backupDirectory, "${DATABASE_NAME}_backup_$timestamp.db-wal")
            val shmBackup = File(backupDirectory, "${DATABASE_NAME}_backup_$timestamp.db-shm")

            if (walBackup.exists()) {
                val walFile = File(dbFile.path + "-wal")
                FileInputStream(walBackup).use { input ->
                    FileOutputStream(walFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            if (shmBackup.exists()) {
                val shmFile = File(dbFile.path + "-shm")
                FileInputStream(shmBackup).use { input ->
                    FileOutputStream(shmFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }

            Log.i(TAG, "Database restored from backup: ${backupFile.name}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to restore database from backup", e)
            false
        }
    }

    fun getBackupFiles(): List<File> {
        return backupDirectory.listFiles { file ->
            file.name.startsWith("${DATABASE_NAME}_backup_") && file.name.endsWith(".db")
        }?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    private fun cleanupOldBackups() {
        val backups = getBackupFiles()
        if (backups.size > MAX_BACKUPS) {
            backups.drop(MAX_BACKUPS).forEach { backup ->
                try {
                    backup.delete()

                    val timestamp = backup.name.replace("${DATABASE_NAME}_backup_", "").replace(".db", "")
                    File(backupDirectory, "${DATABASE_NAME}_backup_$timestamp.db-wal").delete()
                    File(backupDirectory, "${DATABASE_NAME}_backup_$timestamp.db-shm").delete()

                    Log.i(TAG, "Deleted old backup: ${backup.name}")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to delete old backup: ${backup.name}", e)
                }
            }
        }
    }

    fun deleteAllBackups(): Boolean {
        return try {
            backupDirectory.listFiles()?.forEach { it.delete() }
            Log.i(TAG, "All backups deleted")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete all backups", e)
            false
        }
    }

    fun getTotalBackupSize(): Long {
        return backupDirectory.listFiles()?.sumOf { it.length() } ?: 0L
    }

    fun getFormattedBackupSize(): String {
        val bytes = getTotalBackupSize()
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "${bytes / 1024} KB"
            else -> "${bytes / (1024 * 1024)} MB"
        }
    }

    fun exportDatabaseToExternal(): File? {
        return try {
            val dbFile = context.getDatabasePath(DATABASE_NAME)
            if (!dbFile.exists()) return null
            val exports = File(context.getExternalFilesDir(null), "exports")
            if (!exports.exists()) exports.mkdirs()
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val out = File(exports, "${DATABASE_NAME}_export_$timestamp.db")
            FileInputStream(dbFile).use { input ->
                FileOutputStream(out).use { output ->
                    input.copyTo(output)
                }
            }
            val wal = File(dbFile.path + "-wal")
            val shm = File(dbFile.path + "-shm")
            if (wal.exists()) {
                FileInputStream(wal).use { input ->
                    FileOutputStream(File(exports, out.name + "-wal")).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            if (shm.exists()) {
                FileInputStream(shm).use { input ->
                    FileOutputStream(File(exports, out.name + "-shm")).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            Log.i(TAG, "Exported DB to ${out.absolutePath}")
            out
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export database", e)
            null
        }
    }
}
