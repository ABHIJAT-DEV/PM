package com.saurav.pixelmusic.presentation.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

private val TabSmoothMorphEasing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

@Composable
fun TabAnimation(
    modifier: Modifier = Modifier,
    index: Int,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    onSelectedColor: Color = MaterialTheme.colorScheme.onPrimary,
    unselectedColor: Color = MaterialTheme.colorScheme.surface,
    onUnselectedColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
    title: String,
    selectedIndex: Int,
    onClick: () -> Unit,
    transformOrigin: TransformOrigin = TransformOrigin.Center,
    selectionFraction: Float? = null,
    content: @Composable () -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val isSelected = index == selectedIndex
    val tabShape = CircleShape
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetFraction = (selectionFraction ?: if (isSelected) 1f else 0f).coerceIn(0f, 1f)
    val morphFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 520, easing = TabSmoothMorphEasing),
        label = "Tab Morph Fraction"
    )

    // Transient ~1s neighbor-push pulse when a tab becomes selected, then falls back to default size
    val pushPulse = remember { Animatable(0f) }
    var hasInitialized by remember { mutableStateOf(false) }
    LaunchedEffect(isSelected) {
        if (!hasInitialized) {
            hasInitialized = true
            return@LaunchedEffect
        }
        if (isSelected) {
            pushPulse.snapTo(0f)
            pushPulse.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 280, easing = TabSmoothMorphEasing)
            )
            pushPulse.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 680, easing = TabSmoothMorphEasing)
            )
        } else {
            pushPulse.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 220, easing = TabSmoothMorphEasing)
            )
        }
    }

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "Tab Press Scale"
    )

    val containerColor = lerp(unselectedColor, selectedColor, morphFraction)
    val contentColor = lerp(onUnselectedColor, onSelectedColor, morphFraction)
    val pillScale = (1f + 0.04f * pushPulse.value) * pressScale

    Tab(
        modifier = modifier
            .padding(all = 4.dp)
            .graphicsLayer {
                scaleX = pillScale
                scaleY = pillScale
                this.transformOrigin = transformOrigin
            }
            .clip(tabShape)
            .background(containerColor, tabShape),
        selected = isSelected,
        interactionSource = interactionSource,
        text = {
            Box(
                modifier = Modifier.padding(horizontal = (14f * pushPulse.value).dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        },
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        selectedContentColor = contentColor,
        unselectedContentColor = contentColor
    )
}
