/*
 * Copyright (C) 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.shade.ui

import android.content.Context
import com.android.internal.graphics.ColorUtils
import com.android.systemui.res.R

object ShadeColors {
    private const val QS_PANEL_COLOR_STYLE = "qs_panel_color_style"
    private const val QS_PANEL_COLOR_TRANSPARENT = 2

    @JvmStatic
    fun isTransparentQsPanel(context: Context): Boolean =
        android.provider.Settings.System.getIntForUser(
            context.contentResolver,
            QS_PANEL_COLOR_STYLE,
            1,
            android.os.UserHandle.USER_CURRENT,
        ) == QS_PANEL_COLOR_TRANSPARENT

    /**
     * Calculate notification shade panel color.
     *
     * @param context Context to resolve colors.
     * @param blurSupported Whether blur is enabled (can be off due to battery saver)
     * @param withScrim Whether to composite a scrim when blur is enabled (used by legacy shade).
     * @return color for the shade panel.
     */
    @JvmStatic
    fun shadePanel(context: Context, blurSupported: Boolean, withScrim: Boolean): Int {
        if (isTransparentQsPanel(context)) {
            return android.graphics.Color.TRANSPARENT
        }
        return if (blurSupported) {
            if (withScrim) {
                ColorUtils.compositeColors(
                    shadePanelStandard(context),
                    shadePanelScrimBehind(context),
                )
            } else {
                shadePanelStandard(context)
            }
        } else {
            shadePanelFallback(context)
        }
    }

    @JvmStatic
    fun notificationScrim(context: Context, blurSupported: Boolean): Int {
        if (isTransparentQsPanel(context)) {
            return android.graphics.Color.TRANSPARENT
        }
        return if (blurSupported) {
            notificationScrimStandard(context)
        } else {
            notificationScrimFallback(context)
        }
    }

    @JvmStatic
    fun shadePanelScrimBehind(context: Context): Int {
        return context.resources.getColor(
            com.android.internal.R.color.shade_panel_scrim,
            context.theme,
        )
    }

    /**
     * Background color of the rounded cornered rect behind Notifications in Single- or SplitShade.
     */
    @JvmStatic
    fun classicShadeNotificationScrimBg(
        context: Context,
        blurSupported: Boolean,
        composited: Boolean,
    ): Int {
        val surfaceEffect0Color =
            context.resources.getColor(
                com.android.internal.R.color.customColorSurfaceEffect0,
                context.theme,
            )
        if (!composited || isTransparentQsPanel(context)) {
            // SplitShade (avoid adding the shade panel with scrim)
            return surfaceEffect0Color
        }
        // SingleShade
        val shadePanelWithScrim = shadePanel(context, blurSupported, withScrim = true)
        return ColorUtils.compositeColors(surfaceEffect0Color, shadePanelWithScrim)
    }

    @JvmStatic
    private fun shadePanelStandard(context: Context): Int {
        val layerAbove =
            context.resources.getColor(com.android.internal.R.color.shade_panel_fg, context.theme)
        val layerBelow =
            context.resources.getColor(com.android.internal.R.color.shade_panel_bg, context.theme)
        return ColorUtils.compositeColors(layerAbove, layerBelow)
    }

    @JvmStatic
    private fun shadePanelFallback(context: Context): Int {
        return context.getColor(R.color.shade_panel_fallback)
    }

    @JvmStatic
    private fun notificationScrimStandard(context: Context): Int {
        return ColorUtils.setAlphaComponent(
            context.getColor(R.color.notification_scrim_base),
            (0.5f * 255).toInt(),
        )
    }

    @JvmStatic
    private fun notificationScrimFallback(context: Context): Int {
        return context.getColor(R.color.notification_scrim_fallback)
    }
}
