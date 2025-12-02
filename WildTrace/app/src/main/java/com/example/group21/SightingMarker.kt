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
import kotlin.math.roundToInt

suspend fun createSightingMarkerBitmap(
    context: Context,
    imageUrl: String?,
    sizeDp: Int = 60,
    borderWidth: Float = 6f,
    color: Color
): Bitmap {
    //
    // Load the image, using placeholder on fail
    var drawable: BitmapDrawable? = null
    if(imageUrl == "addSighting"){
        drawable = context.getDrawable(R.drawable.plus_symbol) as BitmapDrawable
    }
    else {
        val loader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(imageUrl?.takeIf { it.isNotBlank() })
            .allowHardware(false)
            .build()
        //
        drawable = runCatching {
            val result = loader.execute(request)
            (result.drawable ?: context.getDrawable(R.drawable.image_not_found)) as BitmapDrawable
        }.getOrElse {
            context.getDrawable(R.drawable.image_not_found) as BitmapDrawable
        }
    }
    //
    val bitmap = drawable!!.bitmap
    //
    // Convert to px
    val size = sizeDp * context.resources.displayMetrics.density.roundToInt()
    //
    // Create the bitmap
    val markerBitmap = createBitmap(size, size)
    val canvas = android.graphics.Canvas(markerBitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    //
    // Draw the border around the circular image
    val pointerHeight = size / 6f
    val circleRadius = (size - borderWidth * 2 - pointerHeight) / 2f
    val centerX = size / 2f
    val centerY = circleRadius + borderWidth
    //
    // Center the image to fill the circular frame
    val shader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
    val bitmapRect = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
    val targetRect = RectF(
        centerX - circleRadius,
        centerY - circleRadius,
        centerX + circleRadius,
        centerY + circleRadius
    )
    val matrix = Matrix()
    matrix.setRectToRect(bitmapRect, targetRect, Matrix.ScaleToFit.CENTER)
    val scaleX = targetRect.width() / bitmapRect.width()
    val scaleY = targetRect.height() / bitmapRect.height()
    val scale = maxOf(scaleX, scaleY)
    matrix.setScale(scale, scale)
    val dx = targetRect.centerX() - bitmapRect.centerX() * scale
    val dy = targetRect.centerY() - bitmapRect.centerY() * scale
    matrix.postTranslate(dx, dy)
    shader.setLocalMatrix(matrix)
    //
    // Draw the circle
    paint.shader = shader
    paint.style = Paint.Style.FILL
    canvas.drawCircle(centerX, centerY, circleRadius, paint)
    paint.shader = null
    //
    // Draw the colored border
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = borderWidth
    paint.color = color.toArgb()
    canvas.drawCircle(centerX, centerY, circleRadius, paint)
    //
    // Draw the triangular bottom
    val path = Path()
    val pointerWidth = size / 6f
    path.moveTo(centerX, size.toFloat()) // tip of triangle
    path.lineTo(centerX - pointerWidth * 1.1f, centerY + circleRadius * 0.9f) // left base
    path.lineTo(centerX + pointerWidth * 1.1f, centerY + circleRadius * 0.9f) // right base
    path.close()
    //
    // Draw on the canvas
    paint.style = Paint.Style.FILL
    paint.color = color.toArgb()
    canvas.drawPath(path, paint)
    //
    // return the bitmap
    return markerBitmap
}



