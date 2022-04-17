package com.certified.easyv.ui.candidate

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import com.certified.easyv.R
import com.certified.easyv.data.model.Candidate
import com.certified.easyv.databinding.FragmentAddCandidateBinding
import com.certified.easyv.util.Extensions.checkFieldEmpty
import com.certified.easyv.util.Extensions.chooseFromGallery
import com.certified.easyv.util.Extensions.launchCamera
import com.certified.easyv.util.Extensions.showToast
import com.certified.easyv.util.UIState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

@AndroidEntryPoint
class AddCandidateFragment : Fragment() {

    private var _binding: FragmentAddCandidateBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddCandidateViewModel by viewModels()
    private lateinit var preferences: SharedPreferences
    private lateinit var storage: FirebaseStorage
    private var imageUri: Uri? = null
    private val args: AddCandidateFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAddCandidateBinding.inflate(inflater, container, false)
//        auth = Firebase.auth
        storage = Firebase.storage
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.uiState = viewModel.uiState
        binding.lifecycleOwner = this

        viewModel.message.observe(viewLifecycleOwner) {
            if (it != null) {
                showToast(it)
                viewModel._message.postValue(null)
            }
        }

        viewModel.success.observe(viewLifecycleOwner) {
            if (it != null)
                if (it) {
                    viewModel._success.postValue(null)
                    findNavController().navigate(AddCandidateFragmentDirections.actionAddCandidateFragmentToHomeFragment())
                }
        }

        binding.apply {
            candidate = args.candidate
            if (args.candidate != null) {
                btnAddCandidate.text = "Update Candidate"
                tvHeading.text = "Update Candidate"
                etDisplayName.apply {
                    keyListener = null
                    alpha = 0.5F
                }
            }

            btnBack.setOnClickListener {
                findNavController().navigate(AddCandidateFragmentDirections.actionAddCandidateFragmentToVoteFragment())
            }
            btnChangeImage.setOnClickListener {
                launchChangeProfileImageDialog()
            }
            btnAddCandidate.setOnClickListener {
                val name = etDisplayName.text.toString()
                val school = etSchool.text.toString()
                val position = etPost.text.toString()
                val description = etDescription.text.toString()

                if (etDisplayName.checkFieldEmpty())
                    return@setOnClickListener

                if (etSchool.checkFieldEmpty())
                    return@setOnClickListener

                if (etPost.checkFieldEmpty())
                    return@setOnClickListener

                if (etDescription.checkFieldEmpty())
                    return@setOnClickListener

                if (args.candidate == null && imageUri == null) {
                    showToast("Please select image first")
                    return@setOnClickListener
                }

                val path = "profileImages/${binding.etDisplayName.text.toString()}/profileImage.jpg"
                viewModel.uiState.set(UIState.LOADING)
                if (args.candidate != null)
                    viewModel.updateCandidate(
                        imageUri, path, storage,
                        args.candidate!!.copy(
                            school = school,
                            description = description,
                            position = position
                        )
                    )
                else
                    viewModel.uploadCandidate(
                        imageUri,
                        path,
                        storage,
                        Candidate(
                            name = name,
                            school = school,
                            description = description,
                            position = position
                        )
                    )
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
                imageUri = Uri.fromFile(file)

                binding.ivProfileImage.load(imageUri) {
                    transformations(RoundedCornersTransformation(20f))
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else if (requestCode == PICK_IMAGE_CODE && resultCode == Activity.RESULT_OK) {
            assert(data != null)
            try {
                imageUri = data?.data
                binding.ivProfileImage.load(imageUri) {
                    transformations(RoundedCornersTransformation(20f))
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadImage(uri: Uri?) {
        val path = "profileImages/${binding.etDisplayName.text.toString()}/profileImage.jpg"
        viewModel.apply {
            uiState.set(UIState.LOADING)
//            uploadCandidate(uri, path, storage, binding.etDisplayName.text.toString())
            binding.ivProfileImage.load(uri) {
                transformations(CircleCropTransformation())
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.apply {

            val schoolsArrayAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                requireContext().resources.getStringArray(R.array.schools)
            )
            etSchool.setAdapter(schoolsArrayAdapter)

            val positionArrayAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                requireContext().resources.getStringArray(R.array.positions)
            )
            etPost.setAdapter(positionArrayAdapter)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 101
        private const val PICK_IMAGE_CODE = 102
    }
}

//A 300L Computer Engineering student. A very big big tech bro. He has money, bill him when you see him. My guy dey use macbook. Olowo ni oremi.