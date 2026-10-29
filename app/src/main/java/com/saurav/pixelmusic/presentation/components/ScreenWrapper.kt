package com.saurav.pixelmusic.presentation.components

import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.navigation.compose.currentBackStackEntryAsState
import com.saurav.pixelmusic.presentation.viewmodel.PlayerViewModel
import androidx.lifecycle.compose.currentStateAsState
import com.saurav.pixelmusic.presentation.navigation.isMainRootRoute
import com.saurav.pixelmusic.presentation.navigation.TRANSITION_DURATION
import com.saurav.pixelmusic.data.preferences.AppBackgroundStyle

@OptIn(UnstableApi::class)
@Composable
fun ScreenWrapper(
    navController: androidx.navigation.NavController,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    // Lifecycle State
    var isResumed by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isResumed = true
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                isResumed = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Initial Check
    val currentState = lifecycleOwner.lifecycle.currentStateAsState().value
    if (currentState.isAtLeast(Lifecycle.State.RESUMED)) {
        isResumed = true
    }

    // Visible entries is the public Navigation API designed for transition-aware stacking.
    val visibleEntries by navController.visibleEntries.collectAsStateWithLifecycle()
    val myEntry = lifecycleOwner as? androidx.navigation.NavBackStackEntry
    val myIndex = visibleEntries.indexOfFirst { it.id == myEntry?.id }
    val topIndex = visibleEntries.indexOfLast {
        it.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)
    }

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val isNavigationTarget = myEntry != null && currentBackStackEntry?.id == myEntry.id
    val myRoute = myEntry?.destination?.route
    val isMainRootScreen = isMainRootRoute(myRoute)
    val hasVisibleNonMainRootScreen = visibleEntries.any { entry ->
        entry.destination.route?.let { route -> !isMainRootRoute(route) } == true
    }
    val shouldRunDepthEffects = !isMainRootScreen || hasVisibleNonMainRootScreen

    // Receding card depth logic:
    // Only the screen underneath recedes (scales down, rounds corners, dims).
    // The incoming/top screen is always full scale (1.0f) with 0dp corners to prevent floating-box clipping.
    val isUnderneath = remember(visibleEntries, myEntry, myIndex, topIndex, isNavigationTarget) {
        !isNavigationTarget &&
            myIndex != -1 &&
            topIndex != -1 &&
            myIndex < topIndex &&
            myEntry?.lifecycle?.currentState != Lifecycle.State.CREATED
    }

    val depthEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    // Depth Scale: background card recedes to 0.94f; top screen stays 1.0f
    val targetScale = if (shouldRunDepthEffects && isUnderneath) 0.94f else 1.0f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = tween(durationMillis = TRANSITION_DURATION, easing = depthEasing),
        label = "depthScale"
    )

    // Corner Radius: background card rounds its corners to 28dp as it recedes into a card
    val targetRadius = if (shouldRunDepthEffects && isUnderneath) 28f else 0f
    val cornerRadius by animateFloatAsState(
        targetValue = targetRadius,
        animationSpec = tween(durationMillis = TRANSITION_DURATION, easing = depthEasing),
        label = "cornerRadius"
    )

    // Dim overlay: background card dims to 0.35f black
    val targetDim = if (shouldRunDepthEffects && isUnderneath) 0.35f else 0f
    val dimAlpha by animateFloatAsState(
        targetValue = targetDim,
        animationSpec = tween(durationMillis = TRANSITION_DURATION, easing = depthEasing),
        label = "dimAlpha"
    )

    // Fetch background style to determine if the ScreenWrapper should become transparent
    val backgroundStyle by playerViewModel.userPreferencesRepository.appBackgroundStyleFlow.collectAsStateWithLifecycle(initialValue = AppBackgroundStyle.DEFAULT)

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                transformOrigin = TransformOrigin(0.5f, 0.5f)
                if (shouldRunDepthEffects && cornerRadius > 0.5f) {
                    this.shape = RoundedCornerShape(cornerRadius.dp)
                    this.clip = true
                } else {
                    this.clip = false
                }
            }
            // If using a custom wallpaper, make the wrapper transparent so the MainActivity background shows through!
            .background(if (backgroundStyle != AppBackgroundStyle.DEFAULT) Color.Transparent else MaterialTheme.colorScheme.background)
    ) {
        content()

        // Dim Layer Overlay
        if (dimAlpha > 0.005f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = dimAlpha }
                    .background(Color.Black)
            )
        }
    }
}
