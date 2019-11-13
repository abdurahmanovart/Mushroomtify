package ai.arturxdroid.mushroomtify

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.activity_recognition.*
import kotlinx.coroutines.*

class RecognitionActivity : AppCompatActivity() {

    private lateinit var classifier: Classifier
    val job = Job()
    val coroutineScope = CoroutineScope(job)
    val currentRecognizedText: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recognition)
        initUI()
    }

    override fun onPause() {
        super.onPause()
        coroutineScope.cancel()
    }

    private fun initUI() {
        val uriString = intent.extras?.getString(EXTRA_IMAGE_URI)
        val imageBitmap =
            MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(uriString))
        mushroom_recognition_image_view.setImageBitmap(imageBitmap)
        initClassification(imageBitmap)
        val textObserver = Observer<String> { newName ->
            recognized_name_text_view.text = newName
        }
        currentRecognizedText.observe(this, textObserver)
    }

    private fun initClassification(imageBitmap: Bitmap) {
        var text: String
        progress_bar.show()
        progress_bar.animate()
        coroutineScope.launch {
            text = predict(imageBitmap)
            withContext(Dispatchers.Main) {
                progress_layout.visibility = View.GONE
                progress_bar.hide()
                currentRecognizedText.value = text
            }
        }
    }

    private fun predict(imageBitmap: Bitmap): String {
        classifier = Classifier(Utils.assetFilePath(this, "bkp.pt"))
        val text =
            classifier.predict(imageBitmap, true) + "\n\n" + classifier.predict(imageBitmap)
        return text
    }
}

