package com.airbnb.lottie.sample.compose.composables

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.gesture.DragObserver
import androidx.compose.ui.gesture.dragGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview

@Composable
fun SeekBar(
    progress: Float,
    onProgressChanged: (Float) -> Unit,
    trackHeight: Dp = 2.dp,
    thumbRadius: Dp = 4.dp,
    unfilledTrackColor: Color = Color.LightGray,
    filledTrackColor: Color = Color.Red,
    modifier: Modifier = Modifier
) {
    var width = 0
    var dragStartProgress = 0f
    var dragDistanceX = 0f
    val dragObserver = remember {
        object : DragObserver {
            override fun onStart(downPosition: Offset) {
                dragStartProgress = downPosition.x / width.toFloat()
                if (width > 0) onProgressChanged(dragStartProgress)
            }
            override fun onDrag(dragDistance: Offset): Offset {
                dragDistanceX += dragDistance.x
                if (width > 0) {
                    val newProgress = (dragStartProgress + dragDistanceX / width.toFloat()).coerceIn(0f, 1f)
                    onProgressChanged(newProgress)
                }
                return dragDistance
            }

            override fun onStop(velocity: Offset) {
                dragDistanceX = 0f
            }
        }
    }
    Row(
        modifier = Modifier
            .onGloballyPositioned { width = it.size.width }
            .dragGestureFilter(dragObserver, startDragImmediately = true)
            .padding(vertical = 12.dp)
            .then(modifier)
    ) {
        Canvas(
            modifier = Modifier
                .preferredHeight(thumbRadius * 2f)
                .fillMaxWidth()
        ) {
            val thumbX = size.width * progress
            drawRect(
                color = unfilledTrackColor,
                topLeft = Offset(thumbX, size.height / 2f - trackHeight.toPx() / 2f),
                size = Size(size.width - thumbX, trackHeight.toPx())
            )
            drawRect(
                color = filledTrackColor,
                topLeft = Offset(0f, size.height / 2f - trackHeight.toPx() / 2f),
                size = Size(thumbX, trackHeight.toPx())
            )
            drawCircle(
                color = filledTrackColor,
                radius = thumbRadius.toPx(),
                center = Offset(thumbX, size.height / 2f)
            )
        }
    }
}

@Preview
@Composable
fun SeekBarPreview() {
    SeekBar(0.5f, {})
}