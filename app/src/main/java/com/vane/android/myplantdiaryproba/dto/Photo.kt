package com.vane.android.myplantdiaryproba.dto

import java.util.*

data class Photo(
    var localUri: String = "",
    var remoteUrl: String = "",
    var description: String = "",
    var dateTaken: Date = Date(),
    var id: String = ""
) {
}