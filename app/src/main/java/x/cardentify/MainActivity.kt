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

class MainActivity : AppCompatActivity() {

    val REQUEST_CAMERA: Int = 1337

    val VECTOR_SIZE = 256
    val IMAGE_WIDTH = 224
    val IMAGE_HEIGHT = 224
    val INPUT_NAME = "InputImages"
    val OUTPUT_NAMES = arrayOf("network_3/ImageVectors")
    val MODEL_PATH = "file:///android_asset/model.pb"

    val imageVector = FloatArray(VECTOR_SIZE)

    var inferenceInterface: TensorFlowInferenceInterface? = null

    override fun onResume() {
        super.onResume()
    }

    fun calculateImageVector(bitmap: Bitmap) {
        Trace.beginSection("calculateImageVector")
        val pixelCount = bitmap.width * bitmap.height

        // Allocate the image buffers
        // TODO: Move these to where camera photo size is known and allocate only once
        val imageInts = IntArray(pixelCount) // Each pixel stored as 32 bit int
        val imageFloats = FloatArray(pixelCount * 3) // RGB

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

        // Copy image floats to TensorFlow
        inferenceInterface!!.feed(INPUT_NAME, imageFloats, 1, bitmap.width.toLong(),
                bitmap.height.toLong(), 3)

        // Run TensorFlow calculations
        inferenceInterface!!.run(OUTPUT_NAMES, false)

        // Get results
        inferenceInterface!!.fetch(OUTPUT_NAMES[0], imageVector)
        Trace.endSection()

        var vectorString = ""
        for(f in imageVector) {
            vectorString += f.toString() + ", "
        }

        Log.d("calculateImageVector", "Calculated vector: " + vectorString)
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        Trace.beginSection("CreateInferenceInterface")
        inferenceInterface = TensorFlowInferenceInterface(assets, MODEL_PATH)
        Trace.endSection()

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
                    val resizedBitmap = Bitmap.createScaledBitmap(
                            bitmap, IMAGE_WIDTH, IMAGE_HEIGHT, false)
                    calculateImageVector(resizedBitmap)
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
