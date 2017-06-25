package net.cardentify.app

import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount


/**
 * Created by Tora on 6/24/2017.
 */
class LoginPresenter constructor(val loginActivity: LoginActivity)
            : GoogleApiClient.OnConnectionFailedListener {
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(loginActivity.getString(R.string.google_client_id))
            .build()!!

    val googleApiClient = GoogleApiClient.Builder(loginActivity)
            .enableAutoManage(loginActivity, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build()!!

    var account: GoogleSignInAccount? = null

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    fun onSignInClicked() {
        loginActivity.showSignIn(googleApiClient)
    }

    fun onSignInResult(data: Intent?) {
        val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
        if(result.isSuccess) {
            val acc = result.signInAccount
            when(acc) {
                is GoogleSignInAccount -> {
                    account = acc
                    loginActivity.showCarModelPredictorActivity(acc)
                }
                null -> {
                    Log.e("onSignInResult", "account was null")
                }
            }
        } else {
            Log.e("onSignInResult", "Authentication failed: ${result.status.statusMessage}")
        }
    }
}