package com.unitynews.contract

import android.os.Parcel
import android.os.Parcelable

/**
 * Parcelable article sent across the reader/backend AIDL boundary.
 *
 * This is a transport DTO, not the domain Article model. Keeping the two
 * separate lets each app evolve its internal model without changing IPC by
 * accident.
 */
data class ArticleDto(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String,
    val rating: Int,
    val placeholderRed: Int,
    val placeholderGreen: Int,
    val placeholderBlue: Int,
) : Parcelable {

    /** Read fields in the exact same order used by writeToParcel. */
    constructor(parcel: Parcel) : this(
        id = parcel.readString().orEmpty(),
        title = parcel.readString().orEmpty(),
        description = parcel.readString().orEmpty(),
        imageUrl = parcel.readString().orEmpty(),
        rating = parcel.readInt(),
        placeholderRed = parcel.readInt(),
        placeholderGreen = parcel.readInt(),
        placeholderBlue = parcel.readInt(),
    )

    /** Write fields in a stable order for the cross-app AIDL contract. */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(imageUrl)
        parcel.writeInt(rating)
        parcel.writeInt(placeholderRed)
        parcel.writeInt(placeholderGreen)
        parcel.writeInt(placeholderBlue)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ArticleDto> {
        override fun createFromParcel(parcel: Parcel): ArticleDto = ArticleDto(parcel)

        override fun newArray(size: Int): Array<ArticleDto?> = arrayOfNulls(size)
    }
}
