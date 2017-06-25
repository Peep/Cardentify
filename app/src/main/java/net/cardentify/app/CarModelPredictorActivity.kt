package net.cardentify.app

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import com.commonsware.cwac.cam2.CameraActivity
import com.commonsware.cwac.cam2.Facing
import com.commonsware.cwac.cam2.FocusMode
import com.commonsware.cwac.cam2.ZoomStyle
import kotlinx.android.synthetic.main.activity_car_model_predictor.fab_camera
import kotlinx.android.synthetic.main.activity_car_model_predictor.toolbar

class CarModelPredictorActivity : AppCompatActivity() {
    val REQUEST_CAMERA: Int = 1337

    var presenter: CarModelPredictorPresenter? = null

    var carModelAdapter: CarModelListAdapter? = null

    // A dialog showing that something is in progress
    // Use showProgress(title) and hideProgress()
    private var progressDialog: ProgressDialog? = null

    override fun onResume() {
        super.onResume()
    }

    /**
     * Clears all items from the car model list
     */
    fun clearCarModelItems() {
        val adapter = carModelAdapter

        when(adapter) {
            is CarModelListAdapter -> {
                adapter.cars.clear()
                adapter.similarities.clear()

                adapter.notifyDataSetChanged()
            }
            null -> {
                Log.e("clearCarModelItems", "carModelAdapter is null")
            }
        }
    }

    /**
     * Clears all items from the car model list and populates it with
     * [carNames] and [similarities]
     * @param[carNames] List of names of cars
     * @param[similarities] List of similarities for the cars
     */
    fun setCarModelItems(carNames: Collection<String>, similarities: FloatArray) {
        val adapter = carModelAdapter

        when(adapter) {
            is CarModelListAdapter -> {
                adapter.cars.clear()
                adapter.cars.addAll(carNames)

                adapter.similarities.clear()
                adapter.similarities.addAll(similarities.asIterable())

                adapter.notifyDataSetChanged()
            }
            null -> {
                Log.e("setCarModelListItem", "carModelAdapter is null")
            }
        }
    }

    /**
     * Shows a progress overlay
     * @param[message] Message to display in the overlay
     */
    fun showProgress(message: String) {
        val dialog = progressDialog ?: ProgressDialog(this)
        dialog.setMessage(message)
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        dialog.isIndeterminate = true
        dialog.show()
        progressDialog = dialog
    }

    /**
     * Hides the progress overlay if any
     */
    fun hideProgress() {
        progressDialog?.hide()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_car_model_predictor)
        setSupportActionBar(toolbar)

        presenter = CarModelPredictorPresenter(this)

        // Setup the car model list adapter
        carModelAdapter = CarModelListAdapter(this)
        val carModelList = findViewById(R.id.car_model_list) as ListView
        carModelList.adapter = carModelAdapter

        fab_camera.setOnClickListener { view ->
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CAMERA)
            if (resultCode == Activity.RESULT_OK) {
                val bitmap : Bitmap? = data?.getParcelableExtra("data")

                val pres = presenter
                when(pres) {
                    is CarModelPredictorPresenter -> {
                        pres.onCameraResult(bitmap)
                    }
                    null -> {
                        Log.e("onActivityResult", "presenter is null on camera result")
                    }
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
