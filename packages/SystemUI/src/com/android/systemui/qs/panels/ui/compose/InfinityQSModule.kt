/*
 * SPDX-FileCopyrightText: Project Infinity X
 * SPDX-License-Identifier: LicenseRef-InfinityX-Proprietary
 */

package com.android.systemui.qs.panels.ui.compose

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.android.systemui.qs.panels.ui.compose.infinitegrid.rememberClassicTileIconSize
import kotlin.math.abs

@Composable
fun InfinityQSModule(
    style: Int,
    quickQsMode: Boolean = false,
    animatedVisibility: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val sanitizedStyle = sanitizeInfinityQsStyle(style)
    if (sanitizedStyle == INFINITY_QS_STYLE_STOCK) {
        return
    }
    if (!animatedVisibility) {
        Box(modifier = modifier) {
            InfinityQSModuleContent(
                style = sanitizedStyle,
                quickQsMode = quickQsMode,
            )
        }
    } else {
        AnimatedVisibility(
            visible = true,
            enter = expandVertically(tween(300)) + fadeIn(tween(300)),
            exit = shrinkVertically(tween(250)) + fadeOut(tween(250)),
            modifier = modifier,
        ) {
            InfinityQSModuleContent(
                style = sanitizedStyle,
                quickQsMode = quickQsMode,
            )
        }
    }
}

private enum class OxygenRightPaneMode {
    MEDIA,
    SLIDERS,
}

private object InfinityOxygenRightPaneState {
    var mode by mutableStateOf(OxygenRightPaneMode.SLIDERS)
    var mediaAvailable by mutableStateOf(false)
}

@Composable
private fun InfinityQSModuleContent(style: Int, quickQsMode: Boolean) {
    val contentHeight = if (quickQsMode) 144.dp else 160.dp
    val connectivityHeight = if (quickQsMode) 76.dp else 84.dp

    val showConnectivity = style != INFINITY_QS_STYLE_INFINITY_X
    val oxygenStyle =
        style == INFINITY_QS_STYLE_OXYGENOS || style == INFINITY_QS_STYLE_SPIDEY
    val spideyStyle = style == INFINITY_QS_STYLE_SPIDEY
    val spideyRadius =
        if (spideyStyle) (rememberClassicTileIconSize() - 8.dp) / 2 else null
    val spideyCircleSize = spideyRadius?.times(2)
    val spideyPaneHeight =
        if (spideyCircleSize != null) (spideyCircleSize * 2) + 12.dp else 0.dp
    val detectedMediaAvailable = if (oxygenStyle) rememberInfinityMediaHasSession() else false

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
    val rootDensity = LocalDensity.current
    val rootGridWidth = with(rootDensity) { constraints.maxWidth.toDp() }
    val rootColumnWidth = (rootGridWidth - 24.dp) / 4
    val circleSize = rememberClassicTileIconSize() - 8.dp
    val edgeInset = ((rootColumnWidth - circleSize) / 2).coerceAtLeast(0.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = if (!spideyStyle) 0.dp else if (quickQsMode) 12.dp else 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (showConnectivity && !oxygenStyle) {
            InfinityConnectivityModule(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(connectivityHeight),
            )
        }

        if (spideyStyle) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(spideyPaneHeight),
            ) {
                val density = LocalDensity.current
                val gridWidth = with(density) { constraints.maxWidth.toDp() }
                val columnWidth = (gridWidth - 24.dp) / 4
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    InfinityMediaPanel(
                        modifier = Modifier
                            .width(columnWidth * 2 + 8.dp)
                            .fillMaxHeight(),
                    )
                    Box(
                        modifier = Modifier
                            .width(columnWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        InfinityBrightnessModule(
                            modifier = Modifier
                                .width(spideyCircleSize ?: connectivityHeight)
                                .fillMaxHeight(),
                            capsuleStyle = true,
                            capsuleRadius = spideyRadius,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .width(columnWidth)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        InfinityVolumeModule(
                            modifier = Modifier
                                .width(spideyCircleSize ?: connectivityHeight)
                                .fillMaxHeight(),
                            capsuleStyle = true,
                            capsuleRadius = spideyRadius,
                        )
                    }
                }
            }
        } else if (oxygenStyle) {
            LaunchedEffect(detectedMediaAvailable) {
                InfinityOxygenRightPaneState.mediaAvailable = detectedMediaAvailable
                InfinityOxygenRightPaneState.mode =
                    if (detectedMediaAvailable) OxygenRightPaneMode.MEDIA
                    else OxygenRightPaneMode.SLIDERS
            }
            var horizontalDragAccumulation by remember { mutableFloatStateOf(0f) }
            val swipeThresholdPx = 80f
            val rightPaneMode = InfinityOxygenRightPaneState.mode
            val mediaAvailable = InfinityOxygenRightPaneState.mediaAvailable

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(circleSize * 2 + 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                InfinityConnectivityModule(
                    modifier = Modifier
.width(rootColumnWidth * 2 + 8.dp)
                    .padding(horizontal = edgeInset)
                    .fillMaxHeight(),
                stackMobileBelowWifi = true,
                capsuleStyle = true,
            )

                Box(
                    modifier =
                        Modifier
                            .width(rootColumnWidth * 2 + 8.dp)
                            .fillMaxHeight()
                            .pointerInput(mediaAvailable, rightPaneMode) {
                                if (!mediaAvailable) return@pointerInput

                                detectHorizontalDragGestures(
                                    onDragStart = { horizontalDragAccumulation = 0f },
                                    onHorizontalDrag = { _, dragAmount ->
                                        horizontalDragAccumulation += dragAmount
                                    },
                                    onDragEnd = {
                                        if (abs(horizontalDragAccumulation) >= swipeThresholdPx) {
                                            InfinityOxygenRightPaneState.mode =
                                                if (rightPaneMode == OxygenRightPaneMode.MEDIA) {
                                                    OxygenRightPaneMode.SLIDERS
                                                } else {
                                                    OxygenRightPaneMode.MEDIA
                                                }
                                        }
                                        horizontalDragAccumulation = 0f
                                    },
                                    onDragCancel = {
                                        horizontalDragAccumulation = 0f
                                    },
                                )
                            }
                ) {
                    if (mediaAvailable && rightPaneMode == OxygenRightPaneMode.MEDIA) {
                        InfinityMediaPanel(modifier = Modifier.fillMaxWidth().fillMaxHeight())
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth().fillMaxHeight(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                contentAlignment = Alignment.Center,
                            ) {
                                InfinityBrightnessModule(
                                    modifier = Modifier.width(circleSize).fillMaxHeight(),
                                    capsuleStyle = true,
                                )
                            }
                            Box(
                                modifier = Modifier.weight(1f).fillMaxHeight(),
                                contentAlignment = Alignment.Center,
                            ) {
                                InfinityVolumeModule(
                                    modifier = Modifier.width(circleSize).fillMaxHeight(),
                                    capsuleStyle = true,
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(contentHeight),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .width(rootColumnWidth * 2 + 8.dp)
                        .fillMaxHeight(),
                ) {
                    InfinityMediaPanel(modifier = Modifier.fillMaxWidth().fillMaxHeight())
                }

                Box(
                    modifier = Modifier.width(rootColumnWidth).fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    InfinityBrightnessModule(
                        modifier = Modifier.width(circleSize).fillMaxHeight(),
                    )
                }

                Box(
                    modifier = Modifier.width(rootColumnWidth).fillMaxHeight(),
                    contentAlignment = Alignment.Center,
                ) {
                    InfinityVolumeModule(
                        modifier = Modifier.width(circleSize).fillMaxHeight(),
                    )
                }
            }
        }
    }
    }
}
