package net.cardentify.app

import android.content.Context
import android.util.Log
import io.realm.ObjectServerError
import io.realm.Realm
import io.realm.SyncCredentials
import io.realm.SyncUser
import io.realm.exceptions.RealmError

class RealmProvider(context: Context, val method: LoginMethod) : SyncUser.Callback {
    enum class LoginMethod {
        USERPASS,
        GOOGLE
    }

    var token: String? = null

    private val _realm: Realm

    private val HTTP_PORT = 9080
    private val HTTPS_PORT = 9443
    // TODO(Corey): Access the realm and find out how data syncs work
    private val REALM_URL = "realms://cardentify.net:$HTTPS_PORT/~/poopfeast"
    private val HTTPS_AUTH_URL = "https://cardentify.net:$HTTPS_PORT/auth"
    private val HTTP_AUTH_URL = "http://cardentify.net:$HTTP_PORT/auth"
    private val USERNAME = "test@example.com"
    private val PASSWORD = "test123"

    init {
        Realm.init(context)
        _realm = Realm.getDefaultInstance()
    }

    fun createSyncUser() {
        var credentials : SyncCredentials? = null

        when (method) {
            LoginMethod.USERPASS -> credentials = SyncCredentials.usernamePassword(USERNAME, PASSWORD)
            LoginMethod.GOOGLE -> {
                val tk = token
                if(tk != null) {
                    Log.d("createSyncUser", "Using google token $tk as realm credentials")
                    credentials = SyncCredentials.google(tk)
                } else {
                    Log.e("createSyncUsers", "token was null")
                }
            }
            else -> {
                Log.e("createSyncUsers", "No valid login method was provided.")
            }
        }

        if(credentials != null) {
            SyncUser.loginAsync(credentials, HTTPS_AUTH_URL, this)
        } else {
            Log.e("createSyncUsers", "Could not login because credentials were null")
        }
    }

    override fun onSuccess(user: SyncUser?) {
        Log.d("RealmInteractor", "User access token: ${user?.accessToken}")
        Log.d("RealmInteractor", "User is valid?: ${user?.isValid}")
        Log.d("RealmInteractor", "Authentication URL: ${user?.authenticationUrl}")
    }

    override fun onError(error: ObjectServerError?) {
        Log.d("RealmInteractor", "Error of type ${error?.errorCode}. Message ${error?.errorMessage?: "was not provided."}")
        Log.d("RealmInteractor", "Exception: ${error?.exception?.toString()?: "was not thrown."}")
    }
}

