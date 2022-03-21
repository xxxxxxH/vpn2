package com.puresec.safevpn.widget

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.puresec.safevpn.R
import com.puresec.safevpn.ui.dlg.IDialogRate
import com.puresec.safevpn.utils.click

class SettingItemView : LinearLayout {

    private var settingItemRoot: RelativeLayout? = null
    private var settingItemIcon: ImageView? = null
    private var settingItemTv: TextView? = null

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
        val v = LayoutInflater.from(context).inflate(R.layout.layout_setting_item, this, true)
        settingItemRoot = v.findViewById(R.id.settingItemRoot)
        settingItemIcon = v.findViewById(R.id.settingItemIcon)
        settingItemTv = v.findViewById(R.id.settingItemTv)
        settingItemRoot?.click {
            when (settingItemTv?.text) {
                "Rate Us" -> {
                    IDialogRate(context).show()
                }
                "Tell Your Friends" -> {
                    sendEmil("Blow Me a Kiss")
                }
            }
        }
        return v
    }

    fun setItemIcon(id: Int) {
        settingItemIcon?.setImageResource(id)
    }

    fun setItemTv(s: String) {
        settingItemTv?.text = s
    }

    private fun sendEmil(text: String) {
        val reveiverString = arrayOf("nuclearvpnp@outlook.com")
        val ccString = arrayOf<String>()
        val subjectString = "feedBook"
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")
        intent.putExtra(Intent.EXTRA_EMAIL, reveiverString)
        intent.putExtra(Intent.EXTRA_CC, ccString)
        intent.putExtra(Intent.EXTRA_SUBJECT, subjectString)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(intent, "Choose Email Client"))
    }
}