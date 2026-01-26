package com.wiffle.caffinate

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.wiffle.caffinate.data.deleteAllBackups
import com.wiffle.caffinate.data.deleteBackup
import com.wiffle.caffinate.data.getBackupFiles
import com.wiffle.caffinate.data.getBackupSize
import com.wiffle.caffinate.data.backupDatabase
import com.wiffle.caffinate.data.restoreDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupManagementScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Local state for list of backups and UI flags
    var backups by remember { mutableStateOf(context.getBackupFiles()) }
    var totalSize by remember { mutableStateOf(context.getBackupSize()) }
    var showConfirmDeleteAll by remember { mutableStateOf(false) }
    var showConfirmRestoreLatest by remember { mutableStateOf(false) }
    var isPerformingOperation by remember { mutableStateOf(false) }

    fun refresh() {
        backups = context.getBackupFiles()
        totalSize = context.getBackupSize()
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Backup & Restore") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Quick actions in top bar
                    IconButton(
                        onClick = {
                            // Create a backup
                            isPerformingOperation = true
                            val success = context.backupDatabase()
                            isPerformingOperation = false
                            if (success) {
                                Toast.makeText(context, "Backup created", Toast.LENGTH_SHORT).show()
                                refresh()
                            } else {
                                Toast.makeText(context, "Failed to create backup", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = !isPerformingOperation
                    ) {
                        Icon(Icons.Rounded.Save, contentDescription = "Create Backup")
                    }

                    IconButton(
                        onClick = {
                            if (backups.isEmpty()) {
                                Toast.makeText(context, "No backups available to restore", Toast.LENGTH_SHORT).show()
                            } else {
                                showConfirmRestoreLatest = true
                            }
                        },
                        enabled = !isPerformingOperation
                    ) {
                        Icon(Icons.Rounded.Restore, contentDescription = "Restore Latest")
                    }

                    IconButton(
                        onClick = {
                            if (backups.isEmpty()) {
                                Toast.makeText(context, "No backups to delete", Toast.LENGTH_SHORT).show()
                            } else {
                                showConfirmDeleteAll = true
                            }
                        },
                        enabled = !isPerformingOperation
                    ) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Delete All Backups")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Backups", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${backups.size} saved • Total size: $totalSize",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                // Create a backup
                                isPerformingOperation = true
                                val success = context.backupDatabase()
                                isPerformingOperation = false
                                if (success) {
                                    Toast.makeText(context, "Backup created", Toast.LENGTH_SHORT).show()
                                    refresh()
                                } else {
                                    Toast.makeText(context, "Failed to create backup", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = !isPerformingOperation
                        ) {
                            Text("Create")
                        }
                    }
                }
            }

            if (backups.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text("No backups found", style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(backups) { index, backup ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(backup.formattedDate, style = MaterialTheme.typography.titleMedium)
                                    Text("${backup.sizeKB} KB", style = MaterialTheme.typography.bodySmall)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    TextButton(
                                        onClick = {
                                            // Delete this backup
                                            val deleted = try {
                                                // deleteBackup is defined as a Context extension in the project
                                                context.deleteBackup(index)
                                            } catch (e: Exception) {
                                                false
                                            }
                                            if (deleted) {
                                                Toast.makeText(context, "Backup deleted", Toast.LENGTH_SHORT).show()
                                                refresh()
                                            } else {
                                                Toast.makeText(context, "Failed to delete backup", Toast.LENGTH_SHORT)
                                                    .show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Rounded.Delete, contentDescription = "Delete")
                                        Spacer(Modifier.width(6.dp))
                                        Text("Delete")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Footer actions
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { refresh() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Refresh")
                }

                Button(
                    onClick = {
                        if (backups.isEmpty()) {
                            Toast.makeText(context, "No backups to restore", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        showConfirmRestoreLatest = true
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors()
                ) {
                    Text("Restore Latest")
                }
            }
        }
    }

    // Confirm delete all dialog
    if (showConfirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteAll = false },
            title = { Text("Delete all backups?") },
            text = { Text("This action will permanently remove all saved backups. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    isPerformingOperation = true
                    val success = try {
                        context.deleteAllBackups()
                    } catch (e: Exception) {
                        false
                    }
                    isPerformingOperation = false
                    if (success) {
                        Toast.makeText(context, "All backups deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to delete backups", Toast.LENGTH_SHORT).show()
                    }
                    showConfirmDeleteAll = false
                    refresh()
                }) { Text("Delete all") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteAll = false }) { Text("Cancel") }
            }
        )
    }

    // Confirm restore latest dialog
    if (showConfirmRestoreLatest) {
        AlertDialog(
            onDismissRequest = { showConfirmRestoreLatest = false },
            title = { Text("Restore latest backup?") },
            text = { Text("Restoring will overwrite the current database. The app may need to be restarted after restore.") },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmRestoreLatest = false
                    isPerformingOperation = true
                    scope.launch {
                        // restoreDatabase is a suspend extension defined in the project; it calls back with success boolean.
                        scope.restoreDatabase(context) { success ->
                            isPerformingOperation = false
                            if (success) {
                                Toast.makeText(
                                    context,
                                    "Restore successful. Please restart the app.",
                                    Toast.LENGTH_LONG
                                ).show()
                                // If running in an Activity we can ask it to recreate() to pick up restored DB,
                                // but that may not always be desired (leave it to the user).
                                (context as? ComponentActivity)?.let { activity ->
                                    // do not call recreate automatically; just inform user
                                }
                                refresh()
                            } else {
                                Toast.makeText(context, "Restore failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmRestoreLatest = false }) { Text("Cancel") }
            }
        )
    }
}
