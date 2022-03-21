package net.masvate.vpnpri.ui.act

import android.annotation.SuppressLint
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_loaction.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import net.masvate.vpnpri.R
import net.masvate.vpnpri.base.IActivity
import net.masvate.vpnpri.event.IEvent
import net.masvate.vpnpri.holder.Ron
import net.masvate.vpnpri.ibean.ResourceEntity
import net.masvate.vpnpri.utils.ResourceManager
import net.masvate.vpnpri.utils.isLogin
import zhan.auto_adapter.AutoRecyclerAdapter

class HarryPotterLocation : IActivity(R.layout.activity_loaction) {

    var data: ArrayList<ResourceEntity>? = null

    @SuppressLint("NotifyDataSetChanged")
    override fun onConvert() {
        EventBus.getDefault().register(this)
//        lifecycleScope.launch(Dispatchers.IO){
//            val banner = App.instance!!.lovinBanner()
//            banner.loadAd()
//            withContext(Dispatchers.Main){
//                if (adView.childCount == 0){
//                    adView.addView(banner)
//                }
//            }
//        }
        val adapter = AutoRecyclerAdapter()
        recycler.adapter = adapter
        recycler.layoutManager = LinearLayoutManager(this)
        adapter.setHolder(Ron::class.java, R.layout.layout_item_location)
        data = ResourceManager.getResource()
        adapter.setDataList(Ron::class.java, data).notifyDataSetChanged()
        locationCloseIv.setOnClickListener { finish() }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: IEvent) {
        val msg = e.getMessage()
        if (msg[0] == "itemClick") {
            if (isLogin) {
                val entity = msg[1] as ResourceEntity
                MMKV.defaultMMKV().encode("location", entity)
                EventBus.getDefault().post(
                    IEvent(
                        "location",
                        entity
                    )
                )
            } else {
                startActivity(Intent(this, ActivityLo::class.java))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}