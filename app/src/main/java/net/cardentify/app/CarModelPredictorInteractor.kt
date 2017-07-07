package net.cardentify.app

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Trace
import android.util.Log
import net.cardentify.data.CarListOuterClass
import org.tensorflow.contrib.android.TensorFlowInferenceInterface
import java.io.FileInputStream
import java.util.*

/**
 * Created by Tora on 6/25/2017.
 */
class CarModelPredictorInteractor constructor(assets: AssetManager) {
    data class SimilaritiesResult(val similarities: FloatArray, val names: Collection<String>)

    val VECTOR_SIZE = 256
    val INPUT_IMAGES_NAME = "InputImages"
    val INPUT_VECTORS_NAME = "InputVectors"
    val INPUT_LABELS_NAME = "InputLabels"
    val SORTED_SIMILARITIES_NAME = "SortedSimilarities"
    val SORTED_SIMILARITIES_INDICES_NAME = "SortedSimilarities:1"
    val MODEL_PATH = "file:///android_asset/model.pb"
    val VECTORS_PATH = "vectors.pb"

    val inferenceInterface = TensorFlowInferenceInterface(assets, MODEL_PATH)

    var carVectors: FloatArray
    var carLabels: IntArray
    val carCount: Int
        get() {
            return carVectors.size / VECTOR_SIZE
        }

    var carLabelNames: List<String>
    val carLabelCount: Int
        get() {
            return carLabelNames.size
        }

    init {
        val carList = CarListOuterClass.CarList.parseFrom(assets.open(VECTORS_PATH))

        val vectors = ArrayList<Float>()
        val labels = ArrayList<Int>()
        val labelNames = ArrayList<String>()

        for(car in carList.carsList) {
            // Check if we already know about this car
            if(!labelNames.contains(car.name)) {
                labelNames.add(car.name)
            }

            val carLabelIndex = labelNames.indexOf(car.name)

            for(vector in car.vectorsList) {
                if(vector.vectorCount == VECTOR_SIZE) {
                    vectors.addAll(vector.vectorList)
                    labels.add(carLabelIndex)
                } else {
                    Log.w("CarModelPredictorIntera",
                            "Vector has wrong size for car ${car.name}: ${vector.vectorCount}")
                }
            }
        }

        carVectors = vectors.toFloatArray()
        carLabels = labels.toIntArray()
        carLabelNames = labelNames
    }

    fun getSimilarities(bitmap: Bitmap): SimilaritiesResult {
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
                    2.0f * (((pixelValue shr 16) and 0xFF).toFloat() / 255.0f - 0.5f)
            imageFloats[3 * pixelIndex + 1] =
                    2.0f * (((pixelValue shr 8) and 0xFF).toFloat() / 255.0f - 0.5f)
            imageFloats[3 * pixelIndex + 2] =
                    2.0f * ((pixelValue and 0xFF).toFloat() / 255.0f - 0.5f)
        }

        Log.d("calculateImageVector", "Time taken for conversion: " +
                (System.currentTimeMillis() - convTime).toString() + "ms")

        val infTime = System.currentTimeMillis()

        // Copy image to TensorFlow
        inferenceInterface.feed(INPUT_IMAGES_NAME, imageFloats,
                1, bitmap.width.toLong(), bitmap.height.toLong(), 3)

        // Copy vectors to TensorFlow
        inferenceInterface.feed(INPUT_VECTORS_NAME, carVectors, carCount.toLong(),
                VECTOR_SIZE.toLong())

        // Copy labels to TensorFlow
        inferenceInterface.feed(INPUT_LABELS_NAME, carLabels, carCount.toLong())

        // Run TensorFlow calculations
        inferenceInterface.run(arrayOf(SORTED_SIMILARITIES_INDICES_NAME, SORTED_SIMILARITIES_NAME), false)

        // Get results
        val sortedSimilaritiesIndices = IntArray(carLabelCount)
        val sortedSimilarities = FloatArray(carLabelCount)

        inferenceInterface.fetch(SORTED_SIMILARITIES_INDICES_NAME, sortedSimilaritiesIndices)
        inferenceInterface.fetch(SORTED_SIMILARITIES_NAME, sortedSimilarities)

        Log.d("calculateImageVector", "Time taken for inference: " +
                (System.currentTimeMillis() - infTime).toString() + "ms")

        // Map similarities to 0..1 while avoiding division by zero
        val simMin = sortedSimilarities.min()!!
        val simRange = sortedSimilarities.max()!! - simMin
        val normalizedSortedSimilarities = sortedSimilarities.map { sim ->
            (sim - simMin) / if(simRange == 0.0f) 1.0f else simRange
        }.toFloatArray()

        return SimilaritiesResult(normalizedSortedSimilarities, sortedSimilaritiesIndices.map {
            labelIndex -> carLabelNames[labelIndex]
        })
    }
}