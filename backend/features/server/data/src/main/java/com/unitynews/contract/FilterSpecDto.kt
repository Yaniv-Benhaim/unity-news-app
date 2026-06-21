package com.unitynews.contract

import android.os.Parcel
import android.os.Parcelable

/**
 * Parcelable filter definition sent by the backend over AIDL.
 *
 * The reader app converts this transport DTO into the domain FilterSpec model
 * before presentation code sees it.
 */
data class FilterSpecDto(
    val key: String,
    val label: String,
    val type: String,
    val options: List<String>,
) : Parcelable {
    /** Read fields in the exact same order used by writeToParcel. */
    constructor(parcel: Parcel) : this(
        key = parcel.readString().orEmpty(),
        label = parcel.readString().orEmpty(),
        type = parcel.readString().orEmpty(),
        options = parcel.createStringArrayList().orEmpty(),
    )

    /** Write fields in a stable order for the cross-app AIDL contract. */
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
