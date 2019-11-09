package ai.arturxdroid.mushroomtify

import android.graphics.Bitmap
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils

class Classifier(modelPath: String) {

    lateinit var model: Module
    val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
    val std = floatArrayOf(0.229f, 0.224f, 0.225f)

    init {
        model = Module.load(modelPath)
    }

    public fun preprocess(bitmap: Bitmap, size: Int): Tensor =
        TensorImageUtils.bitmapToFloat32Tensor(
            Bitmap.createScaledBitmap(bitmap, size, size, false), mean, std
        )

    public fun argMax(array: FloatArray): Int {

        var maxIndex = -1
        var maxValue = 0f

        for (i in array.indices) {
            if (array[i] > maxValue) {
                maxIndex = i
                maxValue = array[i]
            }
        }
        return maxIndex
    }

    public fun predict(bitmap: Bitmap):String{

        val tensor = preprocess(bitmap,256)

        val inputs = IValue.from(tensor)
        val outputs = model.forward(inputs).toTensor()
        val scores = outputs.dataAsFloatArray

        val classIndex = argMax(scores)

        return Constants.LABELS_LIST[classIndex]

    }
}