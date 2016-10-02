package com.github.alkurop.socialintegration.base

/**
 * Created by alkurop on 10/2/16.
 */
data class SocialModel (val socialType: SocialType,
                        val token:String?,
                        val secret:String?,
                        val userId:String,
                        val userName:String?,
                        val avatar:String?,
                        val email:String?)
