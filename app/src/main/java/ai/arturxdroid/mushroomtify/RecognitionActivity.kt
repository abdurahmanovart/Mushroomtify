package ai.arturxdroid.mushroomtify

import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.activity_recognition.*
import kotlinx.android.synthetic.main.mushroom_item.view.*
import kotlinx.coroutines.*

class RecognitionActivity : AppCompatActivity() {

    private lateinit var classifier: Classifier
    private val job = Job()
    private val coroutineScope = CoroutineScope(job)
    private lateinit var dialog: BottomSheetDialog
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
        dialog = BottomSheetDialog(this)
    }

    private fun initClassification(imageBitmap: Bitmap) {
        var indexes: List<Int>
        progress_bar.show()
        progress_bar.animate()
        coroutineScope.launch {
            indexes = predict(imageBitmap)
            delay(4000)
            withContext(Dispatchers.Main) {
                progress_bar.hide()
                progress_layout.visibility = View.INVISIBLE
                show_results_button.isEnabled = true
                prepareDialog(indexes, dialog)
            }
        }
    }

    private fun prepareDialog(indexes: List<Int>, dialog: BottomSheetDialog) {
        val dialogView = LayoutInflater.from(this).inflate(
            R.layout.recognized_dialog_layout,
            null
        ) as LinearLayout

        val firstMushroomView = LayoutInflater.from(this).inflate(R.layout.mushroom_item, null)
        val secondMushroomView = LayoutInflater.from(this).inflate(R.layout.mushroom_item, null)
        val thirdMushroomView = LayoutInflater.from(this).inflate(R.layout.mushroom_item, null)

        val localNames = resources.getStringArray(R.array.mushroom_names_array)
        val edibility = resources.getStringArray(R.array.mushroom_edibility_array)

        val itemViews = arrayListOf(firstMushroomView, secondMushroomView, thirdMushroomView)
        for (viewIndex in 0..2) {
            val view = itemViews[viewIndex]
            view.mushroom_name_text_view?.text = Constants.LABELS_LIST[indexes[viewIndex]]
            view.mushroom_edibility?.text = edibility[indexes[viewIndex]]
            view.mushroom_local_name?.text = localNames[indexes[viewIndex]]
            view.mushroom_edibility?.setTextColor(getEdibilityColor(view.mushroom_edibility.text.toString()))
            view.mushroom_icon?.setImageResource(getMushroomIcon(view.mushroom_edibility.text.toString()))
        }

        dialogView.addView(firstMushroomView, 1)
        dialogView.addView(secondMushroomView, 3)
        dialogView.addView(thirdMushroomView, 4)

        show_results_button.setOnClickListener { dialog.show() }

        dialog.setContentView(dialogView)
        dialog.show()
    }

    private fun getMushroomIcon(type: String): Int {
        return when (type.toLowerCase()) {
            "edible" -> R.drawable.good_mushroom
            else -> R.drawable.bad_mushroom
        }

    }

    private fun getEdibilityColor(edibility: String): Int {
        return when (edibility.toLowerCase()) {
            "edible" -> getColor(android.R.color.holo_green_dark)
            "inedible" -> getColor(androidx.appcompat.R.color.material_grey_600)
            else -> getColor(android.R.color.holo_red_dark)
        }
    }

    private fun predict(imageBitmap: Bitmap): List<Int> {
        classifier = Classifier(Utils.assetFilePath(this, "bkp.pt"))
        val text =
            classifier.predict(imageBitmap, true)
        return text
    }
}

