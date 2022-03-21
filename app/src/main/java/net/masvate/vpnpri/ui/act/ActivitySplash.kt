package net.masvate.vpnpri.ui.act

import android.content.Intent
import android.view.View
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_splash.*
import net.masvate.vpnpri.R
import net.masvate.vpnpri.base.IActivity
import net.masvate.vpnpri.utils.*

class ActivitySplash : IActivity(R.layout.activity_splash) {

    override fun onConvert() {
        lifecycleScope.requestConfig {
            if (configEntity.needLogin()) {
                if (configEntity.needDeepLink() && configEntity.faceBookId().isNotBlank()) {
                    fetchAppLink(configEntity.faceBookId()) {
                        it?.let {
                            isRealDeepLink = true
                        }
                        showStepTwoImpl()
                    }
                } else {
                    showStepTwoImpl()
                }
            } else {
                showStepTwoImpl()
            }
        }

        activitySplashTv.click {
            startActivity(Intent(this@ActivitySplash, ActivityHome::class.java))
            finish()
        }
    }

    private var isByShowStepTwo = false

    private fun showStepTwoImpl() {
        showStepTwo {
            if (showOpenAd(activitySplashRl, isForce = true)) {
                isByShowStepTwo = true
            } else {
                jumpByConfigImpl()
            }
        }
    }

    private fun jumpByConfigImpl() {
        jumpByConfig({
            activitySplashPb.visibility = View.GONE
            activitySplashLl.visibility = View.VISIBLE
        }, {
            startActivity(Intent(this@ActivitySplash, ActivityHomepage::class.java))
            finish()
        })
    }

    override fun onSplashAdHidden() {
        super.onSplashAdHidden()
        if (isByShowStepTwo) {
            isByShowStepTwo = false
            jumpByConfigImpl()
        }
    }

    override fun onInterstitialAdHidden() {
        super.onInterstitialAdHidden()
        if (isByShowStepTwo) {
            isByShowStepTwo = false
            jumpByConfigImpl()
        }
    }


}