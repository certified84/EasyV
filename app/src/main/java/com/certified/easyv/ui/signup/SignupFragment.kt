package com.certified.easyv.ui.signup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.certified.easyv.data.model.User
import com.certified.easyv.databinding.FragmentSignupBinding
import com.certified.easyv.util.Extensions.checkFieldEmpty
import com.certified.easyv.util.Extensions.showToast
import com.certified.easyv.util.UIState
import com.certified.easyv.util.isValidEmail
import com.certified.easyv.util.verifyPassword
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignupFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignupViewModel by viewModels()

    private lateinit var name: String
    private lateinit var matriculation_number: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.uiState = viewModel.uiState

        viewModel.apply {
            message.observe(viewLifecycleOwner) {
                if (it != null) {
                    showToast(it)
                    _message.postValue(null)
                }
            }
            success.observe(viewLifecycleOwner) {
                if (it) {
                    _success.postValue(false)
                    val currentUser = Firebase.auth.currentUser!!
                    val newUser = User(
                        id = currentUser.uid,
                        name = name,
                        email = currentUser.email.toString(),
                        matriculation_number = matriculation_number
                    )
                    uploadDetails(currentUser.uid, newUser)
                }
            }
            uploadSuccess.observe(viewLifecycleOwner) {
                if (it) {
                    _uploadSuccess.postValue(false)
                    Firebase.auth.apply {
                        val profileChangeRequest = userProfileChangeRequest {
                            displayName = name
                        }
                        currentUser!!.updateProfile(profileChangeRequest)
                        currentUser!!.sendEmailVerification()
                        signOut()
                    }
                    findNavController().navigate(SignupFragmentDirections.actionSignupFragmentToLoginFragment())
                }
            }
        }

        binding.apply {

            etMatriculationNumber.doOnTextChanged { text, _, _, _ ->
                if (text != null) {
                    when (text.length) {
                        3 -> {
                            etMatriculationNumber.apply {
                                setText(text.toString() + "/")
                                setSelection(etMatriculationNumber.text!!.length)
                            }
                        }
                        6 -> {
                            etMatriculationNumber.apply {
                                setText(text.toString() + "/")
                                setSelection(etMatriculationNumber.text!!.length)
                            }
                        }
                    }
                }
            }

            val mKeyListener = etMatriculationNumber.keyListener
            etDisplayName.doOnTextChanged { text, _, _, _ ->
                if (text != null){
                    if (text.startsWith("A_", ignoreCase = true))
                        etMatriculationNumber.apply {
                            keyListener = null
                            alpha = 0.5F
                        }
                    else
                        etMatriculationNumber.apply {
                            keyListener = mKeyListener
                            alpha = 1F
                        }
                }
            }

            btnLogin.setOnClickListener { findNavController().navigate(SignupFragmentDirections.actionSignupFragmentToLoginFragment()) }

            btnSignup.setOnClickListener {
                name = etDisplayName.text.toString().trim()
                matriculation_number = etMatriculationNumber.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (etDisplayName.checkFieldEmpty())
                    return@setOnClickListener

                if (etEmail.checkFieldEmpty())
                    return@setOnClickListener

                if (!isValidEmail(email, etEmail))
                    return@setOnClickListener

                if (!name.startsWith("A_") && etMatriculationNumber.checkFieldEmpty())
                    return@setOnClickListener

                if (etPassword.checkFieldEmpty())
                    return@setOnClickListener

                if (!verifyPassword(password, etPassword))
                    return@setOnClickListener

                viewModel.uiState.set(UIState.LOADING)
                viewModel.createUserWithEmailAndPassword(email, password)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}