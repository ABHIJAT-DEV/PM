package com.saurav.pixelmusic.presentation.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.ui.graphics.TransformOrigin

// MD3 Expressive – Emphasized motion curves
val EmphasizedDecelerateEasing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
val EmphasizedAccelerateEasing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

// Transition duration aligned across scale, blur, and opacity
const val TRANSITION_DURATION = 350

// Forward Push: Enter — new screen scales in smoothly as an overlay over the receding screen
fun enterTransition(): EnterTransition = scaleIn(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedDecelerateEasing),
    initialScale = 0.90f,
    transformOrigin = TransformOrigin(0.5f, 0.5f)
) + fadeIn(
    animationSpec = tween(durationMillis = TRANSITION_DURATION * 2 / 3, easing = EmphasizedDecelerateEasing)
)

// Forward Push: Exit — background screen recedes via ScreenWrapper (scale out + blur + dim)
fun exitTransition(): ExitTransition = ExitTransition.None

// Backward Pop: Enter — returning screen restores via ScreenWrapper (scale in + unblur + undim)
fun popEnterTransition(): EnterTransition = EnterTransition.None

// Backward Pop: Exit — top screen scales down and fades out cleanly
fun popExitTransition(): ExitTransition = scaleOut(
    animationSpec = tween(TRANSITION_DURATION, easing = EmphasizedAccelerateEasing),
    targetScale = 0.90f,
    transformOrigin = TransformOrigin(0.5f, 0.5f)
) + fadeOut(
    animationSpec = tween(durationMillis = TRANSITION_DURATION / 2, easing = EmphasizedAccelerateEasing)
)
