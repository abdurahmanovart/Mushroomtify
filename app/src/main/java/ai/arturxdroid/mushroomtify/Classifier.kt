package ai.arturxdroid.mushroomtify

import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils

class Classifier(modelPath: String) {

    var model = Module.load(modelPath)
    val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
    val std = floatArrayOf(0.229f, 0.224f, 0.225f)
    val size = 512

    private fun preProcess(bitmap: Bitmap, size: Int): Tensor =
        TensorImageUtils.bitmapToFloat32Tensor(
            Bitmap.createScaledBitmap(bitmap, size, size, false), mean, std
        )

    private fun argMax(array: FloatArray): ArrayList<Int> {
        val copy = array.copyOf()
        copy.sort()
        val result = ArrayList<Int>(3)

        for (i in copy.takeLast(3).reversed()) {
            result.add(array.indexOf(i))
        }
        return result
    }

    private fun fullArgMax(array: FloatArray): String {
        val copy = array.copyOf()
        var result = ""

        for (i in copy.indices) {
            result += Constants.LABELS_LIST[i] + ":" + copy[i] + "\n"
        }

        return result
    }

    fun predict(
        bitmap: Bitmap,
        debug: Boolean = false
    ): ArrayList<Int> {

        val bmpOrig = Bitmap.createScaledBitmap(bitmap, size, size, false)
        val bmp90 = bmpOrig.rotate(90f)
        val bmp180 = bmpOrig.rotate(180f)
        val bmp270 = bmpOrig.rotate(270f)

        val tensorOrig = preProcess(bmpOrig, size)
        val tensor90 = preProcess(bmp90, size)
        val tensor180 = preProcess(bmp180, size)
        val tensor270 = preProcess(bmp270, size)

        val inputsOrig = IValue.from(tensorOrig)
        val outputOrig = model.forward(inputsOrig).toTensor()
        val scoreOrig = outputOrig.dataAsFloatArray.asList()

        val inputs90 = IValue.from(tensor90)
        val output90 = model.forward(inputs90).toTensor()
        val score90 = output90.dataAsFloatArray.asList().map { (it * 0.9).toFloat() }

        val inputs180 = IValue.from(tensor180)
        val output180 = model.forward(inputs180).toTensor()
        val score180 = output180.dataAsFloatArray.asList().map { (it * 0.9).toFloat() }

        val inputs270 = IValue.from(tensor270)
        val output270 = model.forward(inputs270).toTensor()
        val score270 = output270.dataAsFloatArray.asList().map { (it * 0.9).toFloat() }

        var scores = scoreOrig.zip(score90).map { (a, b) -> a + b }
        scores = scores.zip(score180).map { (a, b) -> a + b }
        scores = scores.zip(score270).map { (a, b) -> a + b }
        val finalScores = scores.map { it / 4f }.toFloatArray()

        val classIndexes = argMax(finalScores)
        if (debug) {
            Log.i("CLASSIFYTAG", arrayListOf(fullArgMax(finalScores)).toString())
        }
        return classIndexes

    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}