package com.example.project

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SpanData(
    val type: String = "",
    val start: Int = 0,
    val end: Int = 0,
    val color: Int? = null
) : Parcelable {
    constructor() : this("", 0, 0, null)
}


