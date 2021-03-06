package com.certified.easyv.ui.login

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import com.certified.easyv.R
import com.certified.easyv.data.model.User
import com.certified.easyv.databinding.FragmentLoginBinding
import com.certified.easyv.util.Extensions.checkFieldEmpty
import com.certified.easyv.util.Extensions.showToast
import com.certified.easyv.util.PreferenceKeys
import com.certified.easyv.util.UIState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import me.ibrahimsn.lib.SmoothBottomBar

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        binding.lifecycleOwner = this
        binding.uiState = viewModel.uiState

        viewModel.apply {
            message.observe(viewLifecycleOwner) { if (it != null) showToast(it) }
            success.observe(viewLifecycleOwner) {
                if (it) {
                    val user = Firebase.auth.currentUser!!
                    if (user.isEmailVerified) {
                        saveUserPreferences()
                        findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
                    } else {
                        viewModel.uiState.set(UIState.FAILURE)
                        Firebase.auth.signOut()
                        showToast("Check your email for verification link")
                    }
                }
            }
        }
        binding.apply {

            btnForgotButton.setOnClickListener {
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToPasswordRecoveryFragment()
                )
            }

            btnLogin.setOnClickListener {
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString().trim()

                if (etEmail.checkFieldEmpty())
                    return@setOnClickListener

//                if (!isValidEmail(email, etEmail))
//                    return@setOnClickListener

                if (etPassword.checkFieldEmpty())
                    return@setOnClickListener

                viewModel.uiState.set(UIState.LOADING)
                viewModel.signInWithEmailAndPassword(email, password)
            }

            btnSignup.setOnClickListener { findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToSignupFragment()) }
        }
    }

    private fun saveUserPreferences() {
        val query = Firebase.firestore.collection("users").document(Firebase.auth.currentUser!!.uid)
        query.get().addOnSuccessListener {
            val user = it.toObject(User::class.java)!!
            preferences.edit {
                putString(PreferenceKeys.USER_ID_KEY, user.id)
                putString(PreferenceKeys.USER_NAME_KEY, user.name)
                putString(PreferenceKeys.USER_EMAIL_KEY, user.email)
                putString(PreferenceKeys.USER_MATRICULATION_NUMBER_KEY, user.matriculation_number)
                putString(PreferenceKeys.USER_PROFILE_IMAGE_KEY, user.profile_image)
                putString(PreferenceKeys.USER_ACCOUNT_TYPE_KEY, user.account_type)
            }
        }.addOnFailureListener { Log.d("TAG", "saveUserPreferences: ${it.localizedMessage}") }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<SmoothBottomBar>(R.id.bottomBar).visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}