package com.saurav.pixelmusic.presentation.screens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import racra.compose.smooth_corner_rect_library.AbsoluteSmoothCornerShape

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
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val targetFraction = (selectionFraction ?: if (isSelected) 1f else 0f).coerceIn(0f, 1f)
    val morphFraction by animateFloatAsState(
        targetValue = targetFraction,
        animationSpec = tween(durationMillis = 520, easing = TabSmoothMorphEasing),
        label = "Tab Morph Fraction"
    )

    val pressScale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "Tab Press Scale"
    )

    val targetCorner = when {
        isPressed -> 12.dp
        else -> 15.dp + 13.dp * morphFraction
    }
    val cornerRadius by animateDpAsState(
        targetValue = targetCorner,
        animationSpec = tween(durationMillis = 480, easing = TabSmoothMorphEasing),
        label = "Tab Corner Radius"
    )
    val tabShape = remember(cornerRadius) { AbsoluteSmoothCornerShape(cornerRadius, 60) }

    val containerColor = lerp(unselectedColor, selectedColor, morphFraction)
    val contentColor = lerp(onUnselectedColor, onSelectedColor, morphFraction)
    val pillScale = (1f + 0.03f * morphFraction) * pressScale

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
        text = content,
        onClick = {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        selectedContentColor = contentColor,
        unselectedContentColor = contentColor
    )
}
