package com.saurav.pixelmusic.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

// MD3 Expressive – Emphasized motion curves
val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
val EmphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

// Transition duration aligned with depth scale, corner radius, and dim animations
const val TRANSITION_DURATION = 380

// Forward Push: Enter from Right — foreground screen slides in 100% cleanly over the receding card
fun enterTransition(): EnterTransition = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialOffsetX = { fullWidth -> fullWidth }
) + fadeIn(
    animationSpec = tween(durationMillis = TRANSITION_DURATION / 3, easing = EmphasizedDecelerateEasing)
)

// Forward Push: Exit to Left — background card recedes with subtle parallax (-18%) into the back
fun exitTransition(): ExitTransition = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
    targetOffsetX = { fullWidth -> -(fullWidth * 0.18f).toInt() }
)

// Backward Pop: Enter from Left — returning background card returns from parallax
fun popEnterTransition(): EnterTransition = slideInHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialOffsetX = { fullWidth -> -(fullWidth * 0.18f).toInt() }
)

// Backward Pop: Exit to Right — top screen slides out to right cleanly
fun popExitTransition(): ExitTransition = slideOutHorizontally(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
    targetOffsetX = { fullWidth -> fullWidth }
) + fadeOut(
    animationSpec = tween(durationMillis = TRANSITION_DURATION / 2, delayMillis = TRANSITION_DURATION / 2, easing = EmphasizedAccelerateEasing)
)
