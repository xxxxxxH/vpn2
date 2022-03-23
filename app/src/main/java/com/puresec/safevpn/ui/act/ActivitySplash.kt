package com.puresec.safevpn.ui.act

import android.content.Intent
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.puresec.safevpn.R
import com.puresec.safevpn.base.IActivity
import com.puresec.safevpn.utils.*
import kotlinx.android.synthetic.main.activity_splash.*

class ActivitySplash : IActivity(R.layout.activity_splash) {

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onConvert() {
        lifecycleScope.requestConfig {
            if (configEntity.needLogin()) {
                showStepTwoImpl()
            } else {
                showStepTwoImpl()
            }
        }

        activitySplashTv.click {
            startActivity(Intent(this@ActivitySplash, ActivityHomepage::class.java))
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