package ai.arturxdroid.mushroomtify

import android.graphics.Bitmap
import android.graphics.Matrix
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils

class Classifier(modelPath: String) {

    var model = Module.load(modelPath)
    val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
    val std = floatArrayOf(0.229f, 0.224f, 0.225f)

    private fun preProcess(bitmap: Bitmap, size: Int): Tensor =
        TensorImageUtils.bitmapToFloat32Tensor(
            Bitmap.createScaledBitmap(bitmap, size, size, false), mean, std
        )

    private fun argMax(array: FloatArray): Int {
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
        val firstIndexes = IntArray(5)
        var result = ""
        for (i in firstFives.indices) {
            firstIndexes[i] = copy.indexOf(firstFives[i])
            result += Constants.LABELS_LIST[firstIndexes[i]] + ":" + firstFives[i] + "\n"
        }

        return result
    }

    private fun fullArgMax(array: FloatArray):String{
        val copy = array.copyOf()
        var result = ""
        for (i in copy.indices){
            result+= Constants.LABELS_LIST[i] +":"+copy[i]+"\n"
        }
        return result

    }

    fun predict(bitmap: Bitmap, full_info: Boolean = false): String {

        val bmpOrig = Bitmap.createScaledBitmap(bitmap, 256, 256, false)
        val bmp90 = bmpOrig.rotate(90f)
        val bmp180 = bmpOrig.rotate(180f)
        val bmp270 = bmpOrig.rotate(270f)


        val tensorOrig = preProcess(bmpOrig, 256)
        val tensor90 = preProcess(bmp90, 256)
        val tensor180 = preProcess(bmp180, 256)
        val tensor270 = preProcess(bmp270, 256)

        val inputsOrig = IValue.from(tensorOrig)
        val outputOrig = model.forward(inputsOrig).toTensor()
        val scorOrig = outputOrig.dataAsFloatArray.asList()

        val inputs90 = IValue.from(tensor90)
        val output90 = model.forward(inputs90).toTensor()
        val scor90 = output90.dataAsFloatArray.asList().map { (it * 0.8).toFloat() }

        val inputs180 = IValue.from(tensor180)
        val output180 = model.forward(inputs180).toTensor()
        val scror180 = output180.dataAsFloatArray.asList().map { (it * 0.8).toFloat() }

        val inputs270 = IValue.from(tensor270)
        val output270 = model.forward(inputs270).toTensor()
        val scror270 = output270.dataAsFloatArray.asList().map { (it * 0.8).toFloat() }

        var scores = scorOrig.zip(scor90).map { (a, b) -> a + b }
        scores = scores.zip(scror180).map { (a, b) -> a + b }
        scores = scores.zip(scror270).map { (a, b) -> a + b }
        val finalScores = scores.map { it/4f }.toFloatArray()

        val classIndex = argMax(finalScores)
        if (!full_info)
            return Constants.LABELS_LIST[classIndex]
        return fullArgMax(finalScores)

    }

    fun Bitmap.rotate(degrees: Float): Bitmap {
        val matrix = Matrix().apply { postRotate(degrees) }
        return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
    }
}