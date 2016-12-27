package com.github.alkurop.socialintegration.twitter;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.github.alkurop.socialintegration.base.SocialCallback;
import com.github.alkurop.socialintegration.base.SocialModel;
import com.github.alkurop.socialintegration.base.SocialType;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;

/**
 * Created by alkurop on 12/27/16.
 */

public class TwitterLogin {
    public static final String TAG = TwitterLogin.class.getSimpleName();
    private final Activity mActivity;
    private final SocialCallback mCallback;
    private final TwitterAuthClient mTwitterAuthClient;

    public TwitterLogin (final Activity activity, final SocialCallback callback) {
        this.mActivity = activity;
        this.mCallback = callback;
        mTwitterAuthClient = new TwitterAuthClient();
    }

    public void signOut () {
        Twitter.getSessionManager().clearActiveSession();
        Twitter.logOut();
    }

    public void signIn () {
        authorizeActivity(mActivity);
    }

    public void onActivityResult (int request, int result, Intent data) {
        if (result == Activity.RESULT_CANCELED) {
            Log.d(TAG, "user canceled request");
        }
        mTwitterAuthClient.onActivityResult(request, result, data);
    }

    private void authorizeActivity (final Activity activity) {
        mTwitterAuthClient.authorize(activity, new Callback<TwitterSession>() {
            @Override public void success (final Result<TwitterSession> result) {
                try {
                    mCallback.onSuccess(
                              new SocialModel(SocialType.TWITTER)
                                        .setToken(result.data.getAuthToken().token)
                                        .setSecret(result.data.getAuthToken().secret)
                                        .setUserId(String.valueOf(result.data.getUserId()))
                                        .setUserName(result.data.getUserName())
                                       );
                }
                catch (IllegalArgumentException e) {
                    mCallback.onError(e);
                }
            }

            @Override public void failure (final TwitterException e) {
                mCallback.onError(e);
            }
        });
    }
}
