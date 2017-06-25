package net.cardentify.app

import android.content.Intent
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import net.cardentify.app.LoginActivity


/**
 * Created by Tora on 6/24/2017.
 */
class LoginPresenter constructor(val loginActivity: LoginActivity)
            : GoogleApiClient.OnConnectionFailedListener {

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
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
            account = result.signInAccount
            loginActivity.showCarModelPredictorActivity()
        } else {

        }
    }
}