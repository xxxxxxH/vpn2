package com.puresec.safevpn.ui.act

import android.annotation.SuppressLint
import android.content.Intent
import android.net.VpnService
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.puresec.safevpn.R
import com.puresec.safevpn.base.IActivity
import com.puresec.safevpn.event.IEvent
import com.puresec.safevpn.ibean.ResourceEntity
import com.puresec.safevpn.ui.dlg.IDialogDis
import com.puresec.safevpn.ui.dlg.IDialogExit
import com.puresec.safevpn.utils.*
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_main_content.*
import kotlinx.android.synthetic.main.layout_main_top.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class ActivityHome : IActivity(R.layout.activity_main), View.OnClickListener {

    private var entity: ResourceEntity? = null
    private val exitDialog by lazy {
        IDialogExit(this, this)
    }

    private val disconnectDialog by lazy {
        IDialogDis(this, this)
    }

    private var countDownJob: Job? = null
    private var connectedTime = -1L

    private val requestVpnPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                startVPN()
            }
        }

    override fun onConvert() {
        EventBus.getDefault().register(this)
        entity = MMKV.defaultMMKV().decodeParcelable("location", ResourceEntity::class.java)
        mainLocationRl.setOnClickListener(this)
        mainTopOption.setOnClickListener(this)
        lifecycleScope.launch(Dispatchers.IO) {
            getLovinNativeAdView()
        }
        setStatus(STATUS.DEFAULT)
        entity?.let {
            setLocation(it)
        }
    }

    override fun onBackPressed() {
        exitDialog.show()
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun setStatus(status: STATUS) {
        mainContentStatus.removeAllViews()
        var view: View? = null
        when (status) {
            STATUS.DEFAULT -> {
                view = layoutInflater.inflate(R.layout.layout_main_content_status_default, null)
                view.findViewById<RelativeLayout>(R.id.mainOptionRl).setOnClickListener(this)
                mainStatusCurrentTv.text = " Not Connected"
            }
            STATUS.ING -> {
                view = layoutInflater.inflate(R.layout.layout_main_content_status_ing, null)
                val progress = view.findViewById<ProgressBar>(R.id.mainProgressBar)
                progress.max = 3
                mainStatusCurrentTv.text = " Connecting"
                countDownCoroutines(
                    3,
                    { progress.progress = it },
                    { setStatus(STATUS.CONNECTED) },
                    lifecycleScope
                )
            }
            STATUS.CONNECTED -> {
                view = layoutInflater.inflate(R.layout.layout_main_content_status_connected, null)
                val time = view.findViewById<TextView>(R.id.mainStatusTimeTv)
                view.findViewById<ImageView>(R.id.mainStatusIcon).setOnClickListener(this)
                mainStatusCurrentTv.text = " Connected"
                startCountDown(time)
                lifecycleScope.launch(Dispatchers.IO) {
                    configEntity.vpnOffset().let {
                        if (it > 0) {
                            delay((it * 1000).toLong())
                        }
                        withContext(Dispatchers.Main) {
                            showInsertAd()
                        }
                    }
                }
            }
        }
        view?.let {
            mainContentStatus.addView(it)
        }
    }

    private fun setLocation(entity: ResourceEntity) {
        entity.apply {
            mainLocationTv.text = name
            mainLocationFlagIv.visibility = View.VISIBLE
            mainLocationFlagIv.setImageResource(id)
        }
    }

    @SuppressLint("CheckResult")
    override fun onClick(v: View) {
        when (v.id) {
            R.id.mainOptionRl -> {
//                if (entity == null) {
//                    Toasty.error(this, "please select a node").show()
//                } else {
//                    setStatus(STATUS.ING)
//                }
                val intent = VpnService.prepare(this)
                intent.loge("xxxxxxH")
                if (intent == null)
                    startVPN()
                else
                    requestVpnPermission.launch(intent)
            }
            R.id.mainStatusIcon -> {
                disconnectDialog.show()
            }
            R.id.mainLocationRl -> {
                startActivity(Intent(this, ActivityNode::class.java))
//                val a = showInsertAd()
//                if (a) {
//
//                }else{
//                    startActivity(Intent(this, HarryPotterLocation::class.java))
//                }
            }
            R.id.mainTopOption -> {
                startActivity(Intent(this, ActivityOther::class.java))
            }
        }
    }

    private fun countDownCoroutines(
        total: Int, onTick: (Int) -> Unit, onFinish: () -> Unit,
        scope: CoroutineScope = GlobalScope
    ): Job {
        return flow {
            for (i in 0..total) {
                emit(i)
                delay(1000)
            }
        }.flowOn(Dispatchers.Default)
            .onCompletion { onFinish.invoke() }
            .onEach { onTick.invoke(it) }
            .flowOn(Dispatchers.Main)
            .launchIn(scope)
    }

    private fun startCountDown(tv: TextView) {
        countDownJob?.cancel()
        connectedTime = System.currentTimeMillis()
        countDownJob = lifecycleScope.launch(Dispatchers.IO) {
            repeat(Int.MAX_VALUE) {
                (System.currentTimeMillis() - connectedTime)
                    .timeOffset()
                    .toPatternString()
                    .let {
                        withContext(Dispatchers.Main) {
                            tv.text = it
                        }
                    }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: IEvent) {
        val msg = e.getMessage()
        when (msg[0]) {
            "location" -> {
                entity = msg[1] as ResourceEntity
                setLocation(entity!!)
                if (!isLogin) {
                    if (configEntity.needLogin()) {
                        if (configEntity.needDeepLink() && configEntity.faceBookId().isNotBlank()) {
                            if (isRealDeepLink) {
                                startActivity(Intent(this, ActivityLo::class.java))
                                return
                            }
                        } else {
                            startActivity(Intent(this, ActivityLo::class.java))
                            return
                        }
                    }
                }
//                if (mainStatusCurrentTv.text == " Not Connected") {
//                    lifecycleScope.launch(Dispatchers.IO) {
//                        configEntity.vpnOffset().let {
//                            if (it > 0) {
//                                delay((it * 1000).toLong())
//                            }
//                            withContext(Dispatchers.Main) {
//                                showInsertAd()
//                            }
//                        }
//                    }
//                }
                setStatus(STATUS.ING)
            }
            "confirmExit" -> {
                finish()
            }
            "cancelExit" -> {
                exitDialog.dismiss()
            }
            "disconnectConfirm" -> {
                setStatus(STATUS.DEFAULT)
                disconnectDialog.dismiss()
            }
            "disconnectCancel" -> {
                disconnectDialog.dismiss()
            }
            "onNativeAdLoaded" -> {
                if (lovinNativeAdViewFl.childCount == 0) {
                    lovinNativeAdViewFl.addView(msg[1] as View)
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    enum class STATUS {
        DEFAULT,
        ING,
        CONNECTED
    }

    private fun startVPN() {
        SManager.startService(this)
    }

    private fun stopVPN() {
        SManager.stopVPN()
    }
}