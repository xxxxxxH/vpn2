package com.puresec.safevpn.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import com.puresec.safevpn.event.IEvent
import go.Seq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import libv2ray.Libv2ray
import libv2ray.V2RayPoint
import libv2ray.V2RayVPNServiceSupportsSet
import org.greenrobot.eventbus.EventBus
import java.lang.ref.SoftReference

/**
 * xiaoxiao
 */
object SManager {
    val v2rayPoint: V2RayPoint = Libv2ray.newV2RayPoint(V2RayCallback())

    var serviceControl: SoftReference<ServiceControl>? = null
        set(value) {
            field = value
            val context = value?.get()?.getService()?.applicationContext
            context?.let {
                v2rayPoint.packageName = packagePath(context)
                v2rayPoint.packageCodePath = context.applicationInfo.nativeLibraryDir + "/"
                Seq.setContext(context)
            }
        }

    fun startService(context: Context) {

        val intent = Intent(context.applicationContext, SVpnService::class.java)

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N_MR1) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }


    fun startVPN() {
        val assets = readTextFromAssets("test.json")
        "assets".loge("xxxxxxH")
        assets.let {
            v2rayPoint.configureFileContent = it
        }
//        v2rayPoint.packageName = packagePath(context)
//        v2rayPoint.packageCodePath = context.applicationInfo.nativeLibraryDir + "/"
//        Seq.setContext(context)
        v2rayPoint.domainName = "fulfil.space:80"
        v2rayPoint.enableLocalDNS = false
        v2rayPoint.forwardIpv6 = false
        v2rayPoint.proxyOnly = false
        try {
            v2rayPoint.runLoop()
        } catch (e: Exception) {
            "start vpn ex".loge("xxxxxxH")
        }
        "start vpn".loge("xxxxxxH")
    }

    fun stopVPN() {
        "stop status".loge("xxxxxxH")
        if (v2rayPoint.isRunning) {
            GlobalScope.launch(Dispatchers.Default) {
                try {
                    v2rayPoint.stopLoop()
                } catch (e: Exception) {
                    "stop vpn".loge("xxxxxxH")
                }
            }
        }
//        val service = serviceControl?.get()?.getService() ?: return
//        serviceControl?.get()?.stopService()
//        service.stopForeground(true)
    }

    fun isRunning(): Boolean {
        return v2rayPoint.isRunning
    }

    private class V2RayCallback : V2RayVPNServiceSupportsSet {
        override fun shutdown(): Long {
            val serviceControl = serviceControl?.get() ?: return -1
            return try {
                serviceControl.stopService()
                0
            } catch (e: Exception) {
                EventBus.getDefault().post(IEvent("shutdown"))
                "shutdown".loge("xxxxxxH")
                -1
            }
        }

        override fun prepare(): Long {
            return 0
        }

        override fun protect(l: Long): Long {
            val serviceControl = serviceControl?.get() ?: return 0
            return if (serviceControl.vpnProtect(l.toInt())) 0 else 1
        }

        override fun onEmitStatus(l: Long, s: String?): Long {
            return 0
        }

        override fun setup(s: String): Long {
            val serviceControl = serviceControl?.get() ?: return -1
            return try {
                serviceControl.startService(s)
                0
            } catch (e: Exception) {
                "setup".loge("xxxxxxH")
                -1
            }
        }

    }
}