package net.masvate.vpnpri.ibean

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
@Parcelize
data class ResourceEntity(var id: Int = 0, var name: String = "") : Serializable, Parcelable
