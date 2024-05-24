package com.costas.listmeup.models

import android.os.Parcel
import android.os.Parcelable

data class ProfileDetails(
    var id: String = "",
    var itemName: String = "",
    var category: String = "",
    var acquired: Boolean = false,
    var quantity: Int = 0,
    var estimatedCost: Double = 0.0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt() == 1,
        parcel.readInt(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(itemName)
        parcel.writeString(category)
        parcel.writeInt(if (acquired) 1 else 0)
        parcel.writeInt(quantity)
        parcel.writeDouble(estimatedCost)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ProfileDetails> {
        override fun createFromParcel(parcel: Parcel): ProfileDetails {
            return ProfileDetails(parcel)
        }

        override fun newArray(size: Int): Array<ProfileDetails?> {
            return arrayOfNulls(size)
        }
    }
}