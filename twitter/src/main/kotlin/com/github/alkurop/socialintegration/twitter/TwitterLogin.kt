package com.github.alkurop.socialintegration.twitter

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.github.alkurop.socialintegration.base.SocialCallback
import com.github.alkurop.socialintegration.base.SocialModel
import com.github.alkurop.socialintegration.base.SocialType
import com.twitter.sdk.android.Twitter
import com.twitter.sdk.android.core.*
import com.twitter.sdk.android.core.identity.TwitterAuthClient

/**
 * Created by alkurop on 15.07.16.
 */
class TwitterLogin private constructor(var callback: SocialCallback) {
    private val TAG = TwitterLogin::class.java.simpleName
    private val mTwitterAuthClient = TwitterAuthClient()
    private var mActivity: Activity? = null

    constructor(activity: Activity, callback: SocialCallback) : this(callback) {
        mActivity = activity
    }

    fun signOut() {
        Twitter.getSessionManager().clearActiveSession()
        Twitter.logOut()
    }

    fun signIn() {
        authorizeActivity(mActivity!!)
    }

    fun onActivityResult(request: Int, result: Int, data: Intent?) {
        if (result == Activity.RESULT_CANCELED) {
            Log.d(TAG, "user canceled request")
        }
        mTwitterAuthClient.onActivityResult(request, result, data)
    }

    private fun authorizeActivity(activity: Activity) {
        mTwitterAuthClient.authorize(activity, object : Callback<TwitterSession>() {
            override fun success(result: Result<TwitterSession>?) {
                if (result == null ) {
                    callback.onError(IllegalArgumentException("Tweeter returned empty account"))
                } else {
                    try {
                        callback.onSuccess(SocialModel(
                                SocialType.TWITTER,
                                result.data.authToken.token,
                                result.data.authToken.secret,
                                result.data.userId.toString(),
                                null,
                                result.data.userName,
                                null)
                        )
                    } catch(e: IllegalArgumentException) {
                        callback.onError(e)
                    }
                }
            }

            override fun failure(error: TwitterException) {
                callback.onError(error)
            }
        })
    }
}