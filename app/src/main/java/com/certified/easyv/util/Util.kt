package com.certified.easyv.util

import android.util.Patterns
import com.google.android.material.textfield.TextInputEditText
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

fun verifyPassword(password: String, editText: TextInputEditText): Boolean {

    if (password.length < 8) {
        with(editText) {
            error = "Minimum of 8 characters"
            requestFocus()
        }
        return false
    }

    var numberFlag = false
    var upperCaseFlag = false

    for (i in password.indices) {
        val ch = password[i]
        when {
            ch.isUpperCase() -> {
                upperCaseFlag = true
            }
            ch.isDigit() -> {
                numberFlag = true
            }
        }
    }

    if (!upperCaseFlag) {
        with(editText) {
            error = "Uppercase letter required* "
            requestFocus()
        }
        return false
    }

    val pattern = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE)
    if (!pattern.matcher(password).find()) {
        with(editText) {
            error = "Special character required* "
            requestFocus()
        }
        return false
    }

    if (!numberFlag) {
        with(editText) {
            error = "Number required* "
            requestFocus()
        }
        return false
    }

    return true
}

fun isValidEmail(email: String, editText: TextInputEditText): Boolean {
    if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
        editText.apply {
            error = "Enter a valid email"
            requestFocus()
            return false
        }
    }

//    if (!email.contains("@futa.edu.ng")) {
//        editText.apply {
//            error = "Only FUTA email is allowed"
//            requestFocus()
//            return false
//        }
//    }

    return true
}

fun isValidMatriculationNumber(matriculationNumber: String, editText: TextInputEditText): Boolean {
    if (matriculationNumber.length != 10) {
        editText.apply {
            error = "Enter a valid matriculation number"
            requestFocus()
            return false
        }
    }

    return true
}