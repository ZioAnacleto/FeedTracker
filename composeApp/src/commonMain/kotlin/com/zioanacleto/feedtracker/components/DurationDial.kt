package com.zioanacleto.feedtracker.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun DurationDial(durationMs: Long, onDurationChange: (Long) -> Unit, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val numberColor = colors.onSurface
    val tickColor = colors.onSurface.copy(alpha = 0.85f)
    val ringColor = colors.onSurface.copy(alpha = 0.16f)
    val markerColor = colors.primary
    val durationLabel = formatDurationHm(durationMs)
    val durationState = rememberUpdatedState(durationMs)
    val onDurationChangeState = rememberUpdatedState(onDurationChange)
    var lastPointerAngle by remember { mutableFloatStateOf(Float.NaN) }
    var dragging by remember { mutableStateOf(false) }
    var dragTurns by remember { mutableFloatStateOf(turnsFromDurationMs(durationMs)) }
    val displayTurns = if (dragging) dragTurns else turnsFromDurationMs(durationMs)
    val density = LocalDensity.current
    val minTurns = turnsFromDurationMs(MIN_DURATION_MS)
    val maxTurns = turnsFromDurationMs(MAX_DURATION_MS)

    val maxDiameter = durationDialMaxDiameter
    val sizeModifier = if (maxDiameter != null) {
        Modifier.wrapContentWidth(Alignment.CenterHorizontally).size(maxDiameter)
    } else {
        Modifier.fillMaxWidth().aspectRatio(1f)
    }

    BoxWithConstraints(
        modifier = modifier
            .then(sizeModifier)
            .padding(8.dp)
            .semantics { contentDescription = durationLabel }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        dragging = true
                        dragTurns = turnsFromDurationMs(durationState.value)
                        lastPointerAngle = pointerAngleDegrees(
                            x = offset.x,
                            y = offset.y,
                            centerX = size.width / 2f,
                            centerY = size.height / 2f,
                        )
                    },
                    onDragEnd = {
                        dragging = false
                        lastPointerAngle = Float.NaN
                    },
                    onDragCancel = {
                        dragging = false
                        lastPointerAngle = Float.NaN
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val angle = pointerAngleDegrees(
                            x = change.position.x,
                            y = change.position.y,
                            centerX = size.width / 2f,
                            centerY = size.height / 2f,
                        )
                        val previous = lastPointerAngle
                        lastPointerAngle = angle
                        if (previous.isNaN()) return@detectDragGestures
                        val deltaTurns = shortestAngleDelta(previous, angle) / 360f
                        dragTurns = (dragTurns + deltaTurns).coerceIn(minTurns, maxTurns)
                        onDurationChangeState.value(durationMsFromTurns(dragTurns))
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        val diameter = maxWidth
        val diameterPx = with(density) { diameter.toPx() }

        Canvas(
            modifier = Modifier
                .size(diameter)
                .graphicsLayer { rotationZ = displayTurns * 360f },
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = min(size.width, size.height) / 2f
            val tickOuter = outerRadius * 0.76f
            val tickInner = outerRadius * 0.64f
            val hourTickInner = outerRadius * 0.60f

            drawCircle(color = ringColor, radius = tickOuter, center = center)
            drawCircle(
                color = tickColor.copy(alpha = 0.35f),
                radius = tickOuter,
                center = center,
                style = Stroke(width = 2.dp.toPx()),
            )

            for (index in 0 until 60) {
                val angle = (index * 6f - 90f) * PI.toFloat() / 180f
                val isHour = index % 5 == 0
                val inner = if (isHour) hourTickInner else tickInner
                drawLine(
                    color = tickColor,
                    start = Offset(
                        x = center.x + cos(angle) * inner,
                        y = center.y + sin(angle) * inner,
                    ),
                    end = Offset(
                        x = center.x + cos(angle) * tickOuter,
                        y = center.y + sin(angle) * tickOuter,
                    ),
                    strokeWidth = if (isHour) 3.dp.toPx() else 1.5.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }

            drawLine(
                color = markerColor,
                start = Offset(center.x, 2.dp.toPx()),
                end = Offset(center.x, 22.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }

        val numberRadiusPx = diameterPx * 0.90f / 2f
        for (value in 1..12) {
            val angle = (value * 30f - 90f) * PI.toFloat() / 180f
            Text(
                text = value.toString(),
                color = numberColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                modifier = Modifier.offset {
                    IntOffset(
                        x = (cos(angle) * numberRadiusPx).roundToInt(),
                        y = (sin(angle) * numberRadiusPx).roundToInt(),
                    )
                },
            )
        }

        Text(
            text = durationLabel,
            color = colors.onSurface,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
