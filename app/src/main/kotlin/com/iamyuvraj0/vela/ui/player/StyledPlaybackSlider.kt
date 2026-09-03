package com.iamyuvraj0.vela.ui.player

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.foundation.progressSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.iamyuvraj0.vela.constants.SliderStyle
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledPlaybackSlider(
    sliderStyle: SliderStyle,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    activeColor: Color,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
) {
    val safeValue = value.coerceIn(valueRange.start, valueRange.endInclusive)
    val enabled = valueRange.start < valueRange.endInclusive
    val colors = when (sliderStyle) {
        SliderStyle.Wavy -> PlayerSliderColors.wavy(activeColor)
        SliderStyle.Simple -> PlayerSliderColors.simple(activeColor)
        else -> PlayerSliderColors.standard(activeColor)
    }

    if (sliderStyle == SliderStyle.Circular) {
        CircularPlaybackSlider(
            value = safeValue,
            valueRange = valueRange,
            enabled = enabled,
            activeColor = activeColor,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            modifier = modifier,
        )
        return
    }

    Slider(
        value = safeValue,
        onValueChange = onValueChange,
        onValueChangeFinished = onValueChangeFinished,
        valueRange = valueRange,
        enabled = enabled,
        colors = colors,
        thumb = {
            if (sliderStyle != SliderStyle.Simple && sliderStyle != SliderStyle.Wavy) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(activeColor, CircleShape),
                )
            }
        },
        track = { sliderState ->
            when (sliderStyle) {
                SliderStyle.Standard -> SliderDefaults.Track(
                    sliderState = sliderState,
                    colors = colors,
                    enabled = enabled,
                )
                SliderStyle.Thick -> VelaStyledTrack(
                    sliderState = sliderState,
                    activeColor = activeColor,
                    inactiveColor = colors.inactiveTrackColor,
                    trackHeight = 10.dp,
                )
                SliderStyle.Simple -> VelaStyledTrack(
                    sliderState = sliderState,
                    activeColor = colors.activeTrackColor,
                    inactiveColor = colors.inactiveTrackColor,
                    trackHeight = 4.dp,
                )
                SliderStyle.Wavy -> VelaWavyTrack(
                    sliderState = sliderState,
                    activeColor = activeColor,
                    inactiveColor = colors.inactiveTrackColor,
                    animate = isPlaying,
                )
                SliderStyle.Circular -> error("Circular slider is handled above")
            }
        },
        modifier = modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VelaStyledTrack(
    sliderState: SliderState,
    activeColor: Color,
    inactiveColor: Color,
    trackHeight: androidx.compose.ui.unit.Dp,
) {
    val fraction = fractionOf(sliderState)
    Canvas(Modifier.fillMaxWidth().height(trackHeight)) {
        val y = size.height / 2f
        val stroke = trackHeight.toPx()
        drawLine(inactiveColor, Offset(0f, y), Offset(size.width, y), stroke, StrokeCap.Round)
        drawLine(activeColor, Offset(0f, y), Offset(size.width * fraction, y), stroke, StrokeCap.Round)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VelaWavyTrack(
    sliderState: SliderState,
    activeColor: Color,
    inactiveColor: Color,
    animate: Boolean,
) {
    val fraction = fractionOf(sliderState)
    val phase = remember { Animatable(0f) }

    LaunchedEffect(animate) {
        if (!animate) {
            phase.snapTo(0f)
            return@LaunchedEffect
        }
        while (true) {
            phase.animateTo(
                targetValue = (2f * PI).toFloat(),
                animationSpec = tween(durationMillis = 1400, easing = LinearEasing),
            )
            phase.snapTo(0f)
        }
    }

    Canvas(Modifier.fillMaxWidth().height(12.dp)) {
        val centerY = size.height / 2f
        val amplitude = 1.8.dp.toPx()
        val wavelength = 18.dp.toPx()
        val stroke = 4.dp.toPx()
        val currentPhase = phase.value

        fun drawWave(color: Color, endX: Float, animated: Boolean) {
            if (endX <= 0f) return
            var x = 0f
            var previous = Offset(0f, centerY)
            while (x <= endX) {
                val angle = (x / wavelength) * (2f * PI.toFloat()) + if (animated) currentPhase else 0f
                val y = centerY + sin(angle) * if (animated) amplitude else amplitude * 0.55f
                val current = Offset(x, y)
                drawLine(color, previous, current, stroke, StrokeCap.Round)
                previous = current
                x += 3.dp.toPx()
            }
        }

        drawWave(inactiveColor, size.width, false)
        drawWave(activeColor, size.width * fraction, animate)
    }
}

@Composable
private fun CircularPlaybackSlider(
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    enabled: Boolean,
    activeColor: Color,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    modifier: Modifier,
) {
    val fraction = fractionOf(value, valueRange)
    val interactionModifier = if (enabled) {
        modifier
            .fillMaxWidth()
            .height(72.dp)
            .pointerInput(valueRange, enabled) {
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    onValueChange(
                        valueFromFraction(
                            circularFraction(down.position, size.width / 2f, size.height / 2f),
                            valueRange,
                        ),
                    )

                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull() ?: break
                        val nextFraction = circularFraction(
                            change.position,
                            size.width / 2f,
                            size.height / 2f,
                        )
                        onValueChange(valueFromFraction(nextFraction, valueRange))
                        change.consume()
                        if (!change.pressed) break
                    }
                    onValueChangeFinished()
                }
            }
    } else {
        modifier.fillMaxWidth().height(72.dp)
    }

    Box(
        modifier = interactionModifier
            .progressSemantics(value, valueRange)
            .semantics {
                contentDescription = "Playback progress"
            },
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f - 10.dp.toPx()).coerceAtLeast(1f)
            val stroke = 6.dp.toPx()
            val startAngle = -90f
            val sweep = 360f * fraction

            drawArc(
                color = Color.White.copy(alpha = 0.16f),
                startAngle = startAngle,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (fraction > 0f) {
                drawArc(
                    color = activeColor,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }

            val thumbAngle = Math.toRadians((startAngle + sweep).toDouble())
            val thumb = Offset(
                x = center.x + radius * cos(thumbAngle).toFloat(),
                y = center.y + radius * sin(thumbAngle).toFloat(),
            )
            drawCircle(color = activeColor, radius = 6.dp.toPx(), center = thumb)
        }
    }
}

private fun circularFraction(offset: Offset, centerX: Float, centerY: Float): Float {
    var angle = Math.toDegrees(atan2((offset.y - centerY).toDouble(), (offset.x - centerX).toDouble())).toFloat()
    angle = (angle + 450f) % 360f
    return (angle / 360f).coerceIn(0f, 1f)
}

private fun valueFromFraction(
    fraction: Float,
    valueRange: ClosedFloatingPointRange<Float>,
): Float = valueRange.start + (valueRange.endInclusive - valueRange.start) * fraction

private fun fractionOf(value: Float, range: ClosedFloatingPointRange<Float>): Float {
    return if (range.endInclusive > range.start) {
        ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
    } else {
        0f
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun fractionOf(state: SliderState): Float = fractionOf(state.value, state.valueRange)
