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

    private fun showModelSelectionDialog(bitmap: Bitmap) {
        val models = arrayOf("bicubicpp", "esrt", "new model")
        AlertDialog.Builder(this)
            .setTitle("Select a Model")
            .setItems(models) { dialog, which ->

                when (which) {
                    0 -> useModel(bitmap,"A")
                    1 -> useModel(bitmap,"B")
                    2 -> useModel(bitmap,"C")
                    else -> Log.e("MainActivity", "Unknown model selected")
                }
            }
            .show()
    }

    private fun useModel(bitmap: Bitmap, modelId: String) {
        when (modelId) {
            "A" -> runPtModule(bitmap, "bicubicpp.pt")
            "B" -> runPtModule(bitmap, "esrt.pt")
            "C" -> runPtModule(bitmap, "zsznbbest.pt")
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
            showModelSelectionDialog(bitmap)
//            runPtModule(bitmap)
        }
    }


    private fun runPtModule(img: Bitmap, modelId: String) {
        try {
            // load pytorch module
            val module = Module.load(assetFilePath(this, modelId))
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
