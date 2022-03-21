package com.puresec.safevpn.ui.dlg

import android.app.Activity
import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.flyco.dialog.widget.base.BaseDialog
import com.puresec.safevpn.R
import com.puresec.safevpn.base.IActivity
import com.puresec.safevpn.event.IEvent
import com.puresec.safevpn.utils.click
import com.puresec.safevpn.utils.text
import kotlinx.android.synthetic.main.dialog_exit.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class IDialogDis(context: Context, val activity: Activity) : BaseDialog<IDialogDis>(context) {
    override fun onCreateView(): View {
        widthScale(0.85f)
        EventBus.getDefault().register(this)
        return View.inflate(context, R.layout.dialog_exit, null)
    }

    override fun setUiBeforShow() {
        setCanceledOnTouchOutside(false)
        findViewById<TextView>(R.id.title).text("Are you sure to disconnect?")
        (activity as IActivity).lifecycleScope.launch(Dispatchers.IO){
            activity.getLovinNativeAdView()
        }
        yes.click{ EventBus.getDefault().post(IEvent("disconnectConfirm", this))}
        no.click { dismiss() }
    }

    override fun onBackPressed() {
            return
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