package com.certified.easyv.data.model

import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize

@Parcelize
data class Candidate(
    val name: String = "",
    val school: String = "",
    val profile_image: String? = null,
    val description: String = "",
    val position: String = "Presidential Candidate",
    val votes: Int = 0
) : Parcelable {
    @IgnoredOnParcel
    val id = name
}