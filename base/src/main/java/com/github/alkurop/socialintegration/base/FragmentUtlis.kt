package com.github.alkurop.socialintegration.base

import android.support.v4.app.Fragment

/**
 * Created by alkurop on 15.07.16.
 */
fun Fragment?.isAdded(): Boolean {
    return this?.isAdded ?: false
}

fun Fragment?.getStateError() =
          java.lang.IllegalStateException("fragment is NULL or not attached to activity")

