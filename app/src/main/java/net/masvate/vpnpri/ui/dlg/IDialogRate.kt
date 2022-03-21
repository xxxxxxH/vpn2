package net.masvate.vpnpri.ui.dlg

import android.content.Context
import android.view.View
import com.flyco.dialog.widget.base.BaseDialog
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_exit.*
import net.masvate.vpnpri.R
import net.masvate.vpnpri.utils.click

class IDialogRate(context: Context) : BaseDialog<IDialogRate>(context) {
    override fun onCreateView(): View {
        widthScale(0.85f)
        return View.inflate(context, R.layout.dialog_rate_us, null)
    }

    override fun setUiBeforShow() {
        setCanceledOnTouchOutside(false)
        yes.click {
            dismiss()
            Toasty.success(context, "Thank you").show()
        }
        no.click {
            dismiss()
        }
    }
}