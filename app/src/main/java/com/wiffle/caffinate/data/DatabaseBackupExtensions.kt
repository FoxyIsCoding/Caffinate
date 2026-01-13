package com.wiffle.caffinate.data

import android.content.Context
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "DatabaseBackup"

fun Context.backupDatabase(): Boolean {
    val success = DatabaseTestHelper.createBackup(this)
    if (success) {
        Log.i(TAG, "✅ Backup created successfully")
    } else {
        Log.e(TAG, "❌ Failed to create backup")
    }
    return success
}

fun Context.getBackupFiles(): List<DatabaseTestHelper.BackupInfo> {
    return DatabaseTestHelper.listBackups(this)
}

fun Context.getBackupSize(): String {
    return DatabaseTestHelper.getBackupSize(this)
}

fun Context.deleteAllBackups(): Boolean {
    return DatabaseTestHelper.deleteAllBackups(this)
}

fun Context.logBackupInfo() {
    val backups = getBackupFiles()
    val size = getBackupSize()

    Log.i(TAG, "═══════════════════════════════════")
    Log.i(TAG, "📊 Database Backup Info:")
    Log.i(TAG, "   Total backups: ${backups.size}")
    Log.i(TAG, "   Total size: $size")
    Log.i(TAG, "───────────────────────────────────")

    if (backups.isEmpty()) {
        Log.i(TAG, "   No backups found")
    } else {
        backups.forEachIndexed { index, backup ->
            Log.i(TAG, "   ${index + 1}. ${backup.formattedDate}")
            Log.i(TAG, "      Size: ${backup.sizeKB} KB")
        }
    }
    Log.i(TAG, "═══════════════════════════════════")
}

suspend fun CoroutineScope.restoreDatabase(
    context: Context,
    onComplete: (Boolean) -> Unit
) {
    DatabaseTestHelper.restoreLatestBackup(context, onComplete)
}

suspend fun CoroutineScope.restoreDatabaseWith(
    context: Context,
    onSuccess: () -> Unit,
    onFailure: () -> Unit = {}
) {
    DatabaseTestHelper.restoreLatestBackup(context) { success ->
        if (success) {
            onSuccess()
        } else {
            onFailure()
        }
    }
}

fun ComponentActivity.backupDatabase(): Boolean {
    return (this as Context).backupDatabase()
}

fun ComponentActivity.restoreDatabaseAndRestart(
    scope: CoroutineScope,
    onFailure: () -> Unit = {}
) {
    scope.launch {
        restoreDatabaseWith(
            context = this@restoreDatabaseAndRestart,
            onSuccess = { recreate() },
            onFailure = onFailure
        )
    }
}

fun Fragment.backupDatabase(): Boolean {
    return requireContext().backupDatabase()
}

fun Fragment.getBackupFiles(): List<DatabaseTestHelper.BackupInfo> {
    return requireContext().getBackupFiles()
}

fun Fragment.restoreDatabase(
    scope: CoroutineScope,
    onComplete: (Boolean) -> Unit
) {
    scope.launch {
        restoreDatabase(requireContext(), onComplete)
    }
}

fun DatabaseTestHelper.BackupInfo.toReadableString(): String {
    return "$formattedDate (${sizeKB} KB)"
}

fun Context.hasBackups(): Boolean {
    return getBackupFiles().isNotEmpty()
}

fun Context.getLatestBackup(): DatabaseTestHelper.BackupInfo? {
    return getBackupFiles().firstOrNull()
}

fun Context.deleteBackup(index: Int): Boolean {
    val backups = getBackupFiles()
    return if (index in backups.indices) {
        DatabaseTestHelper.deleteBackup(this, backups[index])
    } else {
        false
    }
}

fun Context.quickBackup() {
    Log.d(TAG, "🔄 Creating backup...")
    val success = backupDatabase()
    if (success) {
        logBackupInfo()
    }
}

suspend fun CoroutineScope.quickRestore(context: Context) {
    Log.d(TAG, "🔄 Restoring from backup...")
    restoreDatabase(context) { success ->
        if (success) {
            Log.i(TAG, "✅ Restore successful! Don't forget to recreate() or restart app")
        } else {
            Log.e(TAG, "❌ Restore failed - no backups found or error occurred")
        }
    }
}
