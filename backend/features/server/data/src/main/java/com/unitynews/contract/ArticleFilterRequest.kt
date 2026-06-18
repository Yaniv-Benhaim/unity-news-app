package com.unitynews.contract

import android.os.Parcel
import android.os.Parcelable

data class ArticleFilterRequest(
    val titleQuery: String?,
    val ratingValues: List<Int>,
    val requestId: String,
    val dynamicValues: Map<String, List<String>> = emptyMap(),
) : Parcelable {
    constructor(parcel: Parcel) : this(
        titleQuery = parcel.readString(),
        ratingValues = parcel.createIntArray()?.toList().orEmpty(),
        requestId = parcel.readString().orEmpty(),
        dynamicValues = parcel.readDynamicValues(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(titleQuery)
        parcel.writeIntArray(ratingValues.toIntArray())
        parcel.writeString(requestId)
        parcel.writeDynamicValues(dynamicValues)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ArticleFilterRequest> {
        override fun createFromParcel(parcel: Parcel): ArticleFilterRequest =
            ArticleFilterRequest(parcel)

        override fun newArray(size: Int): Array<ArticleFilterRequest?> = arrayOfNulls(size)
    }
}

private fun Parcel.writeDynamicValues(values: Map<String, List<String>>) {
    val sortedValues = values.toSortedMap()
    writeInt(sortedValues.size)
    sortedValues.forEach { (key, entryValues) ->
        writeString(key)
        writeStringList(entryValues)
    }
}

private fun Parcel.readDynamicValues(): Map<String, List<String>> {
    val size = readInt()
    if (size <= 0) {
        return emptyMap()
    }
    val values = linkedMapOf<String, List<String>>()
    repeat(size) {
        val key = readString()
        val entryValues = createStringArrayList().orEmpty()
        if (!key.isNullOrBlank()) {
            values[key] = entryValues
        }
    }
    return values
}
