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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.outlined.Home
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
import androidx.navigation.compose.currentBackStackEntryAsState
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
                    MainAppWithNavigation(navController = navController)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainAppWithNavigation(navController: NavHostController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route

    val navbarRoutes = listOf("home", "gallery")
    val showNavbar = currentDestination in navbarRoutes

    val homeViewModel: DrinkViewModel = viewModel()
    val galleryViewModel: DrinkViewModel = viewModel()

    LaunchedEffect(Unit) {
        homeViewModel.recentDrinks.value
        galleryViewModel.allDrinks.value
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(
        rememberTopAppBarState(),
        canScroll = { true }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = "loading",
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                val isHomeToGallery =
                    initialState.destination.route == "home" && targetState.destination.route == "gallery"
                val isGalleryToHome =
                    initialState.destination.route == "gallery" && targetState.destination.route == "home"

                when {
                    isHomeToGallery -> slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(100, easing = FastOutSlowInEasing))

                    isGalleryToHome -> slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(100, easing = FastOutSlowInEasing))

                    else -> slideIntoContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    ) + fadeIn(
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    ) + scaleIn(
                        initialScale = 0.9f,
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    )
                }
            },
            exitTransition = {
                val isHomeToGallery =
                    initialState.destination.route == "home" && targetState.destination.route == "gallery"
                val isGalleryToHome =
                    initialState.destination.route == "gallery" && targetState.destination.route == "home"

                when {
                    isHomeToGallery -> slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Left,
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(100, easing = FastOutSlowInEasing))

                    isGalleryToHome -> slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Right,
                        animationSpec = tween(200, easing = FastOutSlowInEasing)
                    ) + fadeOut(animationSpec = tween(100, easing = FastOutSlowInEasing))

                    else -> slideOutOfContainer(
                        towards = AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    ) + fadeOut(
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    ) + scaleOut(
                        targetScale = 0.95f,
                        animationSpec = tween(400, easing = FastOutSlowInEasing)
                    )
                }
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                    initialOffset = { (it * 0.1f).toInt() }
                ) + fadeIn(
                    animationSpec = tween(400, easing = FastOutSlowInEasing),
                    initialAlpha = 0.5f
                ) + scaleIn(
                    initialScale = 0.95f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.End,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + fadeOut(
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                ) + scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(400, easing = FastOutSlowInEasing)
                )
            }
        ) {
            composable("home") {
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = { ExpressiveTopBar(scrollBehavior, navController) },
                    floatingActionButton = {
                        Box(modifier = Modifier.padding(bottom = 80.dp)) {
                            ExpressiveFAB({ navController.navigate("new_can") })
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) { padding ->
                    MonsterTrackerContent(
                        navController = navController,
                        paddingValues = padding,
                        viewModel = homeViewModel
                    )
                }
            }

            composable("gallery") {
                Scaffold(
                    modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                    topBar = { ExpressiveTopBar(scrollBehavior, navController) },
                    floatingActionButton = {
                        Box(modifier = Modifier.padding(bottom = 80.dp)) {
                            ExpressiveFAB({ navController.navigate("new_can") })
                        }
                    },
                    floatingActionButtonPosition = FabPosition.End
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        GalleryScreenContent(
                            navController = navController,
                            viewModel = galleryViewModel
                        )
                    }
                }
            }

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

            composable(
                "settings",
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
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToBackup = { navController.navigate("backup") }
                )
            }

            composable(
                "backup",
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
                BackupManagementScreen(onBack = { navController.popBackStack() })
            }
        }

        AnimatedVisibility(
            visible = showNavbar,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(250, easing = FastOutSlowInEasing)
            ) + fadeOut(animationSpec = tween(200)),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ExpressiveNavBar(navController)
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

@Composable
fun MonsterTrackerScreen(navController: NavHostController, viewModel: DrinkViewModel = viewModel()) {
    MonsterTrackerContent(navController = navController, paddingValues = PaddingValues(0.dp), viewModel = viewModel)
}

@Composable
fun MonsterTrackerContent(
    navController: NavHostController,
    paddingValues: PaddingValues,
    viewModel: DrinkViewModel = viewModel()
) {
    val recentDrinks by viewModel.recentDrinks.collectAsState()
    val totalDrinksCount by viewModel.totalDrinksCount.collectAsState()
    val favoritesCount by viewModel.favoritesCount.collectAsState()
    val todaysCaffeineIntake by viewModel.todaysCaffeineIntake.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()
    val averageDailyCaffeine by viewModel.averageDailyCaffeine.collectAsState()

    val isLoading = recentDrinks.isEmpty() && totalDrinksCount == 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = paddingValues.calculateTopPadding() + 8.dp,
            bottom = paddingValues.calculateBottomPadding() + 100.dp,
            start = 0.dp,
            end = 0.dp
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
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
                StatCard(stat = item)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun StatCard(
    stat: StatData,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.92f
            isVisible -> 1f
            else -> 0.85f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = tween(100),
        label = "elevation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "stat")
    val iconRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconRotation"
    )

    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
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
        color = stat.containerColor,
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
                                stat.containerColor,
                                stat.contentColor.copy(alpha = 0.1f)
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
                    stat.icon,
                    null,
                    tint = stat.contentColor,
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
                        text = stat.value,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = stat.contentColor,
                        letterSpacing = (-0.5).sp,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        fontSize = 30.sp
                    )
                    Text(
                        text = stat.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = stat.contentColor.copy(alpha = 0.85f),
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


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DrinkItemSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val shimmerAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
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
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha)
                    )
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.7f)
                            .height(20.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = shimmerAlpha)
                            )
                    )
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DailyLimitHero(todaysCaffeineIntake: Int) {
    val dailyLimit = 400
    val progress = (todaysCaffeineIntake.toFloat() / dailyLimit).coerceIn(0f, 1f)
    val actualPercentage = (progress * 100).toInt()
    val isOverLimit = todaysCaffeineIntake > dailyLimit

    var isPressed by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            isVisible -> 1f
            else -> 0.9f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    val cardAlpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "cardAlpha"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = tween(150),
        label = "elevation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "dailyLimit")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val iconRotation by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconRotation"
    )

    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
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
fun ExpressiveTopBar(scrollBehavior: TopAppBarScrollBehavior, navController: NavHostController) {
    val infiniteTransition = rememberInfiniteTransition(label = "coffee")
    val coffeeRotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Rounded.LocalCafe,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .rotate(coffeeRotation),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Caffinate",
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.headlineMedium,
                    letterSpacing = (-0.5).sp
                )
            }
        },
        actions = {
            var isPressed by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isPressed) 0.85f else 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "scale"
            )

            IconButton(
                onClick = { navController.navigate("settings") },
                modifier = Modifier
                    .padding(end = 4.dp)
                    .scale(scale)
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        CircleShape
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            }
                        )
                    }
            ) {
                Icon(
                    Icons.Rounded.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DrinkHistoryItem(drink: Drink, onClick: () -> Unit = {}) {
    val timeText = formatTimeAgo(drink.consumedDate)
    var isPressed by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(50)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.95f
            isVisible -> 1f
            else -> 0.9f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "alpha"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 1.dp else 3.dp,
        animationSpec = tween(100),
        label = "elevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(alpha)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() }
                )
            },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
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
                    Icon(
                        Icons.Rounded.LocalDrink,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    drink.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "${drink.category} • ${drink.caffeineContent}mg",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        timeText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
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
    var isPressed by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            isVisible -> 1f
            else -> 0.7f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "fabScale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isPressed) 45f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabRotation"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 3.dp else 6.dp,
        animationSpec = tween(100),
        label = "fabElevation"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "fab")
    val iconPulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "iconPulse"
    )

    MediumFloatingActionButton(
        onClick = {
            isPressed = true
            onClick()
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .scale(scale)
            .graphicsLayer { rotationZ = rotation },
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = elevation,
            pressedElevation = 3.dp
        )
    ) {
        Icon(
            Icons.Rounded.Add,
            contentDescription = "Add drink",
            modifier = Modifier
                .size(32.dp)
                .scale(iconPulse)
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}


@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ExpressiveNavBar(navController: NavHostController) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        val homeScale by animateFloatAsState(
            targetValue = if (currentDestination == "home") 1.08f else 1f,
            animationSpec = tween(100, easing = FastOutSlowInEasing),
            label = "homeScale"
        )

        val homeAlpha by animateFloatAsState(
            targetValue = if (currentDestination == "home") 1f else 0.7f,
            animationSpec = tween(100),
            label = "homeAlpha"
        )

        NavigationBarItem(
            selected = currentDestination == "home",
            onClick = {
                if (currentDestination != "home") {
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = {
                Icon(
                    imageVector = if (currentDestination == "home") Icons.Rounded.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier
                        .scale(homeScale)
                        .alpha(homeAlpha)
                )
            },
            label = {
                Text(
                    "Home",
                    modifier = Modifier.alpha(homeAlpha)
                )
            }
        )

        val galleryScale by animateFloatAsState(
            targetValue = if (currentDestination == "gallery") 1.08f else 1f,
            animationSpec = tween(100, easing = FastOutSlowInEasing),
            label = "galleryScale"
        )

        val galleryAlpha by animateFloatAsState(
            targetValue = if (currentDestination == "gallery") 1f else 0.7f,
            animationSpec = tween(100),
            label = "galleryAlpha"
        )

        NavigationBarItem(
            selected = currentDestination == "gallery",
            onClick = {
                if (currentDestination != "gallery") {
                    navController.navigate("gallery") {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            icon = {
                Icon(
                    imageVector = if (currentDestination == "gallery") Icons.Rounded.PhotoLibrary else Icons.Outlined.PhotoLibrary,
                    contentDescription = "Gallery",
                    modifier = Modifier
                        .scale(galleryScale)
                        .alpha(galleryAlpha)
                )
            },
            label = {
                Text(
                    "Gallery",
                    modifier = Modifier.alpha(galleryAlpha)
                )
            }
        )

        NavigationBarItem(
            selected = false,
            onClick = {},
            enabled = false,
            icon = {
                Icon(
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = "Stats",
                    modifier = Modifier.alpha(0.4f)
                )
            },
            label = {
                Text(
                    "Stats",
                    modifier = Modifier.alpha(0.4f)
                )
            }
        )
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
