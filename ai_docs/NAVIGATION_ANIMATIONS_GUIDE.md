# Jetpack Compose Navigation Animations Guide

## 🎬 Overview

Complete guide to adding beautiful animations to your Compose Navigation, including all the transitions currently implemented in the Caffinate app.

---

## 📚 Current Implementation

### Global Navigation Animations

Applied to all routes by default:

```kotlin
NavHost(
    navController = navController,
    startDestination = "loading",
    enterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(300)
        )
    },
    exitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(300)
        )
    },
    popEnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(300)
        )
    },
    popExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(300)
        )
    }
)
```

**Result:** Standard left/right slide transitions (like iOS)

---

## 🎨 Animation Types

### 1. **Slide Animations**

#### Horizontal Slide (Current Default)
```kotlin
enterTransition = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Left,
        animationSpec = tween(300)
    )
}
```

**Directions:**
- `Left` - Slides from right to left (forward navigation)
- `Right` - Slides from left to right (back navigation)
- `Up` - Slides from bottom to top
- `Down` - Slides from top to bottom

**Use Cases:**
- Left/Right: Primary navigation
- Up: Modal screens, bottom sheets
- Down: Dismissing modals

---

### 2. **Fade Animations**

#### Simple Fade
```kotlin
enterTransition = { fadeIn(animationSpec = tween(300)) }
exitTransition = { fadeOut(animationSpec = tween(300)) }
```

**Use Cases:**
- Loading screens
- Splash screens
- Subtle transitions

#### Current Implementation (Loading Screen)
```kotlin
composable(
    "loading",
    enterTransition = { fadeIn(animationSpec = tween(300)) },
    exitTransition = { fadeOut(animationSpec = tween(300)) }
)
```

---

### 3. **Scale Animations**

#### Scale with Fade (Current: Can Detail)
```kotlin
enterTransition = {
    scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(300))
}

exitTransition = {
    scaleOut(
        targetScale = 0.9f,
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(300))
}
```

**Parameters:**
- `initialScale: Float` - Starting scale (0.0 to 1.0)
- `targetScale: Float` - Ending scale
- Common values: 0.8f (shrink), 0.9f (subtle), 1.1f (expand)

**Use Cases:**
- Detail screens
- Pop-up content
- Card expansions

---

### 4. **Combined Animations**

#### Slide Up + Fade (Current: New Can Screen)
```kotlin
enterTransition = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = tween(400)
    ) + fadeIn(animationSpec = tween(400))
}

popExitTransition = {
    slideOutOfContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Down,
        animationSpec = tween(400)
    ) + fadeOut(animationSpec = tween(400))
}
```

**Use Cases:**
- Modal forms
- Add new item screens
- Bottom sheet style

---

## 🎯 Per-Route Custom Animations

### Home Screen
Uses global defaults (horizontal slide)

### New Can Screen (Modal Style)
```kotlin
composable(
    "new_can",
    enterTransition = {
        slideIntoContainer(Up) + fadeIn()
    },
    popExitTransition = {
        slideOutOfContainer(Down) + fadeOut()
    }
)
```
**Effect:** Slides up from bottom, fades in

### Loading Screen (Fade Only)
```kotlin
composable(
    "loading",
    enterTransition = { fadeIn(tween(300)) },
    exitTransition = { fadeOut(tween(300)) }
)
```
**Effect:** Smooth fade in/out

### Can Detail Screen (Scale + Fade)
```kotlin
composable(
    "can_detail/{drinkId}",
    enterTransition = {
        scaleIn(0.9f) + fadeIn()
    },
    popExitTransition = {
        scaleOut(1.1f) + fadeOut()
    }
)
```
**Effect:** Subtle zoom with fade

---

## 🛠️ Animation Specifications

### Timing Functions

#### tween() - Linear timing
```kotlin
animationSpec = tween(
    durationMillis = 300,
    delayMillis = 0,
    easing = LinearEasing
)
```

**Common Durations:**
- 150ms - Very fast (micro-interactions)
- 300ms - Standard (most transitions)
- 400ms - Slower (modal sheets)
- 600ms - Emphasized (important transitions)

#### spring() - Physics-based
```kotlin
animationSpec = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessMedium
)
```

**Damping Ratios:**
- `DampingRatioNoBouncy` - No bounce
- `DampingRatioLowBouncy` - Subtle bounce
- `DampingRatioMediumBouncy` - Moderate bounce
- `DampingRatioHighBouncy` - Lots of bounce

---

## 📖 Complete Animation Reference

### All Transition Types

| Animation | Description | Best For |
|-----------|-------------|----------|
| `slideIntoContainer` | Slide from direction | Navigation |
| `slideOutOfContainer` | Slide to direction | Navigation |
| `fadeIn` | Opacity 0 → 1 | Subtle transitions |
| `fadeOut` | Opacity 1 → 0 | Subtle transitions |
| `scaleIn` | Scale 0.x → 1.0 | Detail views |
| `scaleOut` | Scale 1.0 → 0.x | Detail views |
| `expandIn` | Expand from point | Tooltips |
| `shrinkOut` | Shrink to point | Tooltips |
| `slideIn` | Slide with offset | Custom slides |
| `slideOut` | Slide with offset | Custom slides |

---

## 💡 Custom Animation Examples

### 1. **Material Shared Axis**
```kotlin
enterTransition = {
    slideIntoContainer(Left, tween(300)) + 
    fadeIn(tween(300, delayMillis = 90))
}
exitTransition = {
    slideOutOfContainer(Left, tween(300)) + 
    fadeOut(tween(90))
}
```

### 2. **Material Fade Through**
```kotlin
enterTransition = {
    fadeIn(tween(210, delayMillis = 90)) + 
    scaleIn(initialScale = 0.92f, tween(210, delayMillis = 90))
}
exitTransition = {
    fadeOut(tween(90)) + 
    scaleOut(targetScale = 0.92f, tween(90))
}
```

### 3. **Material Elevator**
```kotlin
enterTransition = {
    slideIntoContainer(Up, tween(300)) + 
    fadeIn(tween(300)) +
    scaleIn(initialScale = 0.8f, tween(300))
}
popExitTransition = {
    slideOutOfContainer(Down, tween(300)) + 
    fadeOut(tween(300)) +
    scaleOut(targetScale = 0.8f, tween(300))
}
```

### 4. **Material Container Transform**
```kotlin
enterTransition = {
    scaleIn(initialScale = 0.8f, tween(300)) + 
    fadeIn(tween(300))
}
exitTransition = {
    scaleOut(targetScale = 1.2f, tween(300)) + 
    fadeOut(tween(150))
}
```

### 5. **iOS-Style Present Modal**
```kotlin
enterTransition = {
    slideIntoContainer(
        towards = AnimatedContentTransitionScope.SlideDirection.Up,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        )
    )
}
```

### 6. **Crossfade**
```kotlin
enterTransition = { 
    fadeIn(tween(300))
}
exitTransition = { 
    fadeOut(tween(300))
}
popEnterTransition = { 
    fadeIn(tween(300))
}
popExitTransition = { 
    fadeOut(tween(300))
}
```

---

## 🎭 Transition Timing Guide

### Understanding the 4 Transitions

1. **enterTransition** - Screen entering (forward navigation)
2. **exitTransition** - Screen exiting (forward navigation)
3. **popEnterTransition** - Screen re-entering (back navigation)
4. **popExitTransition** - Screen exiting (back navigation)

### Visual Example

```
[Screen A] → navigate → [Screen B]
    ↓                        ↓
exitTransition          enterTransition

[Screen A] ← back ← [Screen B]
    ↓                    ↓
popEnterTransition   popExitTransition
```

---

## 🔧 Implementation Tips

### 1. **Override Global Animations**
```kotlin
composable(
    "special_screen",
    enterTransition = { fadeIn() }, // Overrides global
    exitTransition = null // Uses global
)
```

### 2. **Disable Animations**
```kotlin
composable(
    "instant_screen",
    enterTransition = { EnterTransition.None },
    exitTransition = { ExitTransition.None }
)
```

### 3. **Conditional Animations**
```kotlin
enterTransition = {
    if (initialState.destination.route == "home") {
        slideIntoContainer(Left)
    } else {
        fadeIn()
    }
}
```

### 4. **Shared Element Transitions** (Advanced)
```kotlin
// Requires Compose 1.6.0+
composable(
    "detail",
    enterTransition = { fadeIn() },
    sharedElements = { /* shared element configuration */ }
)
```

---

## 📱 Platform-Specific Styles

### Android Material Design
```kotlin
// Shared Axis Z (recommended)
enterTransition = {
    fadeIn(tween(210, delayMillis = 90)) + 
    scaleIn(0.92f, tween(210, delayMillis = 90))
}
```

### iOS Style
```kotlin
// Horizontal slide
enterTransition = {
    slideIntoContainer(Left, tween(350))
}
popEnterTransition = {
    slideIntoContainer(Right, tween(350))
}
```

### Custom Material You
```kotlin
// Emphasize with spring
enterTransition = {
    slideIntoContainer(
        Left,
        spring(Spring.DampingRatioLowBouncy)
    )
}
```

---

## 🎨 Animation Best Practices

### Do's ✅
- Keep animations under 400ms
- Use consistent timing across app
- Match animation to content type
- Test on slower devices
- Use spring for natural feel
- Combine animations for richness

### Don'ts ❌
- Don't use animations longer than 600ms
- Don't over-animate (too many effects)
- Don't use different styles randomly
- Don't ignore accessibility settings
- Don't block user interaction during animation

---

## 🚀 Performance Tips

1. **Use Hardware Acceleration**
   - Compose automatically uses GPU
   - Avoid complex calculations in animations

2. **Optimize Animation Specs**
   ```kotlin
   // Good - simple tween
   animationSpec = tween(300)
   
   // Avoid - complex keyframes for nav
   animationSpec = keyframes { /* complex */ }
   ```

3. **Reduce Overdraw**
   - Don't animate both screens fully overlapping
   - Use fade when possible

4. **Test on Low-End Devices**
   - Reduce duration if janky
   - Simplify complex animations

---

## 📖 Dependencies Required

```kotlin
// In build.gradle.kts
dependencies {
    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.5")
    
    // Animation (included in Compose)
    implementation("androidx.compose.animation:animation:1.7.0")
}
```

---

## 🎓 Learning Resources

### Official Docs
- [Compose Animation](https://developer.android.com/jetpack/compose/animation)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Material Motion](https://material.io/design/motion)

### Animation Principles
- Duration: 200-400ms for most transitions
- Easing: Use Material easing curves
- Direction: Match navigation direction
- Hierarchy: Emphasize important transitions

---

## 🎬 Quick Reference

### Copy-Paste Ready Examples

#### Standard Navigation
```kotlin
enterTransition = { slideIntoContainer(Left, tween(300)) }
popEnterTransition = { slideIntoContainer(Right, tween(300)) }
```

#### Modal Sheet
```kotlin
enterTransition = { slideIntoContainer(Up, tween(400)) + fadeIn() }
popExitTransition = { slideOutOfContainer(Down, tween(400)) + fadeOut() }
```

#### Detail View
```kotlin
enterTransition = { scaleIn(0.9f) + fadeIn() }
popExitTransition = { scaleOut(1.1f) + fadeOut() }
```

#### Fade Only
```kotlin
enterTransition = { fadeIn(tween(300)) }
exitTransition = { fadeOut(tween(300)) }
```

---

## 🎉 Summary

**Current Caffinate App Animations:**
- ✅ Global: Horizontal slide (300ms)
- ✅ New Can: Slide up + fade (400ms)
- ✅ Loading: Fade in/out (300ms)
- ✅ Can Detail: Scale + fade (300ms)

**Key Takeaways:**
- Use `enterTransition`, `exitTransition`, `popEnterTransition`, `popExitTransition`
- Combine animations with `+` operator
- Duration: 300ms is standard
- Override global animations per route
- Test on real devices

---

**Built with ❤️ using Jetpack Compose Navigation**
**Make your app feel alive with smooth animations! 🚀**