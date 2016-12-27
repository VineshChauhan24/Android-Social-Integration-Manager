package com.github.alkurop.socialintegration.base;

import android.support.v4.app.Fragment;

/**
 * Created by alkurop on 12/27/16.
 */

public class JFragmentUtils {
    public static boolean isFragmentAdded (Fragment fragment) {
        return fragment != null && fragment.isAdded();
    }

    public static Exception getFragmentStateError(){
      return new IllegalStateException("fragment is NULL or not attached to activity");
    }
}
