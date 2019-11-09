package ai.arturxdroid.mushroomtify

import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import kotlinx.android.synthetic.main.activity_recognition.*

class RecognitionActivity : AppCompatActivity() {

    private lateinit var classifier: Classifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognition)
        initUI()
    }

    private fun initUI() {
        val uriString = intent.extras?.getString(EXTRA_IMAGE_URI)
        val imageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(uriString))
        mushroom_recognition_image_view.setImageBitmap(imageBitmap)
        classifier = Classifier(Utils.assetFilePath(this,"last_jit_model.pt"))
        recognized_name_text_view.text = classifier.predict(imageBitmap)
    }



}
