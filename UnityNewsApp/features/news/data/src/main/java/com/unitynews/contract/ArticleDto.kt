package com.unitynews.contract

import android.os.Parcel
import android.os.Parcelable

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
