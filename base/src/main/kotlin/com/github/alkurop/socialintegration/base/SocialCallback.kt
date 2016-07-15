package com.github.alkurop.socialintegration.base

/**
 * Created by alkurop on 15.07.16.
 */
interface SocialCallback {
    fun onSuccess(type: SocialType, token: String, secret: String? = null)

    fun onError(exception: Exception)
}

enum class SocialType {
    GOOGLE_PLUS,
    FACEBOOK,
    TWITTER
}