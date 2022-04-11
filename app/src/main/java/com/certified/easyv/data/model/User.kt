package com.certified.easyv.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val matriculation_number: String? = "",
    val profile_image: String? = null
) {
    val account_type = if (name.contains("A_")) "admin" else "user"
}