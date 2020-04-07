package com.loodos.tensorflowexample.screen

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.camera.core.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.loodos.tensorflowexample.R
import com.loodos.tensorflowexample.util.FileUtil
import java.io.ByteArrayOutputStream
import java.io.File

// Manually give the permission
class ScannerFragment : Fragment() {

    private lateinit var preview: Preview

    private lateinit var takePictureButton: Button

    private lateinit var imgPreview: ImageView

    private lateinit var imgBtnFLash: ImageButton

    private lateinit var textureView: TextureView

    private lateinit var txtOcrText: TextView

    private var mCommunication: ParentChildCommunication? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mCommunication = (context as? ParentChildCommunication)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_scanner, container, false)
        init(rootView)
        startCamera()
        return rootView
    }

    private fun init(v: View) {
        v.apply {
            textureView = findViewById(R.id.camera_texture_view)
            imgPreview = findViewById(R.id.image_preview)
            takePictureButton = findViewById(R.id.btn_take_picture)
            txtOcrText = findViewById(R.id.txt_ocr_text)
            imgBtnFLash = findViewById(R.id.img_btn_flash);
        }
    }

    private fun startCamera() {
        val previewConfig = PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.BACK)
                .build()

        preview = Preview(previewConfig).also {
            it.setOnPreviewOutputUpdateListener { previewOutput ->
                textureView.surfaceTexture = previewOutput.surfaceTexture
            }
        }

        val imageAnalysisConfig = ImageAnalysisConfig.Builder()
                .build()
        val imageAnalysis = ImageAnalysis(imageAnalysisConfig)

        CameraX.bindToLifecycle(this as LifecycleOwner, preview, imageAnalysis, buildImageCaptureUseCase())
    }

    private fun convert(bitmap: Bitmap) {
        mCommunication?.processImage(bitmap)
    }

    private fun buildImageCaptureUseCase(): ImageCapture {
        val config = ImageCaptureConfig.Builder()
                .setFlashMode(FlashMode.OFF)
                .build()

        val imageCapture = ImageCapture(config)

        takePictureButton.setOnClickListener {
            // Create temporary file
            val tempImageFile = createTempFile(FileUtil.getFileName(), FileUtil.getImageFileFormat())
            // Store captured image in the temporary file
            imageCapture.takePicture(tempImageFile, object : ImageCapture.OnImageSavedListener {
                override fun onImageSaved(file: File) {
                    // You may display the image for example using its path file.absolutePath
                    val filePath = file.path
                    val bitmap = BitmapFactory.decodeFile(filePath)
                    val a = bitmap.convertToByteArray()
                    val b: Bitmap = BitmapFactory.decodeByteArray(a, 0, a.size)
                    val c = Bitmap.createScaledBitmap(b, 28, 28, false)
                    imgPreview.setImageBitmap(bitmap)
                    convert(c)
                }

                override fun onError(useCaseError: ImageCapture.ImageCaptureError, message: String, cause: Throwable?) {

                }
            })
        }

        return imageCapture
    }

    private fun Bitmap.convertToByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.JPEG, 50, stream)
        return stream.toByteArray()
    }
}