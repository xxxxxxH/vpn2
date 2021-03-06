package com.puresec.safevpn.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.jackandphantom.circularprogressbar.CircleProgressbar
import com.puresec.safevpn.R
import com.puresec.safevpn.event.IEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import org.greenrobot.eventbus.EventBus

class StatusIngView : LinearLayout {

    private var progressBar: CircleProgressbar? = null

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
        val v = LayoutInflater.from(context).inflate(R.layout.layout_main_content_status_ing, this, true)
        progressBar = v.findViewById(R.id.mainProgressBar)
        progressBar!!.maxProgress = 3f
        startProgress()
        return v
    }

    private fun startProgress() {
        var job: Job? = null
        job = (context as AppCompatActivity).lifecycleScope.launch(Dispatchers.IO) {
            (0..3).asFlow().collect {
                delay(1000)
                withContext(Dispatchers.Main) {
                    progressBar!!.progress = it.toFloat()
                }
                if (it == 3) {
                    EventBus.getDefault().post(IEvent("animDone"))
                    job?.cancel()
                }
            }
        }
    }
}