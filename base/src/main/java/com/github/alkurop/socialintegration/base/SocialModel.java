package com.github.alkurop.socialintegration.base;

/**
 * Created by alkurop on 12/27/16.
 */

public class SocialModel {
    private final   SocialType socialType;
    private   String token;
    private   String secret;
    private   String userId;
    private   String userName;
    private   String avatar;
    private   String email;

    public SocialModel (final SocialType socialType) {
        this.socialType = socialType;
    }

    public SocialModel setAvatar (final String avatar) {
        this.avatar = avatar;
        return this;
    }

    public SocialModel setEmail (final String email) {
        this.email = email;
        return this;
    }

    public SocialModel setSecret (final String secret) {
        this.secret = secret;
        return this;
    }

    public SocialModel setToken (final String token) {
        this.token = token;
        return this;
    }

    public SocialModel setUserId (final String userId) {
        this.userId = userId;
        return this;
    }

    public SocialModel setUserName (final String userName) {
        this.userName = userName;
        return this;
    }

    public String getAvatar () {
        return avatar;
    }

    public String getEmail () {
        return email;
    }

    public String getSecret () {
        return secret;
    }

    public SocialType getSocialType () {
        return socialType;
    }

    public String getToken () {
        return token;
    }

    public String getUserId () {
        return userId;
    }

    public String getUserName () {
        return userName;
    }
}
