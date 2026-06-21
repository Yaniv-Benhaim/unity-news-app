package com.unitynews.contract

import android.os.Parcel
import android.os.Parcelable

/**
 * Parcelable backend status used by the shared AIDL contract.
 *
 * The frontend currently focuses on article reads, but keeping this DTO in the
 * contract makes the backend/control app status API explicit and testable.
 */
data class BackendStatusDto(
    val isRunning: Boolean,
    val scenario: String,
    val articleCount: Int,
) : Parcelable {
    /** Read fields in the exact same order used by writeToParcel. */
    constructor(parcel: Parcel) : this(
        isRunning = parcel.readInt() != 0,
        scenario = parcel.readString().orEmpty(),
        articleCount = parcel.readInt(),
    )

    /** Write fields in a stable order for the cross-app AIDL contract. */
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
