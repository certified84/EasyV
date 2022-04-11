package com.certified.easyv.ui.passwordRecovery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.certified.easyv.databinding.FragmentPasswordRecoveryBinding
import com.certified.easyv.util.Extensions.showToast
import com.certified.easyv.util.UIState
import com.certified.easyv.util.isValidEmail
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PasswordRecoveryFragment : Fragment() {

    private var _binding: FragmentPasswordRecoveryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PasswordRecoveryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPasswordRecoveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this@PasswordRecoveryFragment
        binding.uiState = viewModel.uiState

        viewModel.apply {
            message.observe(viewLifecycleOwner) { if (it != null) showToast(it) }
            success.observe(viewLifecycleOwner) {
                if (it) {
                    findNavController().navigate(PasswordRecoveryFragmentDirections.actionPasswordRecoveryFragmentToLoginFragment())
                }
            }
        }

        binding.apply {
            btnBack.setOnClickListener {
                findNavController().navigate(
                    PasswordRecoveryFragmentDirections.actionPasswordRecoveryFragmentToLoginFragment()
                )
            }
            btnResetPassword.setOnClickListener {
                val email = etEmail.text.toString().trim()

                if (email.isBlank()) {
                    etEmail.error = "Required *"
                    etEmail.requestFocus()
                    return@setOnClickListener
                }

                if (!isValidEmail(email, etEmail))
                    return@setOnClickListener

                viewModel.uiState.set(UIState.LOADING)
                viewModel.sendPasswordResetEmail(email)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}