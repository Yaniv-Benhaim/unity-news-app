package com.unitynews.contract

import android.os.Parcel
import android.os.Parcelable

/**
 * Parcelable request sent from reader app to backend app over AIDL.
 *
 * requestId gives each IPC request a stable identifier for logging/debugging.
 * filterValues carries backend-defined filter keys with their selected values.
 */
data class ArticleFilterRequest(
    val requestId: String,
    val filterValues: Map<String, List<String>> = emptyMap(),
) : Parcelable {
    /** Read fields in the exact same order used by writeToParcel. */
    constructor(parcel: Parcel) : this(
        requestId = parcel.readString().orEmpty(),
        filterValues = parcel.readFilterValues(),
    )

    /** Write fields in a stable order so both apps decode the same contract. */
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(requestId)
        parcel.writeFilterValues(filterValues)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ArticleFilterRequest> {
        override fun createFromParcel(parcel: Parcel): ArticleFilterRequest =
            ArticleFilterRequest(parcel)

        override fun newArray(size: Int): Array<ArticleFilterRequest?> = arrayOfNulls(size)
    }
}

/** Write filter values in sorted key order for deterministic parcel contents. */
private fun Parcel.writeFilterValues(values: Map<String, List<String>>) {
    val sortedValues = values.toSortedMap()
    writeInt(sortedValues.size)
    sortedValues.forEach { (key, entryValues) ->
        writeString(key)
        writeStringList(entryValues)
    }
}

/** Read the filter map written by writeFilterValues. */
private fun Parcel.readFilterValues(): Map<String, List<String>> {
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
