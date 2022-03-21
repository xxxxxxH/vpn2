package net.masvate.vpnpri.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import net.masvate.vpnpri.R

class MainContentView : LinearLayout {
    private var contentView: RelativeLayout? = null
    private var nodeView: MainNodeView? = null
    private var statusView: MainStatusView? = null

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
        val v = LayoutInflater.from(context).inflate(R.layout.layout_content, this, true)
        contentView = v.findViewById(R.id.contentView)
        nodeView = v.findViewById(R.id.nodeView)
        statusView = v.findViewById(R.id.statusView)
        contentView?.addView(StatusDefaultView(context))
        return v
    }

    fun getNodeView(): MainNodeView {
        return nodeView!!
    }

    fun getContentView(): RelativeLayout {
        return contentView!!
    }

    fun getStatusView(): MainStatusView {
        return statusView!!
    }

    fun setStatusWithUi(statusView: View) {
        contentView?.let {
            it.removeAllViews()
            it.addView(statusView)
        }
    }
}