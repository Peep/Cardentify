package net.cardentify.app

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.common.api.GoogleApiClient


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    val RC_SIGN_IN = 1000
    var presenter: LoginPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        presenter = LoginPresenter(this)

        findViewById(R.id.sign_in_button).setOnClickListener(this)
    }

    override fun onClick(clickedView: View?) {
        if(clickedView?.id == R.id.sign_in_button) {
            presenter?.onSignInClicked()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == RC_SIGN_IN) {
            presenter?.onSignInResult(data)
        }
    }

    fun showSignIn(googleApiClient: GoogleApiClient) {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    fun showCarModelPredictorActivity() {
        val intent = Intent(this, CarModelPredictorActivity::class.java)
        startActivity(intent)
    }
}
