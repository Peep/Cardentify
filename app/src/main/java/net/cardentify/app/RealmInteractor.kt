package net.cardentify.app

import android.content.Context
import android.util.Log
import io.realm.ObjectServerError
import io.realm.Realm
import io.realm.SyncCredentials
import io.realm.SyncUser
import io.realm.exceptions.RealmError

enum class LoginMethod {
    USERPASS,
    GOOGLE
}

class RealmProvider(context: Context, val method: LoginMethod) : SyncUser.Callback {
    private var _realm: Realm
    private lateinit var _token: String

    private val HTTP_PORT: Int get() = 9080
    private val HTTPS_PORT: Int get() = 9443
    // TODO(Corey): Access the realm and find out how data syncs work
    private val REALM_URL: String get() = "realms://cardentify.net:$HTTPS_PORT/~/poopfeast"
    private val HTTPS_AUTH_URL: String get() = "https://cardentify.net:$HTTPS_PORT/auth"
    private val HTTP_AUTH_URL: String get() = "http://cardentify.net:$HTTP_PORT/auth"
    private val USERNAME: String get() = "test@example.com"
    private val PASSWORD: String get() = "test123"

    init {
        Realm.init(context)
        _realm = Realm.getDefaultInstance()
    }

    // TODO(Corey): Can probably move this up to another constructor
    fun setAccessToken(token: String) {
        _token = token
    }

    fun CreateSyncUser() {
        val credentials : SyncCredentials

        when (method) {
            LoginMethod.USERPASS -> credentials = SyncCredentials.usernamePassword(USERNAME, PASSWORD)
            // TODO(Corey): Add a way of getting Google tokens in, but allow fallback to user/pass?
            LoginMethod.GOOGLE -> credentials = SyncCredentials.google(_token)
            else -> {
                throw RealmError("No valid login method was provided.")
            }
        }

        SyncUser.loginAsync(credentials, HTTPS_AUTH_URL, this)
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

