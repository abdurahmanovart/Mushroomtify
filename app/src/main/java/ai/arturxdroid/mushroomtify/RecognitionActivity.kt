package ai.arturxdroid.mushroomtify

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_recognition.*
import kotlinx.android.synthetic.main.mushroom_item.*
import kotlinx.coroutines.*

class RecognitionActivity : AppCompatActivity() {

    private lateinit var classifier: Classifier
    private val job = Job()
    private val coroutineScope = CoroutineScope(job)
    private lateinit var dialog:BottomSheetDialog
    private val currentRecognizedText: MutableLiveData<String> by lazy {
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
//            recognized_name_text_view.text = newName
        }
        currentRecognizedText.observe(this, textObserver)
        dialog = BottomSheetDialog(this)
        dialog.setContentView(R.layout.mushroom_item)
//        dialog.show()
    }

    private fun initClassification(imageBitmap: Bitmap) {
        var text: List<String>
        progress_bar.show()
        progress_bar.animate()
        coroutineScope.launch {
            text = predict(imageBitmap)
            delay(4000)
            withContext(Dispatchers.Main) {
                progress_layout.visibility = View.GONE
                progress_bar.hide()
                Log.i("FULL_INFO",text[0])
                dialog.mushroom_name_text_view.text = text[1]
                dialog.mushroom_edible_text_view.text = "edible"
                dialog.mushroom_edible_text_view.setTextColor(resources.getColor(R.color.colorPrimaryDark))
                dialog.show()
            }
        }
    }

    private fun predict(imageBitmap: Bitmap): List<String> {
        classifier = Classifier(Utils.assetFilePath(this, "bkp.pt"))
        val text =
            classifier.predict(imageBitmap, true) + "\n\n" + classifier.predict(imageBitmap)
        return text
    }
}

