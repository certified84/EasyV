package com.certified.easyv.util

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.browser.customtabs.*
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.certified.easyv.R
import com.certified.easyv.util.Config.CUSTOM_PACKAGE_NAME
import com.google.android.material.textfield.TextInputEditText

object Extensions {
    fun TextInputEditText.checkFieldEmpty(): Boolean {
        return if (this.text.toString().isBlank()) {
            with(this) {
                error = "Required *"
                requestFocus()
            }
            true
        } else
            false
    }

    fun AutoCompleteTextView.checkFieldEmpty(): Boolean {
        return if (this.text.toString().isBlank()) {
            with(this) {
                error = "Required *"
                requestFocus()
            }
            true
        } else
            false
    }

    fun Fragment.showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
    }

    fun Fragment.launchCamera(code: Int) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        try {
            startActivityForResult(takePictureIntent, code)
        } catch (e: ActivityNotFoundException) {
            showToast("An error occurred: ${e.localizedMessage}")
        }
    }

    fun Fragment.chooseFromGallery(code: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        try {
            this.startActivityForResult(Intent.createChooser(intent, "Select image"), code)
        } catch (e: ActivityNotFoundException) {
            showToast("An error occurred: ${e.message}")
        }
    }

    fun Context.openBrowser(url: String) {
        try {
            val packageManager = this.packageManager
            packageManager.getPackageInfo(CUSTOM_PACKAGE_NAME, 0)
            showChromeCustomTabView(url, this)
        } catch (e: PackageManager.NameNotFoundException) {
            this.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    private fun showChromeCustomTabView(url: String, context: Context) {
        var customTabsClient: CustomTabsClient?
        var customTabsSession: CustomTabsSession? = null
        val customTabsServiceConnection: CustomTabsServiceConnection =
            object : CustomTabsServiceConnection() {
                override fun onServiceDisconnected(name: ComponentName?) {
                    customTabsClient = null
                }

                override fun onCustomTabsServiceConnected(
                    name: ComponentName,
                    client: CustomTabsClient
                ) {
                    customTabsClient = client
                    customTabsClient!!.warmup(0L)
                    customTabsSession = customTabsClient!!.newSession(null)
                }
            }
        CustomTabsClient.bindCustomTabsService(
            context,
            CUSTOM_PACKAGE_NAME,
            customTabsServiceConnection
        )
        val customTabsIntent = CustomTabsIntent.Builder(customTabsSession)
            .setShowTitle(true).setDefaultColorSchemeParams(
                CustomTabColorSchemeParams.Builder().setToolbarColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.colorPrimary,
                        null
                    )
                ).build()
            )
            .build()

        customTabsIntent.launchUrl(context, Uri.parse(url))
    }
}