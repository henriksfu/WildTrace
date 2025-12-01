package com.example.group21

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.view.View
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Path
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import androidx.compose.ui.graphics.toArgb

@Composable
fun SightingMarker() {
    Canvas(
        modifier = Modifier.size(120.dp)
    ) {
        drawCircle(
            color = Color.Red,
            radius = size.minDimension / 2f
        )
    }
}

suspend fun ComposableToBitmap(
    context: Context,
    width: Int,
    height: Int
): Bitmap {
    val composeView = ComposeView(context)

    // Render a solid red circle
    composeView.setContent {
        Canvas(modifier = Modifier.size(width.dp)) {
            drawCircle(
                color = Color.Red,
                radius = size.minDimension / 2f
            )
        }
    }

    // Measure + layout
    composeView.measure(
        View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY),
        View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY)
    )
    composeView.layout(0, 0, width, height)

    // Draw into bitmap
    val bitmap = createBitmap(width, height)
    val canvas = android.graphics.Canvas(bitmap)
    composeView.draw(canvas)

    return bitmap
}

suspend fun createSightingMarkerBitmap(
    context: Context,
    imageUrl: String?,
    size: Int = 120,
    borderWidth: Float = 4f
): Bitmap {
    // --- Load the image synchronously ---
    val loader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .data(imageUrl?.takeIf { it.isNotBlank() })
        .allowHardware(false)
        .build()

    val drawable = runCatching {
        val result = loader.execute(request)
        (result.drawable ?: context.getDrawable(R.drawable.image_not_found)) as BitmapDrawable
    }.getOrElse {
        context.getDrawable(R.drawable.image_not_found) as BitmapDrawable
    }

    val bitmap = drawable.bitmap

    // --- Create bitmap for marker ---
    val markerBitmap = createBitmap(size, size)
    val canvas = android.graphics.Canvas(markerBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // --- Draw circular image ---
    val imageSize = size - borderWidth * 2
    val left = borderWidth
    val top = borderWidth
    val right = left + imageSize
    val bottom = top + imageSize
    val rect = RectF(left, top, right, bottom)
    canvas.drawBitmap(bitmap, null, rect, paint)

    // --- Draw circle border ---
    paint.style = Paint.Style.STROKE
    val borderColor = Color.Red
    paint.color = borderColor.toArgb()
    paint.strokeWidth = borderWidth
    canvas.drawCircle(size / 2f, size / 2f, imageSize / 2f, paint)

    // --- Draw triangle pointer at bottom ---
    val path = Path()
    val pointerHeight = size / 6f
    val pointerWidth = size / 6f
    path.moveTo(size / 2f, size.toFloat())                    // tip
    path.lineTo(size / 2f - pointerWidth / 2, size - pointerHeight) // left
    path.lineTo(size / 2f + pointerWidth / 2, size - pointerHeight) // right
    path.close()

    paint.style = Paint.Style.FILL
    val fillColor = Color.Blue
    paint.color = fillColor.toArgb()
    canvas.drawPath(path, paint)

    return markerBitmap
}



