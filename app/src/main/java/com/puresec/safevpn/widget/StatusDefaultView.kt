package com.puresec.safevpn.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.puresec.safevpn.R
import com.puresec.safevpn.event.IEvent
import com.puresec.safevpn.utils.startClick
import org.greenrobot.eventbus.EventBus

class StatusDefaultView :LinearLayout{

    private var startConnect:RelativeLayout?=null

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

    private fun initView(context: Context) :View{
        val v = LayoutInflater.from(context).inflate(R.layout.layout_main_content_status_default, this, true)
        startConnect = v.findViewById(R.id.mainOptionRl)
        startConnect!!.startClick {
            EventBus.getDefault().post(IEvent("startConnect"))
        }
        return v
    }
}