package net.masvate.vpnpri

import android.app.Application
import android.os.Build
import android.webkit.WebView
import com.anythink.core.api.ATSDK
import com.applovin.sdk.AppLovinMediationProvider
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkSettings
import com.tencent.mmkv.MMKV
import net.masvate.vpnpri.utils.loge
import kotlin.system.measureTimeMillis

class IKTX private constructor(application: Application) {

    companion object {
        @Volatile
        private var INSTANCE: IKTX? = null

        fun initialize(application: Application) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: IKTX(application)
                    .apply { INSTANCE = this }
            }


        fun getInstance() =
            INSTANCE ?: throw NullPointerException("Have you invoke initialize() before?")
    }

    val app = application

    val lovinSdk by lazy {
        AppLovinSdk.getInstance(
            app.getString(R.string.lovin_app_key).reversed(),
            AppLovinSdkSettings(app),
            app
        )
    }

    fun initStartUp() {
        measureTimeMillis {
            MMKV.initialize(app)
            lovinSdk.apply {
                mediationProvider = AppLovinMediationProvider.MAX
                initializeSdk()
            }
            initOther()
        }.let {
            "application initTime -> ${it}".loge()
        }
    }

    private fun initOther() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = Application.getProcessName()
            if (app.packageName != processName) {
                WebView.setDataDirectorySuffix(processName)
            }
        }

        ATSDK.setNetworkLogDebug(BuildConfig.DEBUG)
        ATSDK.integrationChecking(app)
        ATSDK.init(
            app,
            app.getString(R.string.top_on_app_id),
            app.getString(R.string.top_on_app_key)
        )
    }
}