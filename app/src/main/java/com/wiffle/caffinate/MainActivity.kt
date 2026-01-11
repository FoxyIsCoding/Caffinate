package com.wiffle.caffinate

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import com.wiffle.caffinate.data.Drink
import com.wiffle.caffinate.data.DrinkViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MonsterExpressiveTheme(darkTheme = true, dynamicColor = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "loading",
                        enterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(450, easing = FastOutSlowInEasing)
                            ) + fadeIn(
                                animationSpec = tween(450, easing = FastOutSlowInEasing)
                            ) + scaleIn(
                                initialScale = 0.85f,
                                animationSpec = tween(450, easing = FastOutSlowInEasing)
                            )
                        },
                        exitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                animationSpec = tween(450, easing = FastOutSlowInEasing),
                                targetOffset = { it / 4 }
                            ) + fadeOut(
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ) + scaleOut(
                                targetScale = 0.92f,
                                animationSpec = tween(450, easing = FastOutSlowInEasing)
                            )
                        },
                        popEnterTransition = {
                            slideIntoContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(450, easing = FastOutSlowInEasing),
                                initialOffset = { it / 4 }
                            ) + fadeIn(
                                animationSpec = tween(450, easing = FastOutSlowInEasing)
                            ) + scaleIn(
                                initialScale = 0.92f,
                                animationSpec = tween(450, easing = FastOutSlowInEasing)
                            )
                        },
                        popExitTransition = {
                            slideOutOfContainer(
                                towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                animationSpec = tween(450, easing = FastOutSlowInEasing)
                            ) + fadeOut(
                                animationSpec = tween(300, easing = FastOutSlowInEasing)
                            ) + scaleOut(
                                targetScale = 0.85f,
                                animationSpec = tween(450, easing = FastOutSlowInEasing)
                            )
                        }
                    ) {
                        composable("home") { MonsterTrackerScreen(navController) }

                        composable(
                            "new_can",
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                ) + fadeIn(
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                ) + scaleIn(
                                    initialScale = 0.8f,
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                )
                            },
                            exitTransition = {
                                fadeOut(
                                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                                ) + scaleOut(
                                    targetScale = 0.95f,
                                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                                )
                            },
                            popEnterTransition = {
                                fadeIn(
                                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                                ) + scaleIn(
                                    initialScale = 0.95f,
                                    animationSpec = tween(250, easing = FastOutSlowInEasing)
                                )
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                ) + fadeOut(
                                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                                ) + scaleOut(
                                    targetScale = 0.8f,
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                )
                            }
                        ) {
                            NewCanScreen(onClose = { navController.popBackStack() })
                        }

                        composable(
                            "loading",
                            enterTransition = { fadeIn(animationSpec = tween(300)) },
                            exitTransition = { fadeOut(animationSpec = tween(300)) }
                        ) {
                            ExpressiveLoadingScreen(navController)
                        }

                        composable(
                            "can_detail/{drinkId}",
                            enterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                                ) + fadeIn(
                                    animationSpec = tween(450, easing = FastOutSlowInEasing),
                                    initialAlpha = 0.3f
                                ) + scaleIn(
                                    initialScale = 0.88f,
                                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                                )
                            },
                            exitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                                    animationSpec = tween(450, easing = FastOutSlowInEasing),
                                    targetOffset = { it / 4 }
                                ) + fadeOut(
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                    targetAlpha = 0.5f
                                ) + scaleOut(
                                    targetScale = 0.92f,
                                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                                )
                            },
                            popEnterTransition = {
                                slideIntoContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(450, easing = FastOutSlowInEasing),
                                    initialOffset = { it / 4 }
                                ) + fadeIn(
                                    animationSpec = tween(450, easing = FastOutSlowInEasing),
                                    initialAlpha = 0.5f
                                ) + scaleIn(
                                    initialScale = 0.92f,
                                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                                )
                            },
                            popExitTransition = {
                                slideOutOfContainer(
                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                                ) + fadeOut(
                                    animationSpec = tween(300, easing = FastOutSlowInEasing),
                                    targetAlpha = 0.3f
                                ) + scaleOut(
                                    targetScale = 0.88f,
                                    animationSpec = tween(450, easing = FastOutSlowInEasing)
                                )
                            }
                        ) { backStackEntry ->
                            val drinkId = backStackEntry.arguments?.getString("drinkId")?.toLongOrNull() ?: 0L
                            CanDetailsScreen(
                                drinkId = drinkId,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonsterExpressiveTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColorScheme(
            primary = Color(0xFF8CF79F),
            background = Color(0xFF0A0C0A),
            surface = Color(0xFF111411),
            primaryContainer = Color(0xFF005227),
            onPrimaryContainer = Color(0xFF8CF79F)
        )

        else -> lightColorScheme(primary = Color(0xFF006D36))
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes(
            medium = RoundedCornerShape(20.dp),
            large = RoundedCornerShape(28.dp),
            extraLarge = RoundedCornerShape(32.dp)
        ),
        typography = Typography(
            displayMedium = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            ),
            titleLarge = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MonsterTrackerScreen(navController: NavHostController, viewModel: DrinkViewModel = viewModel()) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )

    val recentDrinks by viewModel.recentDrinks.collectAsState()
    val totalDrinksCount by viewModel.totalDrinksCount.collectAsState()
    val favoritesCount by viewModel.favoritesCount.collectAsState()
    val todaysCaffeineIntake by viewModel.todaysCaffeineIntake.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val averageDailyCaffeine by viewModel.averageDailyCaffeine.collectAsState()

    val isLoading = recentDrinks.isEmpty() && totalDrinksCount == 0

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = { ExpressiveTopBar(scrollBehavior) },
        bottomBar = { ExpressiveNavBar() },
        floatingActionButton = { ExpressiveFAB({ navController.navigate("new_can") }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 100.dp,
                start = 0.dp,
                end = 0.dp
            ),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    DailyLimitHero(todaysCaffeineIntake)
                }
            }

            item {
                StatsLazyRow(
                    totalCans = totalDrinksCount,
                    favorites = favoritesCount,
                    streak = currentStreak,
                    avgCaffeine = averageDailyCaffeine
                )
            }

            item {
                Text(
                    "Recent Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                )
            }

            if (isLoading) {
                items(3) {
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        DrinkItemSkeleton()
                    }
                }
            } else {
                items(recentDrinks) { drink ->
                    Box(Modifier.padding(horizontal = 16.dp)) {
                        DrinkHistoryItem(
                            drink = drink,
                            onClick = { navController.navigate("can_detail/${drink.id}") }
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun StatsLazyRow(
    totalCans: Int = 0,
    favorites: Int = 0,
    streak: Int = 0,
    avgCaffeine: Int = 0
) {
    val stats = listOf(
        StatData(
            Icons.Rounded.LocalDrink,
            totalCans.toString(),
            "Total Cans",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        ),
        StatData(
            Icons.Rounded.Favorite,
            favorites.toString(),
            "Favorites",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        ),
        StatData(
            Icons.Rounded.LocalFireDepartment,
            streak.toString(),
            "Day Streak",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        ),
        StatData(
            Icons.Rounded.Bolt,
            "${avgCaffeine}mg",
            "Avg/Day",
            MaterialTheme.colorScheme.surfaceContainerHigh,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    )

    val lazyListState = rememberLazyListState()

    var titleVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        titleVisible = true
    }

    val titleOffset by animateDpAsState(
        targetValue = if (titleVisible) 0.dp else (-20).dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "titleOffset"
    )

    val titleAlpha by animateFloatAsState(
        targetValue = if (titleVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "titleAlpha"
    )

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Quick Stats",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .offset(y = titleOffset)
                .alpha(titleAlpha)
        )

        LazyRow(
            state = lazyListState,
            flingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState),
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(stats) { index, item ->
                StatCard(item, index)
            }
        }
    }
}

@Composable
fun StatCard(item: StatData, index: Int = 0) {
    var isPressed by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay((index * 100L))
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = when {
            !isVisible -> 0.8f
            isPressed -> 0.95f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(500),
        label = "alpha"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "elevation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "iconAnimation")
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (item.icon == Icons.Rounded.LocalFireDepartment) 15f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconRotation"
    )

    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (item.icon == Icons.Rounded.Bolt) 1.15f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    Surface(
        modifier = Modifier
            .width(180.dp)
            .height(145.dp)
            .scale(scale)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        color = item.containerColor,
        shape = RoundedCornerShape(36.dp),
        tonalElevation = 6.dp,
        shadowElevation = elevation
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                item.contentColor.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset.Infinite
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(22.dp)
            ) {
                Icon(
                    item.icon,
                    null,
                    tint = item.contentColor,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.TopStart)
                        .rotate(iconRotation)
                        .scale(iconScale)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = item.value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = item.contentColor,
                        letterSpacing = (-0.5).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        fontSize = 30.sp
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = item.contentColor.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        letterSpacing = 0.3.sp
                    )
                }
            }
        }
    }
}


@Composable
fun DrinkItemSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "drinkSkeleton")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image skeleton
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha)
                    )
            )

            // Content skeleton
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Title skeleton
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha)
                            )
                    )
                    // Subtitle skeleton
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha * 0.7f)
                            )
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Tag skeletons
                    repeat(2) {
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(24.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha * 0.5f)
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DailyLimitHero(todaysCaffeineIntake: Int = 0) {
    val dailyLimit = 400
    val progress = (todaysCaffeineIntake.toFloat() / dailyLimit).coerceIn(0f, 1f)
    val actualPercentage = (todaysCaffeineIntake.toFloat() / dailyLimit * 100).roundToInt()
    val isOverLimit = todaysCaffeineIntake > dailyLimit

    var isPressed by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = when {
            !isVisible -> 0.95f
            isPressed -> 0.98f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "scale"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(600),
        label = "cardAlpha"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "elevation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val iconRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconRotation"
    )

    val iconScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(175.dp)
            .scale(scale)
            .alpha(cardAlpha)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    }
                )
            },
        shape = RoundedCornerShape(36.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverLimit)
                MaterialTheme.colorScheme.errorContainer
            else
                MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                if (isOverLimit)
                                    MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.08f)
                                else
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            start = androidx.compose.ui.geometry.Offset(0f, 0f),
                            end = androidx.compose.ui.geometry.Offset.Infinite
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "TODAY'S INTAKE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = if (isOverLimit)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        letterSpacing = 1.sp
                    )
                    Text(
                        "${todaysCaffeineIntake}mg",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Black,
                        color = if (isOverLimit)
                            MaterialTheme.colorScheme.onErrorContainer
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        letterSpacing = (-1).sp
                    )
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(CircleShape),
                        color = if (isOverLimit)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer,
                        trackColor = if (isOverLimit)
                            MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.3f)
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                    )
                    Text(
                        if (isOverLimit)
                            "$actualPercentage% - OVER LIMIT!"
                        else
                            "$actualPercentage% of daily limit",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverLimit)
                            MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha)
                        else
                            MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                        fontWeight = if (isOverLimit) FontWeight.Bold else FontWeight.Medium,
                        letterSpacing = 0.3.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
                Icon(
                    Icons.Rounded.Bolt,
                    null,
                    tint = if (isOverLimit)
                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.12f)
                    else
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                    modifier = Modifier
                        .size(90.dp)
                        .rotate(iconRotation)
                        .scale(iconScale)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveTopBar(scrollBehavior: TopAppBarScrollBehavior) {
    MediumTopAppBar(
        title = {
            Text(
                "Caffinate",
                fontWeight = FontWeight.Black,
                style = MaterialTheme.typography.headlineLarge,
                letterSpacing = (-0.5).sp
            )
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.mediumTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.95f),
            titleContentColor = MaterialTheme.colorScheme.onBackground
        )
    )
}

@Composable
fun DrinkHistoryItem(drink: Drink, onClick: () -> Unit = {}) {
    val timeText = formatTimeAgo(drink.consumedDate)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        onClick = onClick
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                contentAlignment = Alignment.Center
            ) {
                if (drink.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = drink.imageUrl,
                        contentDescription = drink.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Rounded.LocalDrink, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    drink.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${drink.category} • ${drink.caffeineContent}mg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    timeText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (drink.isFavorite) {
                    Icon(
                        Icons.Rounded.Favorite,
                        contentDescription = "Favorite",
                        modifier = Modifier.size(16.dp).padding(top = 4.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemExpressive(data: HistoryData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.LocalDrink, null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(data.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    data.desc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                data.time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveFAB(onClick: () -> Unit) {
    MediumFloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Icon(Icons.Rounded.Add, null, modifier = Modifier.size(32.dp))
    }
}


@Composable
fun ExpressiveNavBar() {
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Rounded.Home, null) },
            label = { Text("Home") })
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Outlined.PhotoLibrary, null) },
            label = { Text("Gallery") })
        NavigationBarItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Outlined.BarChart, null) },
            label = { Text("Stats") })
    }
}

data class StatData(
    val icon: ImageVector,
    val value: String,
    val label: String,
    val containerColor: Color,
    val contentColor: Color
)

data class HistoryData(val name: String, val desc: String, val time: String)

@Composable
fun getHistoryData(): List<HistoryData> = listOf(
    HistoryData("Mango Loco", "Juicy kick • 160mg", "9:00 AM"),
    HistoryData("Original Green", "The classic • 160mg", "Yesterday"),
    HistoryData("Ultra White", "Zero sugar • 150mg", "Oct 24")
)

fun formatTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> {
            val hours = diff / 3_600_000
            if (hours == 1L) "1h ago" else "${hours}h ago"
        }

        diff < 172_800_000 -> "Yesterday"
        diff < 604_800_000 -> {
            val days = diff / 86_400_000
            "${days}d ago"
        }

        else -> {
            val sdf = SimpleDateFormat("MMM dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveLoadingScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2000)
        navController.navigate("home") {
            popUpTo("loading") { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            LoadingIndicator(
                modifier = Modifier.size(120.dp),
                color = MaterialTheme.colorScheme.primary
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Caffinate",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    "Loading your energy...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun FullAppPreview() {
    MonsterExpressiveTheme(darkTheme = true) {
        Surface(color = MaterialTheme.colorScheme.background) {
            MonsterTrackerScreen(rememberNavController())
        }
    }
}
