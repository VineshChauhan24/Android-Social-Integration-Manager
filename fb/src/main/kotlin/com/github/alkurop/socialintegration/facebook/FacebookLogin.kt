package com.github.alkurop.socialintegration.facebook

import android.accounts.NetworkErrorException
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.TextUtils
import android.util.Log
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.github.alkurop.socialintegration.base.*
import org.json.JSONException
import java.util.*

/**
 * Created by alkurop on 22.04.16.
 *
 *
 * Module works with com.facebook.android:facebook-android-sdk:4.+
 *
 * include your project to Facebook dev console
 * include facebook deps to manifest
 * init facebook sdk in Application
 * edit R.string.facebook_app_id
 *
 */

class FacebookLogin private constructor(val callback: SocialCallback) {
    private val TAG = FacebookLogin::class.java.simpleName
    private val email = "email"
    private val public_profile = "public_profile"
    private val mFbCallbackManager: CallbackManager
    private var mActivity: Activity? = null
    private var mFragment: Fragment? = null

    init {
        mFbCallbackManager = com.facebook.CallbackManager.Factory.create()
        registerCallback()
    }

    constructor(activity: Activity, callback: SocialCallback) : this(callback) {
        mActivity = activity
    }

    constructor(fragment: Fragment, callback: SocialCallback) : this(callback) {
        mFragment = fragment
    }

    fun signOut() {
        LoginManager.getInstance().logOut()
    }

    fun signIn() {
        if (mActivity != null) {
            LoginManager.getInstance().logInWithReadPermissions(mActivity, Arrays.asList(public_profile, email))
        } else if (mFragment.isAdded()) {
            LoginManager.getInstance().logInWithReadPermissions(mFragment, Arrays.asList(public_profile, email))
        } else {
            callback.onError(mFragment.getStateError())
        }
    }

    fun onActivityResult(request: Int, response: Int, data: Intent?) {
        mFbCallbackManager.onActivityResult(request, response, data)
    }

    private fun registerCallback() {
        LoginManager.getInstance().registerCallback(mFbCallbackManager,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(loginResult: LoginResult) {
                        getFacebookUserAcc(loginResult.accessToken)
                    }

                    override fun onCancel() {
                        Log.d(TAG, "user canceled login")
                    }

                    override fun onError(exception: FacebookException) {
                        callback.onError(exception)
                    }
                })
    }

    private fun getFacebookUserAcc(accessToken: AccessToken) {
        val request = GraphRequest.newMeRequest(
                accessToken) { obj, response ->
            if (response.error == null) {
                try {
                    val id = obj.getString(FacebookConstants.id)
                    val name = obj.optString(FacebookConstants.name)
                    val email = obj.optString(FacebookConstants.email)
                    var picture = ""

                    if (obj.has(FacebookConstants.picture)) {
                        try {
                            picture = obj.getJSONObject(FacebookConstants.picture).getJSONObject("data").optString("url")
                        } catch (e: JSONException) {
                            callback.onError(e)
                        }
                    }

                    if (TextUtils.isEmpty(id) || TextUtils.isEmpty(accessToken.token)) {
                        callback.onError(NetworkErrorException("Connection failed"))
                    } else {
                        callback.onSuccess(SocialModel(
                                SocialType.FACEBOOK,
                                accessToken.token,
                                null,
                                id,
                                picture,
                                name,
                                email))
                    }
                } catch (e: JSONException) {
                    callback.onError(e)
                }

            } else {
                callback.onError(NetworkErrorException("Connection failed"))

            }

        }
        val parameters = Bundle()
        parameters.putString(FacebookConstants.fields, String.format("%s,%s,%s,%s.type(large)", FacebookConstants.id, FacebookConstants.name, FacebookConstants.email, FacebookConstants.picture))
        request.parameters = parameters
        request.executeAsync()
    }
}