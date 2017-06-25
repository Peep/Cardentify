package net.cardentify.app

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Trace
import android.util.Log
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.util.*

/**
 * Created by Tora on 6/25/2017.
 */
class CarModelPredictorInteractor constructor(assets: AssetManager) {
    data class SimilaritiesResult(val similarities: FloatArray, val names: Collection<String>)

    val VECTOR_SIZE = 256
    val INPUT_IMAGES_NAME = "InputImages"
    val INPUT_VECTORS_NAME = "InputVectors"
    val SORTED_SIMILARITIES_NAME = "SortedSimilarities"
    val SORTED_SIMILARITIES_INDICES_NAME = "SortedSimilarities:1"
    val MODEL_PATH = "file:///android_asset/model.pb"

    val inferenceInterface = TensorFlowInferenceInterface(assets, MODEL_PATH)

    var vectorFloats: FloatArray? = null
    val vectorCount: Int
        get() {
            val floats = vectorFloats!!
            return floats.size / VECTOR_SIZE
        }
    var carNames: List<String>? = null

    init {
        // Create some random vectors
        val count = 1000

        val testVectors = FloatArray(count * VECTOR_SIZE)
        val rng = Random()
        for(i in 0..testVectors.size-1) {
            testVectors[i] = 2.0f * (rng.nextFloat() - 0.5f)
        }

        vectorFloats = testVectors

        carNames = (0..count-1).map{i -> "Car_" + i.toString()}
    }

    fun getSimilarities(bitmap: Bitmap): SimilaritiesResult {
        val vectorFloats = vectorFloats!!

        val pixelCount = bitmap.width * bitmap.height

        // Allocate the image buffers
        // TODO: Move these to where camera photo size is known and allocate only once
        val imageInts = IntArray(pixelCount) // Each pixel stored as 32 bit int
        val imageFloats = FloatArray(pixelCount * 3) // RGB

        val convTime = System.currentTimeMillis()

        // Load the bitmap pixels into the integer array
        bitmap.getPixels(imageInts, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        // Convert integers to floats in range -1..1
        for(pixelIndex in 0..pixelCount-1) {
            val pixelValue = imageInts[pixelIndex]
            imageFloats[3 * pixelIndex + 0] =
                    2.0f * (((pixelValue shl 16) and 0xFF).toFloat() / 255.0f - 0.5f)
            imageFloats[3 * pixelIndex + 1] =
                    2.0f * (((pixelValue shl 8) and 0xFF).toFloat() / 255.0f - 0.5f)
            imageFloats[3 * pixelIndex + 2] =
                    2.0f * ((pixelValue and 0xFF).toFloat() / 255.0f - 0.5f)
        }

        Log.d("calculateImageVector", "Time taken for conversion: " +
                (System.currentTimeMillis() - convTime).toString() + "ms")

        val infTime = System.currentTimeMillis()

        // Copy image floats to TensorFlow
        inferenceInterface.feed(INPUT_IMAGES_NAME, imageFloats,
                1, bitmap.width.toLong(), bitmap.height.toLong(), 3)

        // Copy vector floats to TensorFlow
        inferenceInterface.feed(INPUT_VECTORS_NAME, vectorFloats,
                vectorCount.toLong(), VECTOR_SIZE.toLong())

        // Run TensorFlow calculations
        inferenceInterface.run(arrayOf(SORTED_SIMILARITIES_INDICES_NAME, SORTED_SIMILARITIES_NAME), false)

        // Get results
        val sortedSimilaritiesIndices = IntArray(vectorCount)
        val sortedSimilarities = FloatArray(vectorCount)

        inferenceInterface.fetch(SORTED_SIMILARITIES_INDICES_NAME, sortedSimilaritiesIndices)
        inferenceInterface.fetch(SORTED_SIMILARITIES_NAME, sortedSimilarities)

        Log.d("calculateImageVector", "Time taken for inference: " +
                (System.currentTimeMillis() - infTime).toString() + "ms")

        val similarities = sortedSimilarities.map { sim -> (sim - sortedSimilarities.min()!!) /
                (sortedSimilarities.max()!! - sortedSimilarities.min()!!) }.toFloatArray()

        val unsortedNames = carNames!!
        val sortedNames = sortedSimilaritiesIndices.map {i -> unsortedNames[i] }

        return SimilaritiesResult(similarities, sortedNames)
    }
}