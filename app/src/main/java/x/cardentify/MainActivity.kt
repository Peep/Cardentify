package x.cardentify

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Trace
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.commonsware.cwac.cam2.CameraActivity
import com.commonsware.cwac.cam2.Facing
import com.commonsware.cwac.cam2.FocusMode
import com.commonsware.cwac.cam2.ZoomStyle
import org.tensorflow.contrib.android.TensorFlowInferenceInterface

import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val REQUEST_CAMERA: Int = 1337

    val VECTOR_SIZE = 256
    val INPUT_IMAGES_NAME = "InputImages"
    val INPUT_VECTORS_NAME = "InputVectors"
    val SORTED_SIMILARITIES_NAME = "SortedSimilarities"
    val SORTED_SIMILARITIES_INDICES_NAME = "SortedSimilarities:1"
    val MODEL_PATH = "file:///android_asset/model.pb"

    var vectorFloats: FloatArray? = null

    val vectorCount: Int
        get() {
            val floats = vectorFloats!!
            return floats.size / VECTOR_SIZE
        }

    var inferenceInterface: TensorFlowInferenceInterface? = null

    override fun onResume() {
        super.onResume()
    }

    fun loadVectors() {
        // Create some random vectors
        val testVectors = FloatArray(1000 * VECTOR_SIZE)
        val rng = Random()
        for(i in 0..testVectors.size-1) {
            testVectors[i] = 2.0f * (rng.nextFloat() - 0.5f)
        }

        vectorFloats = testVectors
    }

    fun calculateImageVector(bitmap: Bitmap) {
        Trace.beginSection("calculateImageVector")

        val vectorFloats = vectorFloats!!
        val inferenceInterface = inferenceInterface!!

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

        Log.d("calculateImageVector", "Sorted similarities indices: " + sortedSimilaritiesIndices.joinToString())
        Log.d("calculateImageVector", "Sorted similarities: " + sortedSimilarities.joinToString())

        Trace.endSection()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        Trace.beginSection("CreateInferenceInterface")
        inferenceInterface = TensorFlowInferenceInterface(assets, MODEL_PATH)
        Trace.endSection()

        loadVectors()

        fabCamera.setOnClickListener { view ->

            val intent : Intent = CameraActivity.IntentBuilder(view.context)
                .skipConfirm()
                .facing(Facing.BACK)
                .facingExactMatch()
                .focusMode(FocusMode.CONTINUOUS)
                .debug()
                .zoomStyle(ZoomStyle.PINCH)
                .build()

            startActivityForResult(intent, REQUEST_CAMERA)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CAMERA)
            if (resultCode == Activity.RESULT_OK) {
                val bitmap : Bitmap? = data?.getParcelableExtra("data")

                Log.d("Main", bitmap.toString())

                if(bitmap != null) {
                    calculateImageVector(bitmap)
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
