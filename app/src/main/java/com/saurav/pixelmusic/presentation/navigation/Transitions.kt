package com.saurav.pixelmusic.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

// MD3 Expressive – Emphasized easing (matches Material Motion spec)
private val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
private val EmphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

// Base responsive Material 3 transition duration (~320 ms)
const val TRANSITION_DURATION = 320

// Forward Push: Enter from Right — slides in 35% of screen width with a clean fade-in
fun enterTransition(): EnterTransition = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() }
) + fadeIn(
    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
)

// Forward Push: Exit to Left — subtle parallax slide out 18% with fade-out
fun exitTransition(): ExitTransition = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
    targetOffsetX = { fullWidth -> -(fullWidth * 0.18f).toInt() }
) + fadeOut(
    animationSpec = tween(TRANSITION_DURATION / 2, easing = FastOutSlowInEasing)
)

// Backward Pop: Enter from Left — subtle parallax slide in 18% with fade-in
fun popEnterTransition(): EnterTransition = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialOffsetX = { fullWidth -> -(fullWidth * 0.18f).toInt() }
) + fadeIn(
    animationSpec = tween(TRANSITION_DURATION, easing = FastOutSlowInEasing)
)

// Backward Pop: Exit to Right — slides out 35% with fade-out
fun popExitTransition(): ExitTransition = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    targetOffsetX = { fullWidth -> (fullWidth * 0.35f).toInt() }
) + fadeOut(
    animationSpec = tween(TRANSITION_DURATION / 2, easing = FastOutSlowInEasing)
)
