package com.certified.easyv.ui.profile

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import coil.load
import coil.transform.CircleCropTransformation
import com.certified.easyv.R
import com.certified.easyv.databinding.FragmentProfileBinding
import com.certified.easyv.util.Extensions.chooseFromGallery
import com.certified.easyv.util.Extensions.launchCamera
import com.certified.easyv.util.Extensions.openBrowser
import com.certified.easyv.util.Extensions.showToast
import com.certified.easyv.util.PreferenceKeys
import com.certified.easyv.util.UIState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

@AndroidEntryPoint
class ProfileFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var preferences: SharedPreferences
    private lateinit var auth: FirebaseAuth
    private val viewModel: ProfileViewModel by activityViewModels()
    private lateinit var storage: FirebaseStorage

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = Firebase.auth
        storage = Firebase.storage
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.apply {

            uiState = viewModel.uiState
            lifecycleOwner = this@ProfileFragment

            viewModel.message.observe(viewLifecycleOwner) {
                if (it != null) {
                    showToast(it)
                    viewModel._message.postValue(null)
                }
            }

            viewChangePassword.setOnClickListener(this@ProfileFragment)
            viewPrivacy.setOnClickListener(this@ProfileFragment)
            viewHelpSupport.setOnClickListener(this@ProfileFragment)
            btnLogout.setOnClickListener(this@ProfileFragment)
            btnChangeImage.setOnClickListener(this@ProfileFragment)

            tvUserName.text = auth.currentUser?.displayName
            Log.d("TAG", "onViewCreated: profileImage: ${auth.currentUser?.photoUrl}")
            if (auth.currentUser?.photoUrl == null)
                ivProfileImage.load(R.drawable.no_profile_image) {
                    transformations(CircleCropTransformation())
                }
            else
                ivProfileImage.load(auth.currentUser?.photoUrl) {
                    placeholder(R.drawable.no_profile_image)
                    transformations(CircleCropTransformation())
                }

            cbDarkTheme.apply {
                isChecked = preferences.getBoolean(PreferenceKeys.DARK_MODE, false)
                setOnCheckedChangeListener { _, isChecked ->
                    preferences.edit {
                        putBoolean(
                            PreferenceKeys.DARK_MODE,
                            isChecked
                        )
                    }
                    if (isChecked)
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    else
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
        }
    }

    private fun launchChangeProfileImageDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        val selection = arrayOf(
            "Take picture",
            "Choose from gallery",
        )
        builder.setTitle("Options")
        builder.setSingleChoiceItems(selection, -1) { dialog, which ->
            when (which) {
                0 -> launchCamera(REQUEST_IMAGE_CAPTURE)
                1 -> chooseFromGallery(PICK_IMAGE_CODE)
            }
            dialog.dismiss()
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            assert(data != null)
            try {
                val bitmap = data?.extras!!["data"] as Bitmap?
                requireContext().openFileOutput("profile_image", Context.MODE_PRIVATE).use {
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                val file = File(requireContext().filesDir, "profile_image")
                uploadImage(Uri.fromFile(file))
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (requestCode == PICK_IMAGE_CODE && resultCode == Activity.RESULT_OK) {
            assert(data != null)
            try {
                uploadImage(data?.data)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(uri: Uri?) {
        val path = "profileImages/${auth.currentUser!!.uid}/profileImage.jpg"
        viewModel.apply {
            uiState.set(UIState.LOADING)
            uploadImage(uri, path, storage, auth.currentUser!!.uid)
            binding.ivProfileImage.load(uri) {
                transformations(CircleCropTransformation())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (auth.currentUser == null)
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToOnboardingFragment())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(v: View?) {
        binding.apply {
            when (v) {
                viewPrivacy -> {
                    requireContext().openBrowser("https://github.com/certified84")
                }
                viewHelpSupport -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("Sammie_kt@pm.me"))
                        putExtra(Intent.EXTRA_SUBJECT, "Feedback")
                    }
                    if (intent.resolveActivity(requireActivity().packageManager) != null) {
                        startActivity(intent)
                    }
                }
                viewChangePassword -> {
                    launchPasswordChangeDialog()
                }
                btnLogout -> {
                    signOut()
                }
                btnChangeImage -> {
                    launchChangeProfileImageDialog()
                }
            }
        }
    }

    private fun signOut() {
        preferences.edit {
            putBoolean(PreferenceKeys.DARK_MODE, false)
            putString(PreferenceKeys.USER_ID_KEY, null)
            putString(PreferenceKeys.USER_NAME_KEY, null)
            putString(PreferenceKeys.USER_EMAIL_KEY, null)
            putString(PreferenceKeys.USER_MATRICULATION_NUMBER_KEY, null)
            putString(PreferenceKeys.USER_PROFILE_IMAGE_KEY, null)
            putString(PreferenceKeys.USER_ACCOUNT_TYPE_KEY, null)
        }
        auth.signOut()
        findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToLoginFragment())
    }

    private fun launchPasswordChangeDialog() {
        val materialDialog = MaterialAlertDialogBuilder(requireContext()).apply {
            setTitle("Password change")
            setMessage("You are about to change your password. A password reset link will be sent to your email and you'll be signed out")
            setPositiveButton("Continue") { dialog, _ ->
                dialog.dismiss()
                viewModel.uiState.set(UIState.LOADING)
                auth.sendPasswordResetEmail(auth.currentUser!!.email!!).addOnSuccessListener {
                    viewModel.uiState.set(UIState.SUCCESS)
                    signOut()
                }.addOnFailureListener {
                    viewModel.uiState.set(UIState.FAILURE)
                    showToast("An error occurred: ${it.localizedMessage}")
                }
            }
            setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        }
        materialDialog.show()
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val PICK_IMAGE_CODE = 102
    }
}