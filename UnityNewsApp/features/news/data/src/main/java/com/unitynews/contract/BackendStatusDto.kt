package com.unitynews.contract

import android.os.Parcel
import android.os.Parcelable

data class BackendStatusDto(
    val isRunning: Boolean,
    val scenario: String,
    val articleCount: Int,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        isRunning = parcel.readInt() != 0,
        scenario = parcel.readString().orEmpty(),
        articleCount = parcel.readInt(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(if (isRunning) 1 else 0)
        parcel.writeString(scenario)
        parcel.writeInt(articleCount)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BackendStatusDto> {
        override fun createFromParcel(parcel: Parcel): BackendStatusDto = BackendStatusDto(parcel)

        override fun newArray(size: Int): Array<BackendStatusDto?> = arrayOfNulls(size)
    }
}
