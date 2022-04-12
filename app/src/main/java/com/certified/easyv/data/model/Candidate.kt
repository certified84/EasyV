package com.certified.easyv.data.model

data class Candidate(
    val id: String,
    val name: String = "",
    val profile_image: String? = null,
    val description: String = "",
    val post: String = "Presidential Candidate",
    val votes: Int = 0
)