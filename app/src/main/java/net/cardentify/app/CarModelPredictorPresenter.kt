package net.cardentify.app

import android.graphics.Bitmap
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Tora on 6/25/2017.
 */
class CarModelPredictorPresenter constructor(val activity: CarModelPredictorActivity) {
    val predictor = CarModelPredictorInteractor(activity.assets)

    fun onCameraResult(bitmap: Bitmap?) {
        val bm = bitmap
        when (bm){
            is Bitmap -> {
                Observable
                    .just(1)
                    .subscribeOn(Schedulers.computation())
                    .map { return@map predictor.getSimilarities(bm) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { (similarities, names) ->
                        activity.setCarModelItems(names, similarities)
                    }
            }
            null -> {
                Log.e("onCameraResult", "bitmap was null")
            }
        }
    }
}