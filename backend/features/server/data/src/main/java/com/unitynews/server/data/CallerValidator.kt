package com.unitynews.server.data

import android.os.Process

class CallerValidator(
    private val allowedUid: Int = Process.myUid(),
) {
    fun validate(callingUid: Int): Boolean = callingUid == allowedUid
}
