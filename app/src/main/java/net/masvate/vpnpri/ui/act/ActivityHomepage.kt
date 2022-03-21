package net.masvate.vpnpri.ui.act

import android.content.Intent
import android.net.VpnService
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.miaomiao.vpn.SManager
import es.dmoral.prefs.Prefs
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.layout_content.*
import net.masvate.vpnpri.R
import net.masvate.vpnpri.base.IActivity
import net.masvate.vpnpri.event.IEvent
import net.masvate.vpnpri.ui.dlg.IDialogDis
import net.masvate.vpnpri.ui.dlg.IDialogExit
import net.masvate.vpnpri.utils.*
import net.masvate.vpnpri.widget.StatusDefaultView
import net.masvate.vpnpri.widget.StatusEdView
import net.masvate.vpnpri.widget.StatusIngView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@RequiresApi(Build.VERSION_CODES.N)
class ActivityHomepage :IActivity(R.layout.activity_home){

    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                startVPN()
            }
        }


    override fun onConvert() {
        EventBus.getDefault().register(this)
        setNodeView()
        setStatusView(STATUS.DEFAULT)
    }


    private fun setNodeView(){
        if (locationIconId == 0 && nodeName == ""){
            nodeView.setLocationIcon(0)
            nodeView.setNodeName("Select Location")
        }else{
            nodeView.setLocationIcon(locationIconId)
            nodeView.setNodeName(nodeName)
        }
    }

    private fun setStatusView(status:STATUS){
        when(status){
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
                showInsertDelay{ showInsertAd() }
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
        when(msg[0]){
            "startConnect" ->{
                if (nodeName == ""){
                    Toasty.error(this,"Please Select Node").show()
                    return
                }else{
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
                Prefs.with(this).write("nodeName",nodeName)
                val id = msg[2] as Int
                Prefs.with(this).writeInt("id",id)
                setStatusView(STATUS.ING)
                setNodeView()
            }
            "animDone" -> {
                setStatusView(STATUS.CONNECTED)
            }
            "shutdown" -> {
                setStatusView(STATUS.DEFAULT)
                Toasty.error(this,"error").show()
            }
            "startService" -> {

            }
            "confirmExit" -> {
                val dialog = msg[1] as IDialogExit
                if (dialog.isShowing){
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
                if (mainContentView.getStatusView().getStatus() == " Connecting"){
                    Toasty.info(this,"vpn starting, please wait").show()
                }else{
                    startActivity(Intent(this, ActivityNode::class.java))
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