package com.miaomiao.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.*
import android.os.Build
import android.os.ParcelFileDescriptor
import android.os.StrictMode
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.masvate.vpnpri.App
import net.masvate.vpnpri.R
import net.masvate.vpnpri.event.IEvent
import net.masvate.vpnpri.utils.ServiceControl
import net.masvate.vpnpri.utils.isPureIpAddress
import net.masvate.vpnpri.utils.loge
import net.masvate.vpnpri.utils.packagePath
import org.greenrobot.eventbus.EventBus
import java.io.File
import java.lang.ref.SoftReference


class SVpnService : VpnService(), ServiceControl {

    val CHANNEL_ID_STRING = "service_01"
    lateinit var notificationManager: NotificationManager
    lateinit var channel: NotificationChannel
    lateinit var notification: Notification
    private lateinit var mInterface: ParcelFileDescriptor
    private val connectivity by lazy { getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }

    override fun onCreate() {
        super.onCreate()
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        SManager.serviceControl = SoftReference(this)

        notificationManager =
            App.instance!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel =
                NotificationChannel(CHANNEL_ID_STRING, "S_VPN", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
            notification = Notification.Builder(applicationContext, CHANNEL_ID_STRING).build()
            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        SManager.startVPN()
        startForeground(1, notification)
        return START_STICKY
    }

    override fun getService(): Service {
        return this
    }

    override fun startService(parameters: String) {
        " over start ".loge("xxxxxxH")
        EventBus.getDefault().post(IEvent("startService"))
        setup(parameters)
    }

    override fun stopService() {
        "over stop".loge("xxxxxxH")
        stopV2Ray(true)
    }

    override fun vpnProtect(socket: Int): Boolean {
        return protect(socket)
    }

    private fun setup(parameters: String) {

        val prepare = prepare(this)
        if (prepare != null) {
            return
        }

        // If the old interface has exactly the same parameters, use it!
        // Configure a builder while parsing the parameters.
        val builder = Builder()
        val enableLocalDns = false
        val routingMode = "0"

        parameters.split(" ")
            .map { it.split(",") }
            .forEach {
                when (it[0][0]) {
                    'm' -> builder.setMtu(java.lang.Short.parseShort(it[1]).toInt())
                    's' -> builder.addSearchDomain(it[1])
                    'a' -> builder.addAddress(it[1], Integer.parseInt(it[2]))
                    'r' -> {
                        if (routingMode == "1" || routingMode == "3") {
                            if (it[1] == "::") { //not very elegant, should move Vpn setting in Kotlin, simplify go code
                                builder.addRoute("2000::", 3)
                            } else {
                                resources.getStringArray(R.array.bypass_private_ip_address)
                                    .forEach { cidr ->
                                        val addr = cidr.split('/')
                                        builder.addRoute(addr[0], addr[1].toInt())
                                    }
                            }
                        } else {
                            builder.addRoute(it[1], Integer.parseInt(it[2]))
                        }
                    }
                    'd' -> builder.addDnsServer(it[1])
                }
            }

        if (!enableLocalDns) {
            val vpnDns = "1.1.1.1"
            vpnDns.split(",").filter { isPureIpAddress(it) }
                .forEach {
                    if (isPureIpAddress(it)) {
                        builder.addDnsServer(it)
                    }
                }
        }


        builder.setSession("Test")

        // Close the old interface since the parameters have been changed.
        try {
            mInterface.close()
        } catch (ignored: Exception) {
            // ignored
            "ignored".loge("xxxxxxH")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                connectivity.requestNetwork(defaultNetworkRequest, defaultNetworkCallback)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(false)
        }

        // Create a new interface using the builder and save the parameters.
        try {
            mInterface = builder.establish()!!
        } catch (e: Exception) {
            // non-nullable lateinit var

            e.printStackTrace()
            stopV2Ray()
        }

        sendFd()
    }

    private fun sendFd() {
        val fd = mInterface.fileDescriptor
        val path = File(packagePath(applicationContext), "sock_path").absolutePath

        GlobalScope.launch(Dispatchers.IO) {
            var tries = 0
            while (true) try {
                Thread.sleep(1000L shl tries)
                Log.d(packageName, "sendFd tries: $tries")
                LocalSocket().use { localSocket ->
                    localSocket.connect(
                        LocalSocketAddress(
                            path,
                            LocalSocketAddress.Namespace.FILESYSTEM
                        )
                    )
                    localSocket.setFileDescriptorsForSend(arrayOf(fd))
                    localSocket.outputStream.write(42)
                }
                break
            } catch (e: Exception) {
                Log.d(packageName, e.toString())
                if (tries > 5) break
                tries += 1
            }
        }
    }

    private fun stopV2Ray(isForced: Boolean = true) {
//        val configName = defaultDPreference.getPrefString(PREF_CURR_CONFIG_GUID, "")
//        val emptyInfo = VpnNetworkInfo()
//        val info = loadVpnNetworkInfo(configName, emptyInfo)!! + (lastNetworkInfo ?: emptyInfo)
//        saveVpnNetworkInfo(configName, info)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                connectivity.unregisterNetworkCallback(defaultNetworkCallback)
            } catch (ignored: Exception) {
                // ignored
            }
        }

        "stop v2".loge("xxxxxxH")

        SManager.stopVPN()

        if (isForced) {
            //stopSelf has to be called ahead of mInterface.close(). otherwise v2ray core cannot be stooped
            //It's strage but true.
            //This can be verified by putting stopself() behind and call stopLoop and startLoop
            //in a row for several times. You will find that later created v2ray core report port in use
            //which means the first v2ray core somehow failed to stop and release the port.
            stopSelf()

            try {
                mInterface.close()
            } catch (ignored: Exception) {
                // ignored
                "ignored".loge("xxxxxxH")
            }

        }
    }

    @delegate:RequiresApi(Build.VERSION_CODES.P)
    private val defaultNetworkRequest by lazy {
        NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
            .build()
    }

    @delegate:RequiresApi(Build.VERSION_CODES.P)
    private val defaultNetworkCallback by lazy {
        object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                setUnderlyingNetworks(arrayOf(network))
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                // it's a good idea to refresh capabilities
                setUnderlyingNetworks(arrayOf(network))
            }

            override fun onLost(network: Network) {
                setUnderlyingNetworks(null)
            }
        }
    }
}