package com.puresec.safevpn.ui.act

import android.content.Intent
import android.net.Uri
import android.view.View
import com.puresec.safevpn.R
import com.puresec.safevpn.base.IActivity
import com.puresec.safevpn.ui.dlg.IDialogRate
import kotlinx.android.synthetic.main.activity_setting_new.*

class ActivityOther : IActivity(R.layout.activity_setting_new) ,View.OnClickListener{
    
    private val rateDialog by lazy {
        IDialogRate(this)
    }

    override fun onConvert() {
        settingCloseIv.setOnClickListener(this)
        settingRateRl.setOnClickListener(this)
        settingAboutRl.setOnClickListener(this)
        settingShareRl.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when(v.id){
            R.id.settingCloseIv -> {
                finish()
            }
            R.id.settingRateRl -> {
                rateDialog.show()
            }
            R.id.settingAboutRl -> {

            }
            R.id.settingShareRl -> {
                sendEmil("Life is a fking moving")
            }
        }
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
        startActivity(Intent.createChooser(intent, "Choose Email Client"))
    }

}