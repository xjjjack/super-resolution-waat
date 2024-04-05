package com.example.super_resolution
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.ceil

class MainActivity : AppCompatActivity() {
    private lateinit var customImageView: CustomImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        customImageView = findViewById(R.id.customImageView)

        findViewById<Button>(R.id.buttonImage1).setOnClickListener {
            pickImage()
        }
    }

    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), 200)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
            customImageView.setUploadImage(bitmap)
            runPtModule(bitmap)
        }
    }


//    private fun runPtModule(img: Bitmap) {
//        try {
//            // load pytorch module
//            println("start to load module")
//            val module = Module.load(assetFilePath(this, "1234.pt"))
//            println("module loaded")
//            // set input tensor
//            val TORCHVISION_NORM_MEAN_RGB = floatArrayOf(0f, 0f, 0f)
//            val TORCHVISION_NORM_STD_RGB = floatArrayOf(1f, 1f, 1f)
//            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
//                img,
//                TORCHVISION_NORM_MEAN_RGB,
//                TORCHVISION_NORM_STD_RGB
//            )
//            // run the module
//            println("start to run module")
//            val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
//            println(outputTensor)
//            // get the output result
//            val outputImage = customImageView.tensorToBitmap(outputTensor)
//            customImageView.setResult(outputImage)
//        } catch (e: IOException) {
//            Log.e("PytorchHelloWorld", "Error reading assets", e)
//            finish()
//        }
//    }

    private fun runPtModule(img: Bitmap) {
        try {
            // load pytorch module
            val module = Module.load(assetFilePath(this, "bicubicpp.pt"))
            // set input tensor list
            //val inputImageTensorList = preprocessImage(img)
            //val outputImageList = mutableListOf<Bitmap>()
            //for (inputTensor in inputImageTensorList) {
            // 将缩放后的 Bitmap 转换为 PyTorch 的 Tensor
            val TORCHVISION_NORM_MEAN_RGB = floatArrayOf(0f, 0f, 0f)
            val TORCHVISION_NORM_STD_RGB = floatArrayOf(1f, 1f, 1f)
            val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                img,
                TORCHVISION_NORM_MEAN_RGB,
                TORCHVISION_NORM_STD_RGB
            )
            //val inputdata = inputTensor.getDataAsFloatArray()
            // run the module
            val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
            // get the output result
            val outputImage = customImageView.tensorToBitmap(outputTensor)
            //outputImageList.add(outputImage)
            //}
            //val outputBitmap = customImageView.mergeBitmaps(outputImageList,img.width,img.height,3)
            customImageView.setResult(outputImage)

        } catch (e: IOException) {
            Log.e("PytorchHelloWorld", "Error reading assets", e)
            finish()
        }
    }

    // slice + merge images (runPtModule, preprocessImage)

    //    private fun runPtModule(img: Bitmap) {
//        try {
//            // load pytorch module
//            val module = Module.load(assetFilePath(this, "fast.pt"))
//            // set input tensor list
//            val inputImageTensorList = preprocessImage(img)
//            val outputImageList = mutableListOf<Bitmap>()
//            for (inputTensor in inputImageTensorList) {
//                val inputdata = inputTensor.getDataAsFloatArray()
//                // run the module
//                val outputTensor = module.forward(IValue.from(inputTensor)).toTensor()
//                // get the output result
//                val outputImage = customImageView.tensorToBitmap(outputTensor)
//                outputImageList.add(outputImage)
//            }
//            val outputBitmap = customImageView.mergeBitmaps(outputImageList,img.width,img.height,3)
//            customImageView.setResult(outputBitmap)
//
//
//        } catch (e: IOException) {
//            Log.e("PytorchHelloWorld", "Error reading assets", e)
//            finish()
//        }
//    }
    fun preprocessImage(bitmap: Bitmap): List<Tensor> {
        val slices = mutableListOf<Tensor>()
        val imageSize = Pair(bitmap.height, bitmap.width)

        val xSlices = Math.ceil(bitmap.width.toDouble() / 96).toInt()
        val ySlices = Math.ceil(bitmap.height.toDouble() / 96).toInt()

        for (i in 0 until ySlices) {
            for (j in 0 until xSlices) {
                val left = j * 96
                val top = i * 96
                val right = Math.min((j + 1) * 96, bitmap.width)
                val bottom = Math.min((i + 1) * 96, bitmap.height)
                val sliceBitmap = Bitmap.createBitmap(bitmap, left, top, right - left, bottom - top)

                // 将缩放后的 Bitmap 转换为 PyTorch 的 Tensor
                val TORCHVISION_NORM_MEAN_RGB = floatArrayOf(0f, 0f, 0f)
                val TORCHVISION_NORM_STD_RGB = floatArrayOf(1f, 1f, 1f)
                val inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                    sliceBitmap,
                    TORCHVISION_NORM_MEAN_RGB,
                    TORCHVISION_NORM_STD_RGB
                )
                slices.add(inputTensor)
            }
        }
        return slices
    }

//    fun scaleBitmap(bitmap: Bitmap): Bitmap {
//        val scaledBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, bitmap.config)
//        for (x in 0 until bitmap.width) {
//            for (y in 0 until bitmap.height) {
//                val pixel = bitmap.getPixel(x, y)
//                val red = Color.red(pixel) / 255f
//                val green = Color.green(pixel) / 255f
//                val blue = Color.blue(pixel) / 255f
//                val scaledPixel = Color.rgb((red * 255).toInt(), (green * 255).toInt(), (blue * 255).toInt())
//                scaledBitmap.setPixel(x, y, scaledPixel)
//            }
//        }
//        return scaledBitmap
//    }
    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)

        try {
            val inpStream: InputStream = context.assets.open(assetName)
            try {
                val outStream = FileOutputStream(file, false)
                val buffer = ByteArray(4 * 1024)
                var read: Int

                while (true) {
                    read = inpStream.read(buffer)
                    if (read == -1) {
                        break
                    }
                    outStream.write(buffer, 0, read)
                }
                outStream.flush()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}


//package com.example.super_resolution
//import android.content.Context
//import android.content.Intent
//import android.graphics.Bitmap
//import android.graphics.Canvas
//import android.graphics.Color
//import android.graphics.Paint
//import android.graphics.Rect
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.util.AttributeSet
//import android.view.MotionEvent
//import android.view.View
//import android.widget.Button
//import android.widget.ImageView
//import androidx.appcompat.app.AppCompatActivity
//import com.example.super_resolution.R
//
//class MainActivity : AppCompatActivity() {
//    private val pickImageRequestCode = 1
//    private var imageUri: Uri? = null
//    private var secondImageUri: Uri? = null
//
//    private lateinit var imageComparisonView: ImageComparisonView
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        imageComparisonView = findViewById(R.id.imageCompareView)
//
//        val uploadButton: Button = findViewById(R.id.uploadButton)
//        uploadButton.setOnClickListener { openGalleryForImage() }
//
//        val secondUploadButton: Button = findViewById(R.id.recoverButton)
//        secondUploadButton.setOnClickListener { openGalleryForSecondImage() }
//    }
//
//    private fun openGalleryForImage() {
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
//        startActivityForResult(intent, pickImageRequestCode)
//    }
//
//    private fun openGalleryForSecondImage() {
//        // Different request code for the second image
//        val pickSecondImageRequestCode = 2
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
//        startActivityForResult(intent, pickSecondImageRequestCode)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == RESULT_OK) {
//            when (requestCode) {
//                pickImageRequestCode -> {
//                    imageUri = data?.data
//                    imageUri?.let { uri ->
//                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
//                        imageComparisonView.setInitialImage(bitmap)
//                    }
//                }
//                2 -> { // Request code for the second image
//                    secondImageUri = data?.data
//                    secondImageUri?.let { uri ->
//                        val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
//                        imageComparisonView.setRecoveredImage(bitmap)
//                    }
//                }
//            }
//        }
//    }
//
//}
//
