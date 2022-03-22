package com.puresec.safevpn.widget

import android.content.Context
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.puresec.safevpn.R
import com.puresec.safevpn.event.IEvent
import com.puresec.safevpn.utils.click
import com.shehuan.niv.NiceImageView
import org.greenrobot.eventbus.EventBus


@RequiresApi(Build.VERSION_CODES.N)
class MainNodeView : LinearLayout {

    private var selectNode: RelativeLayout? = null

    private var nodeName: TextView? = null

    private var location: NiceImageView? = null

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
        val v = LayoutInflater.from(context).inflate(R.layout.layout_node, this, true)
        selectNode = v.findViewById(R.id.mainLocationRl)
        selectNode?.click { EventBus.getDefault().post(IEvent("routeNode")) }
        nodeName = v.findViewById(R.id.mainLocationTv)
        location = v.findViewById(R.id.mainLocationFlagIv)
        return v
    }

    fun setNodeName(s: String) {
        nodeName?.let {
            it.apply {
                text = s
            }
        }
    }

    fun setLocationIcon(id: Int) {
        Glide.with(this)
            .load(id)
            .into(location!!)
    }
}