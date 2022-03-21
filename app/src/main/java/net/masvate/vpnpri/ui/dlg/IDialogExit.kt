package net.masvate.vpnpri.ui.dlg

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.flyco.dialog.widget.base.BaseDialog
import kotlinx.android.synthetic.main.dialog_exit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import net.masvate.vpnpri.R
import net.masvate.vpnpri.base.IActivity
import net.masvate.vpnpri.event.IEvent
import net.masvate.vpnpri.utils.click
import net.masvate.vpnpri.utils.text

class IDialogExit(context: Context, val activity: Activity): BaseDialog<IDialogExit>(context) {
    override fun onCreateView(): View {
        widthScale(0.85f)
        EventBus.getDefault().register(this)
        return View.inflate(context, R.layout.dialog_exit, null)
    }

    override fun setUiBeforShow() {
        setCanceledOnTouchOutside(false)
        findViewById<TextView>(R.id.title).text("Are you sure to exit the application?")
        (activity as IActivity).lifecycleScope.launch(Dispatchers.IO){
            activity.getLovinNativeAdView()
        }
        yes.click { EventBus.getDefault().post(IEvent("confirmExit",this)) }
        no.click { dismiss() }
    }

    override fun onBackPressed() {

    }

    override fun dismiss() {
        super.dismiss()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: IEvent) {
        val msg = e.getMessage()
        when (msg[0]) {
            "onNativeAdLoaded" -> {
                if (nad.childCount == 0){
                    nad.addView(msg[1] as View)
                }
            }
        }
    }
}