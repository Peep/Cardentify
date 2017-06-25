package     net.cardentify.app

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ProgressBar
import android.widget.TextView

/**
 * Created by Tora on 6/24/2017.
 */

class CarModelListAdapter constructor(val context: Context) : BaseAdapter() {
    private val _cars = mutableListOf<String>()
    val cars: MutableList<String>
        get() = _cars

    private val _similarities = mutableListOf<Float>()
    val similarities: MutableList<Float>
        get() = _similarities

    override fun getCount(): Int {
        return cars.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return cars[position]
    }

    override fun getView(position: Int, convertView: View?, container: ViewGroup?): View {
        val convView = when(convertView) {
            is View -> convertView
            else -> {
                val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                inflater.inflate(R.layout.car_model_list_item, container, false)
            }
        }

        val textView = convView.findViewById(R.id.car_model_text) as TextView
        textView.text = cars[position]

        val progressBar = convView.findViewById(R.id.car_model_bar) as ProgressBar
        progressBar.progress = ((similarities[position]) * 100).toInt()

        return convView
    }
}