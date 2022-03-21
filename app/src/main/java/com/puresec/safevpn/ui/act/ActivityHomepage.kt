package com.puresec.safevpn.ui.act

import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.puresec.safevpn.R
import com.puresec.safevpn.base.IActivity
import com.puresec.safevpn.event.IEvent
import com.puresec.safevpn.ui.dlg.IDialogDis
import com.puresec.safevpn.ui.dlg.IDialogExit
import com.puresec.safevpn.utils.*
import com.puresec.safevpn.widget.StatusDefaultView
import com.puresec.safevpn.widget.StatusEdView
import com.puresec.safevpn.widget.StatusIngView
import es.dmoral.prefs.Prefs
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.layout_content.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@RequiresApi(Build.VERSION_CODES.N)
class ActivityHomepage : IActivity(R.layout.activity_home) {

    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                startVPN()
            }
        }


    override fun onConvert() {
        EventBus.getDefault().register(this)
        lifecycleScope.launch(Dispatchers.IO) {
            getLovinNativeAdView()
        }
        setNodeView()
        setStatusView(STATUS.DEFAULT)
    }


    private fun setNodeView() {
        if (locationIconId == 0 && nodeName == "") {
            nodeView.setLocationIcon(0)
            nodeView.setNodeName("Select Location")
        } else {
            nodeView.setLocationIcon(locationIconId)
            nodeView.setNodeName(nodeName)
        }
    }

    private fun setStatusView(status: STATUS) {
        when (status) {
            STATUS.DEFAULT -> {
                mainContentView.setStatusWithUi(StatusDefaultView(this))
                mainContentView.getStatusView().setStatus(" Not Connected")
            }
            STATUS.ING -> {
                mainContentView.setStatusWithUi(StatusIngView(this))
                mainContentView.getStatusView().setStatus(" Connecting")
            }
            STATUS.CONNECTED -> {
                mainContentView.setStatusWithUi(StatusEdView(this))
                mainContentView.getStatusView().setStatus(" Connected")
                showInsertDelay { showInsertAd() }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    override fun onBackPressed() {
        IDialogExit(this, this).show()
    }

    private fun startVPN() {
        SManager.startService(this)
    }

    private fun stopVPN() {
        SManager.stopVPN()
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: IEvent) {
        val msg = e.getMessage()
        when (msg[0]) {
            "startConnect" -> {
                if (nodeName == "") {
                    Toasty.error(this, "Please Select Node").show()
                    return
                } else {
                    val intent = VpnService.prepare(this)
                    intent.loge("xxxxxxH")
                    if (intent == null)
                        startVPN()
                    else
                        requestVpnPermission.launch(intent)
                    setStatusView(STATUS.ING)
                }
            }
            "disconnect" -> {
                IDialogDis(this, this).show()
            }
            "nodeInfo" -> {
                val nodeName = msg[1].toString()
                Prefs.with(this).write("nodeName", nodeName)
                val id = msg[2] as Int
                Prefs.with(this).writeInt("id", id)
                if (!isLogin) {
                    if (configEntity.needLogin()) {
                        if (configEntity.needDeepLink() && configEntity.faceBookId().isNotBlank()) {
                            if (isRealDeepLink) {
                                startActivity(Intent(this, ActivityLo::class.java))
                                return
                            }
                        } else {
                            startActivity(Intent(this, ActivityLo::class.java))
                            return
                        }
                    }
                }
                EventBus.getDefault().post(IEvent("startConnect"))
                setStatusView(STATUS.ING)
                setNodeView()
            }
            "animDone" -> {
                setStatusView(STATUS.CONNECTED)
            }
            "shutdown" -> {
                setStatusView(STATUS.DEFAULT)
                Toasty.error(this, "error").show()
            }
            "startService" -> {

            }
            "confirmExit" -> {
                val dialog = msg[1] as IDialogExit
                if (dialog.isShowing) {
                    dialog.dismiss()
                }
                finish()
            }
            "disconnectConfirm" -> {
                val dialog = msg[1] as IDialogDis
                if (dialog.isShowing)
                    dialog.dismiss()
                stopVPN()
                setStatusView(STATUS.DEFAULT)
            }
            "routeNode" -> {
                if (mainContentView.getStatusView().getStatus() == " Connecting") {
                    Toasty.info(this, "vpn starting, please wait").show()
                } else {
                    val a = showInsertAd()
                    if (a) {

                    } else {
                        startActivity(Intent(this, ActivityNode::class.java))
                    }
                }
            }
            "onNativeAdLoaded" -> {
                if (lovinNativeAdViewFl.childCount == 0) {
                    lovinNativeAdViewFl.addView(msg[1] as View)
                }
            }
        }
    }

    enum class STATUS {
        DEFAULT,
        ING,
        CONNECTED
    }
}