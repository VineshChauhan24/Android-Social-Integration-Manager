package com.github.alkurop.socialintegration.demo

import android.app.Application
import com.facebook.FacebookSdk
import com.twitter.sdk.android.Twitter
import com.twitter.sdk.android.core.TwitterAuthConfig
import io.fabric.sdk.android.Fabric

/**
 * Created by alkurop on 15.07.16.
 */
class App : Application(){
    override fun onCreate() {
        super.onCreate()
        FacebookSdk.sdkInitialize(this)
        val authConfig = TwitterAuthConfig(getString(R.string.twitter_key), getString(R.string.twitter_secret));
        Fabric.with(this, Twitter(authConfig))
    }
}

