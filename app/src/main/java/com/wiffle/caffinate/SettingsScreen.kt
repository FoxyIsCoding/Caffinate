package com.wiffle.caffinate

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wiffle.caffinate.data.DrinkViewModel
import com.wiffle.caffinate.data.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToBackup: () -> Unit = {},
    viewModel: DrinkViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity
    val scope = rememberCoroutineScope()

    var showAboutDialog by remember { mutableStateOf(false) }
    var showReminderDialog by remember { mutableStateOf(false) }
    var showUnitsDialog by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }

    val isDarkMode by settingsViewModel.isDarkMode.collectAsState()
    val notificationsEnabled by settingsViewModel.notificationsEnabled.collectAsState()
    val dailyGoal by settingsViewModel.dailyGoalMg.collectAsState()
    val maxPerDrink by settingsViewModel.maxCaffeinePerDrinkMg.collectAsState()
    val maxDaily by settingsViewModel.maxDailyCaffeineMg.collectAsState()
    val autoBackupEnabled by settingsViewModel.autoBackupEnabled.collectAsState()
    val useExpressiveTheme by settingsViewModel.useExpressiveTheme.collectAsState()
    val maxHistoryEntries by settingsViewModel.maxHistoryEntries.collectAsState()
    val reminderTime by settingsViewModel.reminderTime.collectAsState()
    val customUnits by settingsViewModel.customUnits.collectAsState()

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "Settings",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Rounded.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionHeader(icon = Icons.Rounded.Palette, title = "Appearance")
            }

            item {
                GroupedSettingsCard {
                    SettingsSwitchRow(
                        icon = Icons.Rounded.DarkMode,
                        title = "Dark Mode",
                        subtitle = "Use dark theme",
                        checked = isDarkMode,
                        onCheckedChange = { settingsViewModel.setDarkMode(it) },
                        showDivider = true
                    )

                    SettingsSwitchRow(
                        icon = Icons.Rounded.ColorLens,
                        title = "Expressive Theme",
                        subtitle = "Use Material 3 expressive surfaces",
                        checked = useExpressiveTheme,
                        onCheckedChange = { settingsViewModel.setUseExpressiveTheme(it) },
                        showDivider = false
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(icon = Icons.Rounded.Notifications, title = "Notifications")
            }

            item {
                GroupedSettingsCard {
                    SettingsSwitchRow(
                        icon = Icons.Rounded.NotificationsActive,
                        title = "Daily Reminders",
                        subtitle = "Get reminded to track your drinks",
                        checked = notificationsEnabled,
                        onCheckedChange = { settingsViewModel.setNotificationsEnabled(it) },
                        showDivider = true
                    )

                    SettingsActionRow(
                        icon = Icons.Rounded.Schedule,
                        title = "Reminder Time",
                        subtitle = "Current: $reminderTime",
                        onClick = { showReminderDialog = true },
                        showDivider = false
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(icon = Icons.Rounded.EmojiEvents, title = "Goals & Limits")
            }

            item {
                GroupedSettingsCard {
                    DailyGoalCard(
                        currentGoal = dailyGoal,
                        onGoalChange = { settingsViewModel.setDailyGoalMg(it) }
                    )

                    Spacer(Modifier.height(8.dp))

                    NumericSettingRow(
                        icon = Icons.Rounded.LocalCafe,
                        title = "Max per Drink",
                        subtitle = "$maxPerDrink ${customUnits}",
                        value = maxPerDrink,
                        onDecrease = { settingsViewModel.setMaxCaffeinePerDrinkMg((maxPerDrink - 10).coerceAtLeast(0)) },
                        onIncrease = { settingsViewModel.setMaxCaffeinePerDrinkMg(maxPerDrink + 10) }
                    )

                    NumericSettingRow(
                        icon = Icons.Rounded.TrendingUp,
                        title = "Max Daily Caffeine",
                        subtitle = "$maxDaily ${customUnits}",
                        value = maxDaily,
                        onDecrease = { settingsViewModel.setMaxDailyCaffeineMg((maxDaily - 50).coerceAtLeast(0)) },
                        onIncrease = { settingsViewModel.setMaxDailyCaffeineMg(maxDaily + 50) }
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(icon = Icons.Rounded.Storage, title = "Database & Backup")
            }

            item {
                GroupedSettingsCard {
                    SettingsActionRow(
                        icon = Icons.Rounded.Backup,
                        title = "Backup & Restore",
                        subtitle = "Manage database backups",
                        badge = "${viewModel.getBackupFiles().size}",
                        onClick = onNavigateToBackup,
                        showDivider = true
                    )

                    SettingsSwitchRow(
                        icon = Icons.Rounded.Autorenew,
                        title = "Auto Backup",
                        subtitle = "Create backups automatically",
                        checked = autoBackupEnabled,
                        onCheckedChange = { settingsViewModel.setAutoBackupEnabled(it) },
                        showDivider = false
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(icon = Icons.Rounded.Build, title = "Advanced")
            }

            item {
                GroupedSettingsCard {
                    SettingsActionRow(
                        icon = Icons.Rounded.History,
                        title = "Max History Entries",
                        subtitle = "$maxHistoryEntries rows",
                        onClick = {
                            // open a simple dialog to set number of entries
                            scope.launch { openMaxHistoryDialog(settingsViewModel) }
                        },
                        showDivider = true
                    )

                    SettingsActionRow(
                        icon = Icons.Rounded.Settings,
                        title = "Custom Units",
                        subtitle = customUnits,
                        onClick = { showUnitsDialog = true },
                        showDivider = true
                    )

                    SettingsActionRow(
                        icon = Icons.Rounded.Restore,
                        title = "Reset to Defaults",
                        subtitle = "Restore all settings to defaults",
                        isDestructive = true,
                        onClick = { showResetConfirm = true },
                        showDivider = false
                    )
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                SectionHeader(icon = Icons.Rounded.Info, title = "About")
            }

            item {
                GroupedSettingsCard {
                    SettingsActionRow(
                        icon = Icons.Rounded.Info,
                        title = "About Caffinate",
                        subtitle = "Version 1.0.0",
                        onClick = { showAboutDialog = true },
                        showDivider = true
                    )
                    SettingsActionRow(
                        icon = Icons.Rounded.Code,
                        title = "Open Source Licenses",
                        subtitle = "View third-party licenses",
                        onClick = {
                            Toast.makeText(context, "Licenses", Toast.LENGTH_SHORT).show()
                        },
                        showDivider = false
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

    if (showReminderDialog) {
        ReminderTimeDialog(
            initial = reminderTime,
            onCancel = { showReminderDialog = false },
            onSave = { hhmm ->
                settingsViewModel.setReminderTime(hhmm)
                showReminderDialog = false
                Toast.makeText(context, "Reminder set to $hhmm", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showUnitsDialog) {
        CustomUnitsDialog(
            initial = customUnits,
            onCancel = { showUnitsDialog = false },
            onSave = { units ->
                settingsViewModel.setCustomUnits(units)
                showUnitsDialog = false
                Toast.makeText(context, "Units set to $units", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            icon = { Icon(Icons.Rounded.Warning, null) },
            title = { Text("Reset all settings?") },
            text = { Text("This will restore all settings to their defaults. This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        settingsViewModel.resetToDefaults()
                        showResetConfirm = false
                        Toast.makeText(context, "Settings reset", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SectionHeader(
    icon: ImageVector,
    title: String
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(400)) + slideInVertically(
            animationSpec = tween(400),
            initialOffsetY = { -20 }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun GroupedSettingsCard(content: @Composable ColumnScope.() -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(50)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.92f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth().scale(scale),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    showDivider: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (checked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }

        if (showDivider) {
            Divider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badge: String? = null,
    isDestructive: Boolean = false,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(if (isDestructive) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isDestructive) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (badge != null) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text(
                        badge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.width(8.dp))
            }

            Icon(
                Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        if (showDivider) {
            Divider(
                modifier = Modifier.padding(horizontal = 20.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun NumericSettingRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    value: Int,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onDecrease) {
            Icon(Icons.Rounded.Remove, null)
        }
        Text(
            "$value",
            modifier = Modifier.padding(horizontal = 6.dp),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Black
        )
        IconButton(onClick = onIncrease) {
            Icon(Icons.Rounded.Add, null)
        }
    }
    Divider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

@Composable
fun DailyGoalCard(currentGoal: Int, onGoalChange: (Int) -> Unit) {
    var sliderValue by remember { mutableStateOf(currentGoal.toFloat()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Daily Caffeine Goal",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        "Set your target intake",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }

                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primary) {
                    Text(
                        "${sliderValue.toInt()}mg",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Slider(
                value = sliderValue,
                onValueChange = { sliderValue = it },
                onValueChangeFinished = { onGoalChange(sliderValue.toInt()) },
                valueRange = 100f..2000f,
                steps = 19,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "100mg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
                Text(
                    "2000mg",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun ReminderTimeDialog(initial: String, onCancel: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    Dialog(onDismissRequest = onCancel) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(0.9f)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Set Reminder Time", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Enter time in 24-hour format HH:mm",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    placeholder = { Text("09:00") })
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = {
                        val normalized = if (text.matches(Regex("^\\d{1,2}:\\d{2}$"))) {
                            val parts = text.split(":").map { it.toIntOrNull() ?: 0 }
                            val hh = parts.getOrNull(0)?.coerceIn(0, 23) ?: 9
                            val mm = parts.getOrNull(1)?.coerceIn(0, 59) ?: 0
                            "%02d:%02d".format(hh, mm)
                        } else "09:00"
                        onSave(normalized)
                    }) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun CustomUnitsDialog(initial: String, onCancel: () -> Unit, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(initial) }
    Dialog(onDismissRequest = onCancel) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(0.9f)) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Custom Units", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "Enter a unit label to display next to values (e.g. mg)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    singleLine = true,
                    placeholder = { Text("mg") })
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onCancel) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onSave(text.trim().ifEmpty { "mg" }) }) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, icon = {
        Box(
            modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.LocalCafe,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }, title = {
        Text(
            "Caffinate",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
    }, text = {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                "Version 1.0.0",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Track your caffeine intake with style. Stay energized, stay informed!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                "Made with ❤️ and lots of ☕",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }, confirmButton = {
        Button(onClick = onDismiss) { Text("Got it!") }
    })
}

private suspend fun openMaxHistoryDialog(settingsViewModel: SettingsViewModel) {
    // Placeholder for flows that need coroutine scope when opening complex dialogs.
    // The actual UI for this is opened from SettingsScreen; to keep this top-level file self-contained
    // we leave the value-change functionality inline in the screen.
}
