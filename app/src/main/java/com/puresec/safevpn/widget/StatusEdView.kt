package com.puresec.safevpn.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.puresec.safevpn.R
import com.puresec.safevpn.event.IEvent
import com.puresec.safevpn.utils.timeOffset
import com.puresec.safevpn.utils.toPatternString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

class StatusEdView : LinearLayout {
    private var countDown: TextView? = null
    private var stopVpn: ImageView? = null
    private var connectedTime = -1L

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(context)
    }

    private fun initView(context: Context): View {
        val v = LayoutInflater.from(context).inflate(R.layout.layout_main_content_status_connected, this, true)
        countDown = v.findViewById(R.id.mainStatusTimeTv)
        stopVpn = v.findViewById(R.id.mainStatusIcon)
        startCountDown()
        stopVpn()
        return v
    }


    fun startCountDown() {
        var countDownJob: Job? = null
        countDownJob?.cancel()
        connectedTime = System.currentTimeMillis()
        countDownJob = (context as AppCompatActivity).lifecycleScope.launch(Dispatchers.IO) {
            repeat(Int.MAX_VALUE) {
                (System.currentTimeMillis() - connectedTime)
                    .timeOffset()
                    .toPatternString()
                    .let {
                        withContext(Dispatchers.Main) {
                            countDown?.text = it
                        }
                    }
            }
        }
    }

    fun stopVpn() {
        stopVpn?.let {
            it.apply {
                setOnClickListener {
                    EventBus.getDefault().post(IEvent("disconnect"))
                }
            }
        }
    }
}