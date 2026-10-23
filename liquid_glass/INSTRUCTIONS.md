# Liquid Glass Architecture & Integration Guide

Comprehensive instructions for implementing Liquid Glass surfaces, spring physics animations, and theme-adaptive styling across PixelMusic.

---

## 1. Core Architecture & Philosophy

PixelMusic utilizes an iOS-style **Liquid Glass** aesthetic powered by Kyant0's backdrop shader library (`io.github.kyant0:backdrop:2.0.1`) combined with dual-spring touch physics.

### Design Principles

1. **The Shadow Policy**:
   - **Material You Expressive (Standard Mode)**: Retains elevation hierarchy and drop shadows (`4.dp` – `8.dp`).
   - **Liquid Glass Mode**: **STRICTLY ZERO SHADOW** (`elevation = 0.dp`, `shadowElevation = 0.dp`, `tonalElevation = 0.dp`). Glass relies on optical refraction, specular rim gradients, and top highlight sheens. External drop shadows create muddy halos that destroy realistic glass refraction.

2. **Optical Effects Pipeline (Kyant0 Backdrop)**:
   - Order: `vibrancy()` (1.5x saturation) → `blur()` (variable radius) → `lens()` (optical refraction, Android 13+ / API 33+).
   - Below API 33: Gracefully falls back to frosted translucent surfaces with specular rim gradients.

3. **Specular Rim & Highlight Sheen**:
   - `0.75.dp` hairline border using a directional vertical gradient (top-bright, bottom-subtle).
   - Top highlight sheen overlay to simulate overhead ambient illumination.

---

## 2. Global Settings Toggle Integration

Liquid Glass is globally controlled by the user in Settings. All components **MUST** react dynamically to this state and provide seamless fallbacks.

### Accessing Global Preferences

```kotlin
// In any @Composable function:
val isLiquidGlass = LocalLiquidGlassEnabled.current
val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

// Optional intensity controls:
val blurIntensity = LocalLiquidGlassBlurIntensity.current
val refractionIntensity = LocalLiquidGlassRefractionIntensity.current
```

### Translucent Glass Palette

When `isLiquidGlass` is active, container colors should use semi-transparent variants so underlying backdrops and colors shine through:

```kotlin
// Primary action button (e.g., Play All)
val playBg = if (isLiquidGlass) {
    MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.55f else 0.70f)
} else {
    MaterialTheme.colorScheme.primary
}

// Secondary / Tonal containers (e.g., Shuffle, Chips)
val secondaryBg = if (isLiquidGlass) {
    MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isDark) 0.55f else 0.70f)
} else {
    MaterialTheme.colorScheme.secondaryContainer
}

// Neutral glass surface (e.g., Back button, 3-dot, Settings)
val neutralBg = if (isLiquidGlass) {
    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.55f else 0.70f)
} else {
    MaterialTheme.colorScheme.surfaceContainerHigh
}
```

---

## 3. Spring Physics & Touch Interaction

Liquid Glass elements do not use standard Material ripples. Instead, the tactile feedback comes from **Dual-Spring Squash & Stretch** volume conservation physics.

### Dual Independent Springs

- **`press` spring**: Tracks contact duration.
  - `dampingRatio = 0.5f`, `stiffness = 450f` (firm, controlled compression).
- **`jiggle` spring**: Instant elastic contact transient.
  - `dampingRatio = 0.32f`, `stiffness = 280f` (rapid underdamped oscillation).

### Squash & Stretch Transform Equations

- **Pills / Capsules / Horizontal Buttons**:
  ```kotlin
  scaleX = 1f + (press * 0.08f) + (jiggle * 0.12f)
  scaleY = 1f - (press * 0.06f) - (jiggle * 0.10f)
  ```
- **Circles / Icon Buttons**:
  ```kotlin
  scaleX = 1f + (press * 0.10f) + (jiggle * 0.15f)
  scaleY = 1f - (press * 0.08f) - (jiggle * 0.13f)
  ```

*Important*: Always apply these transforms to the component's inner `graphicsLayer` or `layerBlock` so the sampled backdrop underneath remains anchored and does not jitter.

---

## 4. Reusable Modifier (`liquidGlassInteractiveSurface`)

For almost all UI elements (buttons, pills, icon buttons, cards, bottom sheet items), use the unified modifier:

**Package**: `com.saurav.pixelmusic.presentation.components.liquidGlassInteractiveSurface`

```kotlin
Modifier.liquidGlassInteractiveSurface(
    shape = CircleShape,   // Any Shape: CircleShape, RoundedCornerShape, AbsoluteSmoothCornerShape
    isCircle = false,      // Set true for circular buttons (triggers circular spring scaling)
    borderWidth = 0.75.dp, // Default specular rim border width
    enabled = true         // Dynamic toggle
)
```

This modifier automatically:
1. Observes `LocalLiquidGlassEnabled` (returns unchanged if disabled).
2. Sets `shadowElevation = 0f` to guarantee zero shadow.
3. Applies dual-spring touch gestures and scale animations.
4. Renders the adaptive specular vertical gradient rim border.
5. Strictly clips to `shape`.

---

## 5. Critical Traps & Rules (MUST READ)

### Trap 1: The Touch-Target Halo Bug (M3 `minimumInteractiveComponentSize`)
- **Symptom**: A larger transparent glass circle or rectangle border appears floating around a smaller inner button (e.g. 40dp button inside a 48dp halo).
- **Cause**: Material 3 buttons with visual size < 48dp expand outer layout bounds to 48dp for accessibility. Modifiers applied outside will wrap the 48dp box instead of the visual component.
- **Fix**: Always wrap small buttons in:
  ```kotlin
  CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
      FilledTonalIconButton(
          modifier = Modifier
              .size(44.dp)
              .liquidGlassInteractiveSurface(isCircle = true),
          ...
      )
  }
  ```

### Trap 2: Grey Rectangular Corner Artifacts
- **Symptom**: Dark/AMOLED themes show grey rectangular corners behind rounded glass components.
- **Cause**: Unclipped `drawRect` inside `drawWithContent`.
- **Fix**: Never use unclipped `drawRect`. Always use `.clip(shape)` and pass `shape` directly to `.background(color, shape)` and `.border(stroke, shape)`.

### Trap 3: Persistent Elevation / Drop Shadows
- **Symptom**: Glass button casts a drop shadow, causing a muddy dark ring.
- **Fix**: Reset button elevation when Liquid Glass is active:
  ```kotlin
  elevation = if (isLiquidGlass) ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp) else ButtonDefaults.buttonElevation()
  ```

---

## 6. Real-World Code Recipes

### Recipe 1: Circular Top Bar Icon Button (Back, 3-Dot, Settings, Sort)

```kotlin
val isLiquidGlass = LocalLiquidGlassEnabled.current
val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
    FilledTonalIconButton(
        modifier = Modifier
            .size(44.dp)
            .liquidGlassInteractiveSurface(isCircle = true),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (isLiquidGlass) {
                MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.55f else 0.70f)
            } else {
                MaterialTheme.colorScheme.surfaceContainerHigh
            },
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = { /* Handle Click */ }
    ) {
        Icon(Icons.Filled.MoreVert, contentDescription = "Options")
    }
}
```

### Recipe 2: Expressive Pill Action Button (Play All, Shuffle, Add)

```kotlin
val isLiquidGlass = LocalLiquidGlassEnabled.current
val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
val pillShape = CircleShape

Button(
    onClick = { /* Handle Click */ },
    shape = pillShape,
    modifier = Modifier
        .height(42.dp)
        .liquidGlassInteractiveSurface(shape = pillShape)
        .animateContentSize(),
    colors = ButtonDefaults.buttonColors(
        containerColor = if (isLiquidGlass) {
            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.55f else 0.70f)
        } else {
            MaterialTheme.colorScheme.primary
        },
        contentColor = MaterialTheme.colorScheme.onPrimary
    ),
    elevation = if (isLiquidGlass) {
        ButtonDefaults.buttonElevation(0.dp, 0.dp, 0.dp, 0.dp, 0.dp)
    } else {
        ButtonDefaults.buttonElevation()
    }
) {
    Icon(Icons.Rounded.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
    Spacer(Modifier.width(6.dp))
    Text("Play all")
}
```

### Recipe 3: Bottom Sheet Action Row / List Item

```kotlin
@Composable
fun GlassActionItem(
    icon: Painter,
    label: String,
    onClick: () -> Unit
) {
    val isLiquidGlass = LocalLiquidGlassEnabled.current
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val itemShape = remember { RoundedCornerShape(18.dp) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .liquidGlassInteractiveSurface(shape = itemShape)
            .clip(itemShape)
            .background(
                if (isLiquidGlass) {
                    MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = if (isDark) 0.55f else 0.70f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isLiquidGlass) {
                        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = if (isDark) 0.55f else 0.70f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerHighest
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
```
