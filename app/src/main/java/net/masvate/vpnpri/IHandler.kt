package net.masvate.vpnpri

import com.facebook.applinks.AppLinkData

abstract class IHandler : AppLinkData.CompletionHandler {

    private var isInvoke = false

    override fun onDeferredAppLinkDataFetched(appLinkData: AppLinkData?) {
        if (!isInvoke) {
            isInvoke = true
            onInvoke(appLinkData)
        }
    }

    abstract fun onInvoke(appLinkData: AppLinkData?)
}