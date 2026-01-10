# Caffinate Database Implementation
- AI DOCS

## Overview

This app now uses **Room Database** to store and manage energy drink consumption data locally on the device. The database automatically populates with sample data on first launch.

## Database Architecture

#### 1. **Entities**
- **`Drink`** (`data/Drink.kt`) - Main entity representing an energy drink entry
  - Fields include: name, brand, caffeine content, sugar, size, location, rating, notes, tags, etc.
  - Timestamps: `consumedDate`, `createdAt`, `updatedAt`

#### 2. **DAO (Data Access Object)**
- **`DrinkDao`** (`data/DrinkDao.kt`) - Database operations interface
  - CRUD operations (Create, Read, Update, Delete)
  - Complex queries for statistics and analytics
  - Uses Kotlin Flow for reactive data streams

#### 3. **Database Class**
- **`CaffinateDatabase`** (`data/CaffinateDatabase.kt`) - Room database singleton
  - Includes callback to populate sample data on first creation
  - Version 1 schema

#### 4. **Repository**
- **`DrinkRepository`** (`data/DrinkRepository.kt`) - Data layer abstraction
  - Handles business logic for data operations
  - Provides clean API for ViewModels

#### 5. **ViewModel**
- **`DrinkViewModel`** (`data/DrinkViewModel.kt`) - UI state management
  - Exposes StateFlows for UI consumption
  - Manages coroutine scopes for database operations

## Key Features

### 📊 **Statistics Tracking**
- **Total Drinks Count** - All drinks consumed
- **Favorites Count** - Number of favorited drinks
- **Today's Caffeine Intake** - Total caffeine consumed today (mg)
- **Average Daily Caffeine** - Rolling 30-day average
- **Current Streak** - Consecutive days with drinks logged
- **Daily Limit Progress** - Percentage of FDA recommended limit (400mg/day)

### 📱 **Main Screen Integration**
The `MainActivity` now displays **real-time data** from the database:
- **Daily Intake Hero Card** - Shows today's caffeine with progress bar
- **Quick Stats** - 4 stat cards with live data
- **Recent Activity** - List of recent drinks with images, timestamps, and favorites

### 🔄 **Reactive Data Flow**
All data uses **Kotlin Flow** for reactive updates:
```kotlin
val recentDrinks by viewModel.recentDrinks.collectAsState()
val todaysCaffeineIntake by viewModel.todaysCaffeineIntake.collectAsState()
```

## Sample Data

The database auto-populates with 7 sample drinks on first launch:
1. **Mango Loco** - 160mg caffeine (3 hours ago) ⭐ Favorite
2. **Original Green** - 160mg caffeine (Yesterday)
3. **Ultra White** - 150mg, Zero Sugar (2 days ago) ⭐ Favorite
4. **Pipeline Punch** - 160mg caffeine (3 days ago) ⭐ Favorite
5. **Java Monster Mean Bean** - 188mg caffeine (4 days ago)
6. **Reign Orange Dreamsicle** - 300mg high caffeine (5 days ago) ⭐ Favorite
7. **Red Bull Original** - 80mg caffeine (6 days ago)

## Database Schema

```sql
CREATE TABLE drinks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    brand TEXT DEFAULT '',
    category TEXT DEFAULT 'Energy Drink',
    caffeineContent INTEGER NOT NULL,
    sugarContent INTEGER DEFAULT 0,
    size TEXT DEFAULT '16 fl oz (473ml)',
    sizeInMl INTEGER DEFAULT 473,
    location TEXT DEFAULT '',
    rating REAL DEFAULT 0.0,
    imageUrl TEXT DEFAULT '',
    consumedDate INTEGER NOT NULL,
    notes TEXT DEFAULT '',
    tags TEXT DEFAULT '[]',
    isFavorite INTEGER DEFAULT 0,
    calories INTEGER DEFAULT 0,
    createdAt INTEGER NOT NULL,
    updatedAt INTEGER NOT NULL
);
```

## Usage Examples

### Insert a New Drink
```kotlin
val viewModel: DrinkViewModel = viewModel()

val newDrink = Drink(
    name = "Monster Ultra Violet",
    brand = "Monster Energy",
    caffeineContent = 150,
    rating = 4.5f,
    tags = listOf("Zero Sugar", "Grape"),
    isFavorite = true
)

viewModel.insertDrink(newDrink)
```

### Update a Drink
```kotlin
viewModel.updateDrink(drink.copy(
    rating = 5.0f,
    notes = "My favorite!",
    isFavorite = true
))
```

### Toggle Favorite
```kotlin
viewModel.toggleFavorite(drinkId = 1L)
```

### Query Recent Drinks
```kotlin
val recentDrinks by viewModel.recentDrinks.collectAsState()

LazyColumn {
    items(recentDrinks) { drink ->
        DrinkHistoryItem(drink)
    }
}
```

## Dependencies Added

### build.gradle.kts (Module)
```kotlin
// Room Database
val roomVersion = "2.6.1"
implementation("androidx.room:room-runtime:$roomVersion")
implementation("androidx.room:room-ktx:$roomVersion")
ksp("androidx.room:room-compiler:$roomVersion")

// Gson for JSON serialization (tags list)
implementation("com.google.code.gson:gson:2.10.1")
```

### build.gradle.kts (Project)
```kotlin
id("com.google.devtools.ksp") version "2.1.0-1.0.29" apply false
```

## File Structure

```
com.wiffle.caffinate/
├── data/
│   ├── Drink.kt              # Entity
│   ├── DrinkDao.kt           # Database queries
│   ├── DrinkRepository.kt    # Data layer
│   ├── DrinkViewModel.kt     # ViewModel
│   ├── CaffinateDatabase.kt  # Database class
│   └── Converters.kt         # Type converters (List<String>)
├── MainActivity.kt           # Main screen with real data
└── CanViewActivity.kt        # Detail screen
```

## Time Formatting

The app includes smart time formatting:
- **Just now** - Less than 1 minute ago
- **5m ago** - Minutes ago
- **2h ago** - Hours ago
- **Yesterday** - 1 day ago
- **3d ago** - Days ago
- **Oct 24** - Older dates show month/day

## Next Steps

### 🚀 **Potential Enhancements**
1. **CanViewActivity Integration** - Load drink details from database by ID
2. **Search Functionality** - Search drinks by name, brand, or tags
3. **Date Range Filtering** - View drinks for specific date ranges
4. **Export Data** - Export to CSV/JSON
5. **Import Data** - Import from file or backup
6. **Cloud Sync** - Sync across devices with Firebase
7. **Charts & Analytics** - Visualize consumption patterns
8. **Notifications** - Caffeine intake warnings
9. **Widgets** - Home screen widgets for quick stats

## API Reference

### DrinkViewModel Methods

| Method | Description |
|--------|-------------|
| `insertDrink(drink)` | Add new drink to database |
| `updateDrink(drink)` | Update existing drink |
| `deleteDrink(drink)` | Delete drink |
| `toggleFavorite(id)` | Toggle favorite status |
| `loadDrink(id)` | Load specific drink by ID |
| `searchDrinks(query)` | Search drinks by query |

### StateFlows Available

| StateFlow | Type | Description |
|-----------|------|-------------|
| `recentDrinks` | `List<Drink>` | 10 most recent drinks |
| `allDrinks` | `List<Drink>` | All drinks |
| `favoriteDrinks` | `List<Drink>` | Only favorited drinks |
| `totalDrinksCount` | `Int` | Total number of drinks |
| `favoritesCount` | `Int` | Number of favorites |
| `todaysCaffeineIntake` | `Int` | Today's caffeine in mg |
| `todaysDrinkCount` | `Int` | Drinks consumed today |
| `averageDailyCaffeine` | `Int` | 30-day average caffeine |
| `currentStreak` | `Int` | Consecutive days streak |

## Performance Notes

- **Room uses coroutines** - All database operations are async
- **Flow is lifecycle-aware** - Automatically stops when UI is destroyed
- **Database is cached** - Singleton pattern prevents multiple instances
- **Queries are optimized** - Indexed on `consumedDate` for fast sorting

## Troubleshooting

### Database not populating?
- Check if database file exists in app data
- Uninstall and reinstall app to trigger fresh creation
- Check Logcat for Room migration errors

### StateFlows not updating?
- Ensure you're collecting as State: `collectAsState()`
- Check ViewModel is properly scoped to Activity/NavGraph
- Verify database operations are in coroutine scope

### Type conversion errors?
- Check `Converters.kt` is properly registered
- Ensure `@TypeConverters` annotation is on Database class
- Validate JSON structure for List<String> fields

---

**Built with ❤️ using Room, Kotlin Coroutines, and Jetpack Compose**
