package com.github.alkurop.socialintegration.base

/**
 * Created by alkurop on 15.07.16.
 */
interface SocialCallback {
    fun onSuccess(model :SocialModel)

    fun onError(exception: Exception)
}
