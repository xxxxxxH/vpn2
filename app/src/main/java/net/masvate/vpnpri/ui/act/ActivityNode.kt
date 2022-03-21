package net.masvate.vpnpri.ui.act

import android.graphics.Typeface
import android.os.Build
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import kotlinx.android.synthetic.main.activity_node.*
import net.masvate.vpnpri.R
import net.masvate.vpnpri.base.IActivity
import net.masvate.vpnpri.event.IEvent
import net.masvate.vpnpri.utils.*
import net.masvate.vpnpri.widget.NodeContainer
import org.greenrobot.eventbus.EventBus

@RequiresApi(Build.VERSION_CODES.N)
class ActivityNode : IActivity(R.layout.activity_node) {
    override fun onConvert() {
        rootView.addView(setTitleLayout())
        setLayout()
    }

    private fun setLayout(){
        val scrollView = ScrollView(this)
        val p = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)
        scrollView.layoutParams = p
        scrollView.isScrollbarFadingEnabled = true
        val linearLayout = LinearLayout(this)
        val p1 = ViewGroup.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT)
        linearLayout.layoutParams = p1
        linearLayout.orientation = LinearLayout.VERTICAL
        setContentLayout(linearLayout)
        scrollView.addView(linearLayout)
        rootView.addView(scrollView)
    }


    private fun setContentLayout(parent:LinearLayout){
        ResourceManager.getRes().apply {
            forEach { (key, value) ->
                val item = NodeContainer(this@ActivityNode)
                item.setIcon(value)
                item.setString(key)
                item.itemClick {
                    "$key, $value".loge("xxxxxxH")
                    EventBus.getDefault().post(IEvent("nodeInfo", key, value))
                    runOnUiThread { finish() }
                }
                parent.addView(item)
            }
            val lastItem = parent.getChildAt(parent.childCount -1) as NodeContainer
            lastItem.setDividerShow(false)
        }

    }

    private fun setTitleLayout():RelativeLayout{
        val titleRelativeLayout = RelativeLayout(this)
        val p = LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
        p.topMargin = dp2px(this, 40f)
        titleRelativeLayout.layoutParams = p
        titleRelativeLayout.addView(createTitleBackIcon())
        titleRelativeLayout.addView(createTitleTextView())
        return titleRelativeLayout
    }

    private fun createTitleBackIcon():ImageView{
        val imageView = ImageView(this)
        val p = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
        p.addRule(RelativeLayout.CENTER_VERTICAL)
        p.marginStart = dp2px(this, 20f)
        imageView.layoutParams = p
        imageView.setBackgroundResource(R.drawable.arrow_l)
        imageView.setOnClickListener { finish() }
        return imageView
    }

    private fun createTitleTextView():TextView{
        val textView = TextView(this)
        val p = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
        p.addRule(RelativeLayout.CENTER_HORIZONTAL)
        textView.layoutParams = p
        textView.text = "Connect"
        textView.textSize = 20f
        textView.setTextColor(resources.getColor(R.color.white))
        textView.typeface = Typeface.DEFAULT_BOLD
        return textView
    }
}