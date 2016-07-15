package com.github.alkurop.socialintegration.gplus

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import com.github.alkurop.socialintegration.base.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Scope

/**
 * Created by alkurop on 15.07.16.
 */
/**
 * Created by alkurop on 25.04.16.
 * make sure you have an application registered on the google dev console
 * generate  OAuth2 key for a Web Application (not android app, but Web Application), and put the client id to
 * constructor of this class
 */


class GooglePlusLogin private constructor(val clientId: String, val callback: SocialCallback) : GoogleApiClient.ConnectionCallbacks,
          GoogleApiClient.OnConnectionFailedListener {

    private val REQUEST_CODE = 555
    private val TAG = GooglePlusLogin::class.java.simpleName
    private val mGso: GoogleSignInOptions
    private lateinit var mGoogleApiClient: GoogleApiClient
    private var mPendingRequest = PendingRequest.NONE
    private var mActivity: Activity? = null
    private var mFragment: Fragment? = null

    init {
        mGso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                  .requestScopes(Scope(Scopes.PLUS_LOGIN))
                  .requestEmail()
                  .build()
    }

    constructor(fra: Fragment, id: String, callback: SocialCallback) : this(id, callback) {
        mFragment = fra
        initClient()
    }

    constructor(act: Activity, id: String, callback: SocialCallback) : this(id, callback) {
        mActivity = act
        initClient()
    }

    private fun initClient() {
        val context: Context
        if (mActivity != null) {
            context = mActivity!!
        } else if (mFragment.isAdded()) {
            context = mFragment!!.context
        } else {
            callback.onError(mFragment.getStateError())
            return
        }
        mGoogleApiClient = GoogleApiClient.Builder(context)
                  .addApi(Auth.GOOGLE_SIGN_IN_API, mGso)
                  .addConnectionCallbacks(this)
                  .addOnConnectionFailedListener(this)
                  .build();
    }

    fun signIn() {
        mPendingRequest = PendingRequest.SIGN_IN
        executeRequestIfConnected()
    }

    fun signOut() {
        mPendingRequest = PendingRequest.SIGN_OUT
        executeRequestIfConnected()
    }

    fun onStop() {
        disconnect()
    }

    fun onActivityResult(request: Int, result: Int, data: Intent?) {
        if (request == REQUEST_CODE) {
            if (result == 0) {
                mPendingRequest = PendingRequest.NONE
                Log.d(TAG, "user canceled login")
            }
            handleSignInResult(Auth.GoogleSignInApi.getSignInResultFromIntent(data))
        }
    }

    override fun onConnected(p0: Bundle?) {
        executePendingRequest()
    }

    override fun onConnectionSuspended(code: Int) {
    }

    override fun onConnectionFailed(result: ConnectionResult) {
        callback.onError(IllegalStateException("connection failed"))
    }

    private fun handleSignInResult(result: GoogleSignInResult?) {
        mPendingRequest = PendingRequest.NONE
        Log.d(TAG, "signing res = ${result?.isSuccess} ")
        signOut()
        if (result?.isSuccess ?: false) {
            signOut()
            val token = result?.signInAccount?.serverAuthCode
            if (token == null) {
                callback.onError(IllegalStateException("token == null"))
            } else {
                callback.onSuccess(SocialType.GOOGLE_PLUS, token)
            }

        } else {
            callback.onError(IllegalStateException("signing result  == failed"))
        }

    }

    private fun disconnect() {
        if (mGoogleApiClient.isConnected)
            mGoogleApiClient.disconnect()
    }

    private fun connect() {
        if (!mGoogleApiClient.isConnected)
            mGoogleApiClient.connect()
    }

    private fun executeRequestIfConnected() {
        if (mGoogleApiClient.isConnected)
            executePendingRequest()
        else
            connect()
    }

    private fun executePendingRequest() {
        if (mPendingRequest == PendingRequest.SIGN_OUT) {
            executeSignOutRequest()
        } else if (mPendingRequest == PendingRequest.SIGN_IN) {
            executeSignInRequest()
        }
    }

    private fun executeSignInRequest() {
        val signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient)
        if (mActivity != null) {
            mActivity?.startActivityForResult(signInIntent, REQUEST_CODE)
        } else if (mFragment.isAdded()) {
            mFragment?.startActivityForResult(signInIntent, REQUEST_CODE)
        } else {
            callback.onError(mFragment.getStateError())
        }
    }

    private fun executeSignOutRequest() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback {
            mPendingRequest = PendingRequest.NONE
        }
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback {
            mPendingRequest = PendingRequest.NONE
        }
    }

    private enum class PendingRequest {
        SIGN_IN,
        SIGN_OUT,
        NONE
    }
}