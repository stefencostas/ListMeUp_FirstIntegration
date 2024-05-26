package com.costas.listmeup.models

import android.os.Parcel
import android.os.Parcelable

data class ShoppingItem(
    var id: String = "",
    var userId: String = "",
    var itemName: String = "",
    var category: String = "",
    var acquired: Boolean = false,
    var quantity: Int = 0,
    var estimatedCost: Double = 0.0
) : Parcelable {

    constructor(map: Map<String, Any>) : this(
        map["id"] as String,
        map["userId"] as String,
        map["itemName"] as String,
        map["category"] as String,
        map["acquired"] as Boolean,
        map["quantity"] as Int,
        map["estimatedCost"] as Double
    )

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt() != 0,
        parcel.readInt(),
        parcel.readDouble()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(userId)
        parcel.writeString(itemName)
        parcel.writeString(category)
        parcel.writeInt(if (acquired) 1 else 0)
        parcel.writeInt(quantity)
        parcel.writeDouble(estimatedCost)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ShoppingItem> {
        override fun createFromParcel(parcel: Parcel): ShoppingItem {
            return ShoppingItem(parcel)
        }

        override fun newArray(size: Int): Array<ShoppingItem?> {
            return arrayOfNulls(size)
        }
    }
}