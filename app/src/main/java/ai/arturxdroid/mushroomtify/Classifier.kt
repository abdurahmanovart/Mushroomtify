package ai.arturxdroid.mushroomtify

import android.graphics.Bitmap
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils

class Classifier(modelPath: String) {

    var model = Module.load(modelPath)
    val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
    val std = floatArrayOf(0.229f, 0.224f, 0.225f)

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

    private fun fiveArgMax(array: FloatArray): String {
        val copy = array.copyOf()
        val sortedCopy = array.copyOf()
        sortedCopy.sort()
        val firstFives = sortedCopy.takeLast(5)
        val firtsIndexes = IntArray(5)
        var result = ""
        for (i in firstFives.indices) {
            firtsIndexes[i] = copy.indexOf(firstFives[i])
            result += Constants.LABELS_LIST[firtsIndexes[i]] + ":" + firstFives[i]+"\n"
        }

        return result
    }

    public fun predict(bitmap: Bitmap, full_info: Boolean = false): String {

        val tensor = preprocess(bitmap, 256)

        val inputs = IValue.from(tensor)
        val outputs = model.forward(inputs).toTensor()
        val scores = outputs.dataAsFloatArray

        val classIndex = argMax(scores)
        if (!full_info)
            return Constants.LABELS_LIST[classIndex]
        return fiveArgMax(scores)

    }
}