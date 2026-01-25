package com.wiffle.caffinate

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wiffle.caffinate.data.DrinkViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatsScreen(onBackClick: () -> Unit = {}) {
    val viewModel: DrinkViewModel = viewModel()

    val totalDrinks by viewModel.totalDrinksCount.collectAsState()
    val favorites by viewModel.favoritesCount.collectAsState()
    val todayCaffeine by viewModel.todaysCaffeineIntake.collectAsState()
    val avgDailyCaffeine by viewModel.averageDailyCaffeine.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()
    val recentDrinks by viewModel.recentDrinks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        StatsTopBar()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 80.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                StatsHeaderCard(totalDrinks, todayCaffeine)

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HealthStatusCard(
                        title = "Streak",
                        value = streak.toString(),
                        icon = Icons.Rounded.TrendingUp,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.primary
                    )

                    HealthStatusCard(
                        title = "Favorites",
                        value = favorites.toString(),
                        icon = Icons.Rounded.WaterDrop,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HealthStatusCard(
                        title = "Avg Daily",
                        value = "$avgDailyCaffeine mg",
                        icon = Icons.Rounded.LocalFireDepartment,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    HealthStatusCard(
                        title = "Today",
                        value = "$todayCaffeine mg",
                        icon = Icons.Rounded.BarChart,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Health Status",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                HealthIndicatorCard(
                    label = "Caffeine Level",
                    current = todayCaffeine,
                    max = 400,
                    unit = "mg",
                    status = when {
                        todayCaffeine > 500 -> "High"
                        todayCaffeine > 300 -> "Moderate"
                        else -> "Good"
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                HealthIndicatorCard(
                    label = "Daily Consumption",
                    current = recentDrinks.count { System.currentTimeMillis() - it.consumedDate < 86400000 },
                    max = 5,
                    unit = "drinks",
                    status = "On Track"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Cool Stats",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                val avgCaffeinePerDrink =
                    if (recentDrinks.isNotEmpty()) (recentDrinks.sumOf { it.caffeineContent } / recentDrinks.size) else 0

                CoolStatCard(
                    title = "Most Consumed",
                    value = recentDrinks.groupingBy { it.name }
                        .eachCount()
                        .maxByOrNull { it.value }
                        ?.key ?: "N/A",
                    icon = Icons.Rounded.BarChart
                )

                Spacer(modifier = Modifier.height(12.dp))

                CoolStatCard(
                    title = "Avg Per Drink",
                    value = "$avgCaffeinePerDrink mg",
                    icon = Icons.Rounded.LocalFireDepartment
                )

                Spacer(modifier = Modifier.height(12.dp))

                CoolStatCard(
                    title = "Total Caffeine",
                    value = "${recentDrinks.sumOf { it.caffeineContent }} mg",
                    icon = Icons.Rounded.TrendingUp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsTopBar() {
    TopAppBar(
        title = {
            Text(
                "Statistics",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun StatsHeaderCard(totalDrinks: Int, todayCaffeine: Int) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Total Drinks",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        totalDrinks.toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.BarChart,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                )
            }

            Text(
                "Today: $todayCaffeine mg of caffeine",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun HealthStatusCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = tween(100),
        label = "cardScale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable(enabled = true) {
                isPressed = !isPressed
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.15f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = color
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                value,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HealthIndicatorCard(
    label: String,
    current: Int,
    max: Int,
    unit: String,
    status: String
) {
    val progress = (current.toFloat() / max.toFloat()).coerceIn(0f, 1.5f)
    val progressColor = when {
        current > max * 1.25f -> MaterialTheme.colorScheme.error
        current > max -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "$current / $max $unit",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }

                Text(
                    status,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = progressColor
                )
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                drawStopIndicator = {}
            )
        }
    }
}

@Composable
fun CoolStatCard(
    title: String,
    value: String,
    icon: ImageVector
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(100),
        label = "statCardScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { isPressed = !isPressed },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    value,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}
