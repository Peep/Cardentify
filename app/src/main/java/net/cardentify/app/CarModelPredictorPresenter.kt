package net.cardentify.app

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Tora on 6/25/2017.
 */
class CarModelPredictorPresenter constructor(val activity: CarModelPredictorActivity) {
    val predictor = CarModelPredictorInteractor(activity.assets)

    fun onAccountReceived(account: GoogleSignInAccount?) {
        // Get the account token and create the realm user
        if(account != null) {
            val token = account.idToken
            when(token) {
                is String -> {
                    val realm = RealmProvider(activity.applicationContext,
                            RealmProvider.LoginMethod.GOOGLE)
                    realm.token = token
                    realm.createSyncUser()
                }
                null -> {
                    Log.e("onAccountReceived", "account.idToken was null")
                }
            }

        } else {
            Log.e("onAccountReceived", "account was null")
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