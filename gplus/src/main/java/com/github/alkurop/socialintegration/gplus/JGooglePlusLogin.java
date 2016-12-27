package com.github.alkurop.socialintegration.gplus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import com.github.alkurop.socialintegration.base.JFragmentUtils;
import com.github.alkurop.socialintegration.base.JSocialCallback;
import com.github.alkurop.socialintegration.base.JSocialModel;
import com.github.alkurop.socialintegration.base.SocialType;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

/**
 * Created by alkurop on 12/27/16.
 */

public class JGooglePlusLogin implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    public static final int REQUEST_CODE = 555;
    public static final String TAG = JGooglePlusLogin.class.getSimpleName();
    private final Activity mActivity;
    private final Fragment mFragment;
    private final Type mType;
    private final JSocialCallback mCallback;
    private final String clientId;
    private GoogleSignInOptions mGso;
    private GoogleApiClient mGoogleApiClient;
    private PendingRequest mPendingRequest = PendingRequest.NONE;

    private enum Type {
        Fragment,
        Activity;

    }

    private enum PendingRequest {
        SIGN_IN,
        SIGN_OUT,
        NONE;

    }

    public JGooglePlusLogin (final Activity activity, String clientId, JSocialCallback callback) {
        this.mActivity = activity;
        this.mType = Type.Activity;
        this.mFragment = null;
        this.mCallback = callback;
        this.clientId = clientId;
        initRequest();
        initClient();
    }

    public JGooglePlusLogin (final Fragment fragment, String clientId, JSocialCallback callback) {
        this.mFragment = fragment;
        this.mActivity = null;
        this.mType = Type.Fragment;
        this.mCallback = callback;
        this.clientId = clientId;
        initRequest();
        initClient();
    }

    private void initRequest () {
        if (!TextUtils.isEmpty(clientId)) {
            mGso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                      .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                      .requestScopes(new Scope(Scopes.PLUS_ME))
                      .requestScopes(new Scope(Scopes.EMAIL))
                      .requestServerAuthCode(clientId)
                      .build();
        } else {
            mGso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                      .requestScopes(new Scope(Scopes.PLUS_LOGIN))
                      .requestScopes(new Scope(Scopes.PLUS_ME))
                      .requestScopes(new Scope(Scopes.EMAIL))
                      .build();
        }
    }

    private void initClient () {
        Context context = null;
        switch (mType) {
            case Activity:
                context = mActivity;
                break;
            case Fragment:
                if (!JFragmentUtils.isFragmentAdded(mFragment)) {

                } else {
                    mCallback.onError(JFragmentUtils.getFragmentStateError());
                    return;
                }
                break;
            default:
                context = null;
        }
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                  .addApi(Auth.GOOGLE_SIGN_IN_API, mGso)
                  .addConnectionCallbacks(this)
                  .addOnConnectionFailedListener(this)
                  .build();
    }

    public void signIn () {
        mPendingRequest = PendingRequest.SIGN_IN;
        executeRequestIfConnected();
    }

    public void signOut () {
        mPendingRequest = PendingRequest.SIGN_OUT;
        executeRequestIfConnected();
    }

    public void onStop () {
        disconnect();
    }

    private void executeRequestIfConnected () {
        if (mGoogleApiClient.isConnected()) {
            executePendingRequest();
        } else {
            connect();
        }
    }

    private void connect () {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    private void disconnect () {
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void onActivityResult (int request, int result, Intent data) {
        if (request == REQUEST_CODE) {
            if (result == 0) {
                mPendingRequest = PendingRequest.NONE;
                Log.d(TAG, "user canceled login");
            }
            handleSignInResult(Auth.GoogleSignInApi.getSignInResultFromIntent(data));
        }
    }

    @Override public void onConnectionFailed (@NonNull final ConnectionResult connectionResult) {
        mCallback.onError(new IllegalStateException("connection failed"));
    }

    @Override public void onConnected (@Nullable final Bundle bundle) {
        executePendingRequest();
    }

    private void handleSignInResult (final GoogleSignInResult result) {
        mPendingRequest = PendingRequest.NONE;
        Log.d(TAG, "signing res = ${result?.isSuccess} ");
        signOut();
        if (result.isSuccess()) {
            signOut();
            GoogleSignInAccount signInAccount = result.getSignInAccount();
            String token = signInAccount.getServerAuthCode();
            if (token == null) {
                mCallback.onError(new IllegalStateException("token == null"));
            } else {
                String email = signInAccount.getEmail();
                String photoUrl = null;
                if (signInAccount.getPhotoUrl() != null) {
                    photoUrl = signInAccount.getPhotoUrl().toString();
                }
                String id = signInAccount.getId();
                String displayName = signInAccount.getDisplayName();
                mCallback.onSuccess(
                          new JSocialModel(
                                    SocialType.GOOGLE_PLUS)
                                    .setToken(token)
                                    .setUserId(id)
                                    .setAvatar(photoUrl)
                                    .setUserName(displayName)
                                    .setEmail(email));
            }
        } else {
            mCallback.onError(new IllegalStateException("signing result  == failed"));
        }
    }

    private void executePendingRequest () {
        if (mPendingRequest == PendingRequest.SIGN_OUT) {
            executeSignOutRequest();
        } else if (mPendingRequest == PendingRequest.SIGN_IN) {
            executeSignInRequest();
        }
    }

    private void executeSignInRequest () {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        switch (mType) {
            case Activity:
                mActivity.startActivityForResult(signInIntent, REQUEST_CODE);
                break;
            case Fragment:
                if (JFragmentUtils.isFragmentAdded(mFragment)) {

                    mFragment.startActivityForResult(signInIntent, REQUEST_CODE);
                } else {
                    mCallback.onError(JFragmentUtils.getFragmentStateError());

                }

                break;
        }
    }

    private void executeSignOutRequest () {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override public void onResult (@NonNull final Status status) {
                mPendingRequest = PendingRequest.NONE;
            }
        }); {

        }
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override public void onResult (@NonNull final Status status) {
                mPendingRequest = PendingRequest.NONE;
            }
        });
    }

    @Override public void onConnectionSuspended (final int i) {

    }
}
