package net.masvate.vpnpri.ui.act

import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.activity_setting.*
import net.masvate.vpnpri.R
import net.masvate.vpnpri.base.IActivity
import net.masvate.vpnpri.utils.dp2px
import net.masvate.vpnpri.widget.SettingItemView

class ActivitySetting : IActivity(R.layout.activity_setting) {

    override fun onConvert() {
        rootView.addView(createCloseImageview())
        rootView.addView(createIconImageview())
        rootView.addView(createItem("Rate Us",R.drawable.setting_rate))
        rootView.addView(createItem("About Us",R.drawable.setting_about))
        rootView.addView(createItem("Tell Your Friends",R.drawable.setting_share))
    }

    private fun createCloseImageview(): ImageView {
        val imageView = ImageView(this)
        val p =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.marginStart = dp2px(this, 20f)
        p.topMargin = dp2px(this, 33f)
        imageView.layoutParams = p
        imageView.setImageResource(R.drawable.setting_close)
        imageView.setOnClickListener { finish() }
        return imageView
    }

    private fun createIconImageview(): ImageView {
        val imageView = ImageView(this)
        val p =
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        p.gravity = Gravity.CENTER_HORIZONTAL
        p.topMargin = dp2px(this, 120f)
        p.bottomMargin = dp2px(this, 70f)
        imageView.layoutParams = p
        imageView.setImageResource(R.drawable.setting_icon)
        return imageView
    }

    private fun createItem(content:String, id:Int):SettingItemView{
        val item = SettingItemView(this)
        item.setItemIcon(id)
        item.setItemTv(content)
        return item
    }
}