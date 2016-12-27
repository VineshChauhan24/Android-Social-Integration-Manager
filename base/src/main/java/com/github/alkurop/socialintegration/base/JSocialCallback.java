package com.github.alkurop.socialintegration.base;

/**
 * Created by alkurop on 12/27/16.
 */

public interface JSocialCallback {
    void  onSuccess(JSocialModel model );

    void onError(Exception exception);
}
