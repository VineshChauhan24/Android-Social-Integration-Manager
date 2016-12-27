package com.github.alkurop.socialintegration.base;

/**
 * Created by alkurop on 12/27/16.
 */

public interface SocialCallback {
    void  onSuccess (SocialModel model);

    void onError(Exception exception);
}
