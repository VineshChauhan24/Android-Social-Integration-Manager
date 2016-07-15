package com.github.alkurop.socialintegration.facebook

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import android.util.Log
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.github.alkurop.socialintegration.base.*
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
        LoginManager.getInstance().logOut();
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
                          callback.onSuccess(SocialType.FACEBOOK, loginResult.accessToken.token)
                      }

                      override fun onCancel() {
                          Log.d(TAG, "user canceled login")
                      }

                      override fun onError(exception: FacebookException) {
                          callback.onError(exception)
                      }
                  })
    }
}