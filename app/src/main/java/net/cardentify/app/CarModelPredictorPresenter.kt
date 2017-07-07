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
    lateinit var predictor: CarModelPredictorInteractor

    init {
        activity.showProgress("Loading data")
        Observable
            .just(1)
            .subscribeOn(Schedulers.io())
            .map {
                return@map CarModelPredictorInteractor(activity.assets)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { pred: CarModelPredictorInteractor ->
                predictor = pred
                activity.hideProgress()
            }
    }

    fun onCameraResult(bitmap: Bitmap?) {
        val bm = bitmap
        when (bm){
            is Bitmap -> {
                activity.clearCarModelItems()
                activity.showProgress("Predicting car")

                Observable
                    .just(1)
                    .subscribeOn(Schedulers.computation())
                    .map { return@map predictor.getSimilarities(bm) }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { (similarities, names) ->
                        activity.setCarModelItems(names, similarities)
                        activity.hideProgress()
                    }
            }
            null -> {
                Log.e("onCameraResult", "bitmap was null")
            }
        }
    }

    fun onCameraButtonClicked() {
        activity.showCameraActivity()
    }
}