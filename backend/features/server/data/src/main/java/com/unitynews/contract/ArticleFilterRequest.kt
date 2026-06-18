package com.unitynews.contract

import android.os.Parcel
import android.os.Parcelable

data class ArticleFilterRequest(
    val titleQuery: String?,
    val ratingValues: List<Int>,
    val requestId: String,
) : Parcelable {
    constructor(parcel: Parcel) : this(
        titleQuery = parcel.readString(),
        ratingValues = parcel.createIntArray()?.toList().orEmpty(),
        requestId = parcel.readString().orEmpty(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(titleQuery)
        parcel.writeIntArray(ratingValues.toIntArray())
        parcel.writeString(requestId)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ArticleFilterRequest> {
        override fun createFromParcel(parcel: Parcel): ArticleFilterRequest =
            ArticleFilterRequest(parcel)

        override fun newArray(size: Int): Array<ArticleFilterRequest?> = arrayOfNulls(size)
    }
}
