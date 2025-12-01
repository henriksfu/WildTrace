package com.example.group21

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapShader
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Path
import android.graphics.Shader
import androidx.core.graphics.createBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import androidx.compose.ui.graphics.toArgb

suspend fun createSightingMarkerBitmap(
    context: Context,
    imageUrl: String?,
    size: Int = 110,
    borderWidth: Float = 6f,
    color: Color
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
    val pointerHeight = size / 6f
    val circleRadius = (size - borderWidth * 2 - pointerHeight) / 2f
    val centerX = size / 2f
    val centerY = circleRadius + borderWidth

    val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)

// --- Use Matrix.setRectToRect for center-crop scaling ---
    val bitmapRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
    val targetRect = RectF(
        centerX - circleRadius,
        centerY - circleRadius,
        centerX + circleRadius,
        centerY + circleRadius
    )
    val matrix = Matrix()
    matrix.setRectToRect(bitmapRect, targetRect, Matrix.ScaleToFit.CENTER)
// ScaleToFit.CENTER preserves aspect ratio; to fully fill the circle (centerCrop) use CENTER with postScale
    val scaleX = targetRect.width() / bitmapRect.width()
    val scaleY = targetRect.height() / bitmapRect.height()
    val scale = maxOf(scaleX, scaleY)
    matrix.setScale(scale, scale)
    val dx = targetRect.centerX() - bitmapRect.centerX() * scale
    val dy = targetRect.centerY() - bitmapRect.centerY() * scale
    matrix.postTranslate(dx, dy)

    shader.setLocalMatrix(matrix)

    paint.shader = shader
    paint.style = Paint.Style.FILL
    canvas.drawCircle(centerX, centerY, circleRadius, paint)
    paint.shader = null

// --- Draw circle border ---
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = borderWidth
    paint.color = color.toArgb()
    canvas.drawCircle(centerX, centerY, circleRadius, paint)

// --- Draw triangle pointer at bottom ---
    val path = Path()
    val pointerWidth = size / 6f
    path.moveTo(centerX, size.toFloat()) // tip of triangle
    path.lineTo(centerX - pointerWidth * 1.1f, centerY + circleRadius * 0.9f) // left base
    path.lineTo(centerX + pointerWidth * 1.1f, centerY + circleRadius * 0.9f) // right base
    path.close()

    paint.style = Paint.Style.FILL
    paint.color = color.toArgb()
    canvas.drawPath(path, paint)

    return markerBitmap
}



