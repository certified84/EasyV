package com.certified.easyv.data.model

data class Candidate(
    val name: String = "",
    val school: String = "",
    val position: String = "",
    val profile_image: String? = null,
    val description: String = "",
    val post: String = "Presidential Candidate",
    val votes: Int = 0
) {
    val id = name
}