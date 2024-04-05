package com.example.super_resolution
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.pytorch.Tensor
import android.graphics.Bitmap
import android.graphics.Bitmap.createBitmap
import android.graphics.Color
import kotlin.math.ceil

class CustomImageView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var resImage: Bitmap? = null
    private var originalImage: Bitmap? = null
    private var dividerY: Float = 0f
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val win_width = width
        val win_height = height
        dividerY = if (dividerY == 0f) height / 2f else dividerY

        resImage?.let {
            val rotatedBitmap = rotateBitmap(it, 90f)
            val scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, win_width, win_height, true)

            val srcRect = Rect(0, 0, width, dividerY.toInt())
            val dstRect = Rect(0, 0, width, dividerY.toInt())

            canvas.drawBitmap(scaledBitmap, srcRect, dstRect, null)
        }

        originalImage?.let {
            val rotatedBitmap = rotateBitmap(it, 90f)
            val scaledBitmap = Bitmap.createScaledBitmap(rotatedBitmap, win_width, win_height, true)

            val srcRect = Rect(0, dividerY.toInt(), width, height)
            val dstRect = Rect(0, dividerY.toInt(), width, height)

            canvas.drawBitmap(scaledBitmap, srcRect, dstRect, null)
        }

        val paint = Paint().apply {
            color = Color.BLACK
            strokeWidth = 5f
        }
        canvas.drawLine(0f, dividerY, width.toFloat(), dividerY, paint)
    }

    fun mergeBitmaps(slices: MutableList<Bitmap>, width:Int, height:Int, upscaleFactor: Int): Bitmap {
        val upscaledWidth = (width * upscaleFactor).toInt()
        val upscaledHeight = (height * upscaleFactor).toInt()

        // Assuming all slices have the same dimensions and bitmap config
        val sliceConfig = slices.first().config
        val upscaledImage = Bitmap.createBitmap(upscaledWidth, upscaledHeight, sliceConfig)

        val xSlices = ceil(upscaledWidth / (96.0 * upscaleFactor)).toInt()
        val ySlices = ceil(upscaledHeight / (96.0 * upscaleFactor)).toInt()

        var currentSliceIndex = 0
        for (i in 0 until ySlices) {
            for (j in 0 until xSlices) {
                if (currentSliceIndex < slices.size) {
                    val top = (i * 96 * upscaleFactor).toInt()
                    val left = (j * 96 * upscaleFactor).toInt()
                    val slice = slices[currentSliceIndex]

                    // Calculate the dimensions to place the slice correctly
                    val sliceWidth = slice.width
                    val sliceHeight = slice.height

                    // Create a temporary bitmap if necessary to adjust the slice dimensions
                    val tempSlice = if (sliceWidth + left > upscaledWidth || sliceHeight + top > upscaledHeight) {
                        Bitmap.createScaledBitmap(slice, upscaledWidth - left, upscaledHeight - top, false)
                    } else {
                        slice
                    }

                    for (y in 0 until tempSlice.height) {
                        for (x in 0 until tempSlice.width) {
                            val pixel = tempSlice.getPixel(x, y)
                            upscaledImage.setPixel(left + x, top + y, pixel)
                        }
                    }
                    currentSliceIndex++
                }
            }
        }

        return upscaledImage
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event ?: return false

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                dividerY = event.y
                invalidate()
            }
        }

        return true
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    fun setResult(bitmap: Bitmap) {
        resImage = bitmap
        invalidate()
    }

    fun setUploadImage(bitmap: Bitmap) {
        originalImage = bitmap
        invalidate()
    }

    fun getResImage(): Bitmap? {
        // get the result image from the model
        // waiting for shanzhi to implement
        return resImage
    }

    fun transferInputImage(img: Bitmap): Bitmap {
        // 获取 Bitmap 图像的宽度和高度
        val width = img.width
        val height = img.height

// 创建一个新的 Bitmap 用于存储结果
        val resultBitmap = Bitmap.createBitmap(width, height, img.config)

// 遍历图像的每个像素
        for (y in 0 until height) {
            for (x in 0 until width) {
                // 获取当前像素的颜色值
                val pixel = img.getPixel(x, y)

                // 提取 RGB 参数
                val r = Color.red(pixel) / 255f
                val g = Color.green(pixel) / 255f
                val b = Color.blue(pixel) / 255f

                // 计算新的颜色值并设置到结果图像中
                val newPixel = Color.rgb((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
                resultBitmap.setPixel(x, y, newPixel)
            }
        }
        return resultBitmap
    }


    fun tensorToBitmap(outputTensor: Tensor): Bitmap {
        val width = outputTensor.shape()[3].toInt() // width
        val height = outputTensor.shape()[2].toInt() // height
        val pixels = IntArray(width * height)

        // get tensor
        val outputData = outputTensor.getDataAsFloatArray()

        // change to bitmap
        for (y in 0 until height) {
            for (x in 0 until width) {
                val index = y * width + x
                val r = (outputData[index] * 255).toInt().coerceIn(0, 255)
                val g = (outputData[width * height + index] * 255).toInt().coerceIn(0, 255)
                val b = (outputData[2 * width * height + index] * 255).toInt().coerceIn(0, 255)
                pixels[index] = Color.rgb(r, g, b)
            }
        }

        // createBitmap
        return createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888)
    }
}
