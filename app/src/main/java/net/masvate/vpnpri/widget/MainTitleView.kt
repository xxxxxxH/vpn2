package net.masvate.vpnpri.widget

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import net.masvate.vpnpri.R
import net.masvate.vpnpri.ui.act.ActivitySetting
import net.masvate.vpnpri.utils.click

class MainTitleView :LinearLayout{

    private var settingIcon:ImageView?=null

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
        val v = LayoutInflater.from(context).inflate(R.layout.layout_main_top, this, true)
        settingIcon = v.findViewById(R.id.mainTopOption)
        settingIcon?.click {
            context.startActivity(Intent(context, ActivitySetting::class.java))
        }
        return v
    }
}