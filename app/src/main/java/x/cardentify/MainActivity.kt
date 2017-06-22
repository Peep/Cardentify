package x.cardentify

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.commonsware.cwac.cam2.CameraActivity
import com.commonsware.cwac.cam2.Facing
import com.commonsware.cwac.cam2.FocusMode
import com.commonsware.cwac.cam2.ZoomStyle

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val REQUEST_CAMERA : Int = 1337

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

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
