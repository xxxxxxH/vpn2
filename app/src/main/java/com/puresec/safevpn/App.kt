package com.puresec.safevpn

import android.app.Activity
import android.content.Context
import androidx.multidex.MultiDexApplication
import com.anythink.splashad.api.ATSplashAd
import com.anythink.splashad.api.ATSplashAdListener
import com.applovin.mediation.ads.MaxAdView
import com.applovin.mediation.ads.MaxInterstitialAd
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkSettings
import org.xutils.x


class App : MultiDexApplication() {

    companion object {
        var instance: App? = null
    }

    private val LOVINSDK by lazy {
        AppLovinSdk.getInstance(
            this.getString(R.string.lovin_app_key).reversed(),
            AppLovinSdkSettings(this),
            this
        )
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        IKTX.initialize(this)
        x.Ext.init(this)
        instance = this
    }

    override fun onCreate() {
        super.onCreate()
        IKTX.getInstance().initStartUp()
    }

    fun lovinSdk(): AppLovinSdk {
        return LOVINSDK
    }

    fun lovinInterstitialAd(ac: Activity): MaxInterstitialAd {
        return MaxInterstitialAd(getString(R.string.lovin_insert_ad_id), LOVINSDK, ac)
    }

    fun lovinNative(): MaxNativeAdLoader {
        return MaxNativeAdLoader(this.getString(R.string.lovin_native_ad_id), LOVINSDK, this)
    }

    fun lovinBanner(): MaxAdView {
        return MaxAdView(this.getString(R.string.lovin_banner_ad_id), LOVINSDK, this)
    }

    fun openAd(listener: ATSplashAdListener?): ATSplashAd {
        return ATSplashAd(this, this.getString(R.string.top_on_open_ad_id), listener)
    }

}