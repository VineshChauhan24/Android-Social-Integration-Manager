package com.github.alkurop.socialintegration.demo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.github.alkurop.socialintegration.base.SocialCallback
import com.github.alkurop.socialintegration.base.SocialModel
import com.github.alkurop.socialintegration.base.SocialType
import com.github.alkurop.socialintegration.facebook.FacebookLogin
import com.github.alkurop.socialintegration.gplus.GooglePlusLogin
import com.github.alkurop.socialintegration.twitter.TwitterLogin
import kotlinx.android.synthetic.main.activity_main.*

class SocialLoginExampleActivity : AppCompatActivity() {
    val TAG = SocialLoginExampleActivity::class.java.simpleName
    lateinit var facebookLogin: FacebookLogin
    lateinit var twitterLogin: TwitterLogin
    lateinit var googleLogin: GooglePlusLogin
    lateinit var socialCallback: SocialCallback
    lateinit var facebookButton: View
    lateinit var twitterButton: View
    lateinit var googleButton: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initSocialCallback()
        initSocialNetworks()

        facebookButton = LoginViewHelper.getFacebookButton(this, container)
        twitterButton = LoginViewHelper.getTweeterButton(this, container)
        googleButton = LoginViewHelper.getGooglePlusNewButton(this, container)

        facebookButton.setOnClickListener { facebookLogin.signIn() }
        twitterButton.setOnClickListener { twitterLogin.signIn() }
        googleButton.setOnClickListener { googleLogin.signIn() }
    }

    private fun initSocialCallback() {
        socialCallback = object : SocialCallback {
            override fun onSuccess(model: SocialModel) {
                Log.d(TAG, "Social login success: $model")

                // don't forget to finish social session when log out from user account
                when (model.socialType) {
                    SocialType.FACEBOOK -> facebookLogin.signOut()
                    SocialType.GOOGLE_PLUS -> googleLogin.signOut()
                    SocialType.TWITTER -> twitterLogin.signOut()
                }
            }

            override fun onError(exception: Exception) {
                exception.printStackTrace()
            }
        }
    }

    private fun initSocialNetworks() {
        facebookLogin = FacebookLogin(this, socialCallback)
        twitterLogin = TwitterLogin(this, socialCallback)
        googleLogin = GooglePlusLogin(this, getString(R.string.googlePlus_client_id), socialCallback)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        facebookLogin.onActivityResult(requestCode, resultCode, data)
        twitterLogin.onActivityResult(requestCode, resultCode, data)
        googleLogin.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onStop() {
        googleLogin.onStop()
        super.onStop()
    }
}

object LoginViewHelper {
    fun getFacebookButton(context: Context, container: LinearLayout): Button {
        val facebookBtn = Button(context)
        container.addView(facebookBtn)
        with(facebookBtn) {
            text = "login facebook"
            setBackgroundColor(Color.parseColor("#6666ff"))
            setTextColor(Color.parseColor("#ffffff"))
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            val p = layoutParams as LinearLayout.LayoutParams
            p.setMargins(20, 20, 20, 20)
        }
        return facebookBtn
    }

    fun getTweeterButton(context: Context, container: LinearLayout): Button {
        val tweeterButton = Button(context)
        container.addView(tweeterButton)
        with(tweeterButton) {
            text = "login tweeter"
            setBackgroundColor(Color.parseColor("#00aa00"))
            setTextColor(Color.parseColor("#ffffff"))
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            val p = layoutParams as LinearLayout.LayoutParams
            p.setMargins(20, 20, 20, 20)
        }
        return tweeterButton
    }

    fun getGooglePlusNewButton(context: Context, container: LinearLayout): Button {
        val gPlusButton = Button(context)
        container.addView(gPlusButton)
        with(gPlusButton) {
            text = "login google plus "
            setBackgroundColor(Color.parseColor("#ff6666"))
            setTextColor(Color.parseColor("#ffffff"))
            layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
            layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            val p = layoutParams as LinearLayout.LayoutParams
            p.setMargins(20, 20, 20, 20)
        }
        return gPlusButton
    }
}