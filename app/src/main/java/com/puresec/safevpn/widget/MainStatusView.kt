package com.puresec.safevpn.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.puresec.safevpn.R

class MainStatusView : LinearLayout {

    private var currentStatus: TextView? = null

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
        val v = LayoutInflater.from(context).inflate(R.layout.layout_status, this, true)
        currentStatus = v.findViewById(R.id.mainStatusCurrentTv)
        return v
    }

    fun setStatus(s: String) {
        currentStatus?.let {
            it.apply {
                text = s
            }
        }
    }

    fun getStatus(): String {
        return currentStatus?.text.toString()
    }
}