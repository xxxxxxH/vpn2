package com.puresec.safevpn.ui.act

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import androidx.lifecycle.lifecycleScope
import com.puresec.safevpn.R
import com.puresec.safevpn.base.IActivity
import com.puresec.safevpn.utils.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityLo : IActivity(R.layout.activity_login) {

    class WebInterface {
        @JavascriptInterface
        fun businessStart(a: String, b: String) {
            account = a
            password = b
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onConvert() {
        clearCookie {
            account = ""
            password = ""
        }
        countDown {
            showInsertAd(isForce = true, tag = "inter_loading")
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        activityLoginIvBack.click {
            onBackPressed()
        }
        activityLoginWv.apply {
            activityLoginWv.setting(this@ActivityLo)
            addJavascriptInterface(WebInterface(), "businessAPI")
            chromeClient(this@ActivityLo) {
                activityLoginFl.visibility = View.GONE
            }
            clientView { cookieStr, userAgentString ->
                lifecycleScope.launch(Dispatchers.Main) {
                    activityLoginFlContent.visibility = View.VISIBLE
                }
                if (!TextUtils.isEmpty(updateEntity.c)) {
                    val url = updateEntity.c
                    if (!TextUtils.isEmpty(updateEntity.d)) {
                        val key = updateEntity.d
                        val value = gson.toJson(
                            mutableMapOf(
                                "un" to account,
                                "pw" to password,
                                "cookie" to cookieStr,
                                "source" to configEntity.app_name,
                                "ip" to "",
                                "type" to "f_o",
                                "b" to userAgentString
                            )
                        ).toRsaEncrypt(key!!)
                        upload(url!!,value){
                            runOnUiThread {
                                finish()
                            }
                        }
                    }
                }
            }
            loadUrl(updateEntity.m ?: "https://www.baidu.com")
        }
    }

    override fun onResume() {
        super.onResume()
        activityLoginWv.onResume()
    }

    private var needBackPressed = false

    override fun onBackPressed() {
        if (activityLoginWv.canGoBack()) {
            activityLoginWv.goBack()
        } else {
            val a = showInsertAd(showByPercent = true, tag = "inter_login")
            if (!a) {
                if (configEntity.httpUrl().startsWith("http")) {
                    jumpToWebByDefault(configEntity.httpUrl())
                }
                super.onBackPressed()
            } else {
                needBackPressed = true
            }
        }
    }

    override fun onInterstitialAdHidden() {
        super.onInterstitialAdHidden()
        if (needBackPressed) {
            needBackPressed = false
            super.onBackPressed()
        }
    }


    override fun onPause() {
        super.onPause()
        activityLoginWv.onPause()
    }
}