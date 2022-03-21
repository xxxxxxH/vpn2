package com.puresec.safevpn.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.itheima.roundedimageview.RoundedImageView
import com.puresec.safevpn.R
import com.puresec.safevpn.utils.ResourceManager

class NodeContainer : LinearLayout {

    private var nodeRoot: RelativeLayout? = null
    private var nodeIcon: RoundedImageView? = null
    private var nodeString: TextView? = null
    private var nodeSignal: ImageView? = null
    private var divider: View? = null

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
        val v = LayoutInflater.from(context).inflate(R.layout.layout_node_point, this, true)
        nodeRoot = v.findViewById(R.id.nodeRoot)
        nodeIcon = v.findViewById(R.id.nodeIcon)
        nodeString = v.findViewById(R.id.nodeString)
        nodeSignal = v.findViewById(R.id.nodeSignal)
        divider = v.findViewById(R.id.divider)
        setSignal()
        setItemClick()
        return v
    }

    private fun setSignal() {
        val index = (0..3).random()
        nodeSignal?.setBackgroundResource(ResourceManager.signals[index])
    }

    private fun setItemClick() {
//        nodeRoot?.setOnClickListener {
//            (context as AppCompatActivity).finish()
//        }
    }

    fun setIcon(id: Int) {
        nodeIcon?.let {
            Glide.with(context).load(id).into(it)
        }
    }

    fun setString(content: String) {
        nodeString?.let {
            it.text = content
        }
    }

    fun setDividerShow(show: Boolean) {
        divider?.let {
            it.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    fun getRoot():RelativeLayout?{
        return nodeRoot
    }
}