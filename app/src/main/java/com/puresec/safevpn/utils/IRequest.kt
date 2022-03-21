package com.puresec.safevpn.utils

import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.applinks.AppLinkData
import com.google.gson.Gson
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.puresec.safevpn.IHandler
import com.puresec.safevpn.R
import com.puresec.safevpn.base.IActivity
import com.puresec.safevpn.ibean.IConfig
import com.puresec.safevpn.ibean.IResult
import com.puresec.safevpn.ibean.IUpdate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.xutils.common.Callback
import org.xutils.http.RequestParams
import org.xutils.x

fun CoroutineScope.requestConfig(block: () -> Unit) {
    launch(Dispatchers.IO) {
        val params = RequestParams(app.getString(R.string.base_url) + "config")
        x.http().get(params, object : Callback.CommonCallback<String> {
            override fun onSuccess(result: String?) {
                if (!TextUtils.isEmpty(result)) {
                    val s1 = StringBuffer(result!!).replace(1, 2, "").toString()
                    s1.loge("xxxxxxHs1")
                    var s2: String? = null
                    if (s1.isBase64()) {
                        s2 = s1.toByteArray().fromBase64().decodeToString()
                        s2.loge("xxxxxxHs2")
                    }
                    if (!TextUtils.isEmpty(s2)) {
                        configEntity = Gson().fromJson(s2, IConfig::class.java)
                        configEntity.loge("xxxxxxHconfigEntity")
                    }
                    if (configEntity != null) {
                        if (configEntity.insertAdInvokeTime() != adInvokeTime || configEntity.insertAdRealTime() != adRealTime) {
                            adInvokeTime = configEntity.insertAdInvokeTime()
                            adRealTime = configEntity.insertAdRealTime()
                            adShownIndex = 0
                            adLastTime = 0
                            adShownList = mutableListOf<Boolean>().apply {
                                if (adInvokeTime >= adRealTime) {
                                    (0 until adInvokeTime).forEach { _ ->
                                        add(false)
                                    }
                                    (0 until adRealTime).forEach { index ->
                                        set(index, true)
                                    }
                                }
                            }
                        }
                    }
                    if (configEntity.faceBookId().isNotBlank()) {
                        initFaceBook()
                    }
                    var info: String? = null
                    if (configEntity.info != null) {
                        if (configEntity.info!!.isBase64()) {
                            info = configEntity.info!!.toByteArray().fromBase64().decodeToString()
                            info.loge("xxxxxxHinfo")
                        }
                        if (!TextUtils.isEmpty(info)) {
                            updateEntity = Gson().fromJson(info, IUpdate::class.java)
                            updateEntity.loge("xxxxxxHupdateEntity")
                        }
                    }
                }
            }

            override fun onError(ex: Throwable?, isOnCallback: Boolean) {
                "xxxxxxHonError -> ${ex.toString()}".loge()
            }

            override fun onCancelled(cex: Callback.CancelledException?) {

            }

            override fun onFinished() {

            }

        })

        withContext(Dispatchers.Main) {
            block()
        }
    }
}


fun upload(url:String,value:String,block: () -> Unit){
    val body: RequestBody =
        Gson().toJson(mutableMapOf("content" to value))
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
    OkGo.post<String>(url).upRequestBody(body)
        .execute(object : StringCallback() {
            override fun onSuccess(response: com.lzy.okgo.model.Response<String>?) {
                Log.i("xxxxxxH", response?.body().toString())
                response?.let {
                    val result = Gson().fromJson(
                        it.body().toString(),
                        IResult::class.java
                    )
                    if (result.code == "0" && result.data?.toBooleanStrictOrNull() == true) {
                        "requestCollect success".loge()
                        isLogin = true
                        block()
                    }
                }
            }
        })
}

fun initFaceBook() {
    FacebookSdk.apply {
        setApplicationId(configEntity.faceBookId())
        sdkInitialize(app)
        setAdvertiserIDCollectionEnabled(true)
        setAutoLogAppEventsEnabled(true)
        fullyInitialize()
    }
}

fun IActivity.fetchAppLink(key: String, callback: (Uri?) -> Unit) {
    AppLinkData.fetchDeferredAppLinkData(this, key, object : IHandler() {
        override fun onInvoke(appLinkData: AppLinkData?) {
            callback(appLinkData?.targetUri)
        }
    })
}

suspend fun <T> doSuspendOrNull(block: suspend () -> T) =
    try {
        block()
    } catch (e: Exception) {
        "doOrNull ->$e".loge()
        null
    }
