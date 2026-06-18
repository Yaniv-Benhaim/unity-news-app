package com.unitynews.contract

import android.os.Parcel
import android.os.Parcelable

data class FilterSpecDto(
    val key: String,
    val label: String,
    val type: String,
    val options: List<String>,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        key = parcel.readString().orEmpty(),
        label = parcel.readString().orEmpty(),
        type = parcel.readString().orEmpty(),
        options = parcel.createStringArrayList().orEmpty(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(key)
        parcel.writeString(label)
        parcel.writeString(type)
        parcel.writeStringList(options)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FilterSpecDto> {
        override fun createFromParcel(parcel: Parcel): FilterSpecDto = FilterSpecDto(parcel)

        override fun newArray(size: Int): Array<FilterSpecDto?> = arrayOfNulls(size)
    }
}
