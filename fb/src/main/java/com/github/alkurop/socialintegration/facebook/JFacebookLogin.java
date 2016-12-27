package com.github.alkurop.socialintegration.facebook;

import android.accounts.NetworkErrorException;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.github.alkurop.socialintegration.base.JSocialCallback;
import com.github.alkurop.socialintegration.base.JSocialModel;
import com.github.alkurop.socialintegration.base.SocialType;
import java.util.ArrayList;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alkurop on 12/27/16.
 */

public class JFacebookLogin {
    public static final String TAG = JFacebookLogin.class.getSimpleName();
    private final CallbackManager mFbCallbackManager;
    private final Activity mActivity;
    private final Fragment mFragment;
    private final Type mType;
    private final JSocialCallback mCallback;

    private enum Type {
        Fragment,
        Activity
    }

    public JFacebookLogin (final Activity activity, JSocialCallback callback) {
        mFbCallbackManager = com.facebook.CallbackManager.Factory.create();
        this.mActivity = activity;
        this.mType = Type.Activity;
        this.mFragment = null;
        this.mCallback = callback;
        registerCallback();
    }

    public JFacebookLogin (final Fragment fragment, JSocialCallback callback) {
        mFbCallbackManager = com.facebook.CallbackManager.Factory.create();
        this.mFragment = fragment;
        this.mActivity = null;
        this.mType = Type.Fragment;
        this.mCallback = callback;
        registerCallback();
    }

    private void registerCallback () {
        LoginManager.getInstance().registerCallback(mFbCallbackManager, new FacebookCallback<LoginResult>() {
            @Override public void onSuccess (final LoginResult loginResult) {
                getFacebookUserAcc(loginResult.getAccessToken());
            }

            @Override public void onCancel () {
                Log.d(TAG, "user canceled login");
            }

            @Override public void onError (final FacebookException exception) {
                mCallback.onError(exception);
            }
        });
    }

    public void signOut () {
        LoginManager.getInstance().logOut();
    }

    public void signIn () {
        switch (mType) {
            case Activity:
                signInActivity();
                break;
            case Fragment:
                signInFragment();
                break;
        }
    }

    private void signInFragment () {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(JFacebookConstants.email);
        permissions.add(JFacebookConstants.public_profile);
        LoginManager.getInstance().logInWithReadPermissions(mFragment, permissions);
    }

    private void signInActivity () {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(JFacebookConstants.email);
        permissions.add(JFacebookConstants.public_profile);
        LoginManager.getInstance().logInWithReadPermissions(mActivity, permissions);
    }

    public void onActivityResult (int request, int response, Intent data) {
        mFbCallbackManager.onActivityResult(request, response, data);
    }

    private void getFacebookUserAcc (final AccessToken accessToken) {
        GraphRequest.GraphJSONObjectCallback callback = new GraphRequest.GraphJSONObjectCallback() {
            @Override public void onCompleted (final JSONObject obj, final GraphResponse response) {
                if (response.getError() == null) {
                    try {
                        String id = obj.getString(JFacebookConstants.id);
                        String name = obj.optString(JFacebookConstants.name);
                        String email = obj.optString(JFacebookConstants.email);
                        String picture = "";

                        if (obj.has(JFacebookConstants.picture)) {
                            try {
                                picture = obj.getJSONObject(JFacebookConstants.picture)
                                          .getJSONObject("data")
                                          .optString("url");
                            }
                            catch (JSONException e) {
                                mCallback.onError(e);
                            }
                        }

                        if (TextUtils.isEmpty(id) || TextUtils.isEmpty(accessToken.getToken())) {
                        } else {
                            mCallback.onSuccess(
                                      new JSocialModel(
                                                SocialType.FACEBOOK)
                                                .setToken(accessToken.getToken())
                                                .setUserId(id)
                                                .setAvatar(picture)
                                                .setUserName(name)
                                                .setEmail(email));
                        }
                    }
                    catch (JSONException e) {
                        mCallback.onError(e);
                    }
                } else {
                    mCallback.onError(new NetworkErrorException("Connection failed"));
                }
            }
        };

        GraphRequest request = GraphRequest.newMeRequest(accessToken, callback);
        Bundle params = new Bundle();
        params.putString(JFacebookConstants.fields, String.format("%s,%s,%s,%s.type(large)",
                                                                  JFacebookConstants.id,
                                                                  JFacebookConstants.name,
                                                                  JFacebookConstants.email,
                                                                  JFacebookConstants.picture));
        request.setParameters(params);
        request.executeAsync();
    }
}
