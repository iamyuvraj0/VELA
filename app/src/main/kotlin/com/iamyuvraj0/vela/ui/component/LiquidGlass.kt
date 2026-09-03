package com.iamyuvraj0.vela.ui.component

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop as nativeBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy

typealias PlatformBackdrop = LayerBackdrop

val LocalBackdrop = staticCompositionLocalOf<PlatformBackdrop?> { null }

@Composable
fun rememberBackdrop(): PlatformBackdrop = rememberLayerBackdrop()

fun Modifier.layerBackdrop(backdrop: PlatformBackdrop): Modifier = this.nativeBackdrop(backdrop)

/**
 * Genuine backdrop-sampled liquid glass used by VELA's floating surfaces.
 * On Android versions without RenderEffect/backdrop support the caller's normal
 * surface rendering is preserved.
 */
fun Modifier.velaLiquidGlass(
    backdrop: PlatformBackdrop?,
    shape: Shape,
    enabled: Boolean = false, // TEMP: disabled to isolate RenderThread SIGSEGV crash — was Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
): Modifier {
    if (!enabled || backdrop == null) return this

    return drawBackdrop(
        backdrop = backdrop,
        effects = {
            vibrancy()
            blur(24f.dp.toPx())
            lens(18f.dp.toPx(), size.minDimension / 2f, true)
        },
        onDrawBackdrop = { drawBackdrop ->
            drawBackdrop()
        },
        shape = { shape },
        onDrawSurface = {
            drawRect(Color.White.copy(alpha = 0.14f))
        },
    )
}
