package com.certified.easyv.ui.vote

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.certified.easyv.R
import com.certified.easyv.adapter.CandidateRecyclerAdapter
import com.certified.easyv.data.model.Candidate
import com.certified.easyv.data.model.Voter
import com.certified.easyv.databinding.DialogConfirmVoteBinding
import com.certified.easyv.databinding.FragmentVoteBinding
import com.certified.easyv.util.Extensions.showToast
import com.certified.easyv.util.PreferenceKeys
import com.certified.easyv.util.UIState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class VoteFragment : Fragment() {

    private var _binding: FragmentVoteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VoteViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private lateinit var preferences: SharedPreferences
    private lateinit var biometricManager: BiometricManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentVoteBinding.inflate(inflater, container, false)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        auth = Firebase.auth
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        biometricManager = BiometricManager.from(requireContext())
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.uiState = viewModel.uiState

        viewModel.apply {
            message.observe(viewLifecycleOwner) {
                if (it != null) {
                    showToast(it)
                    _message.postValue(null)
                }
            }
            voted.observe(viewLifecycleOwner) {
                if (it != null) {
                    binding.isVoted = it
                }
            }
        }

        binding.apply {
            val adapter = CandidateRecyclerAdapter("vote")
            recyclerViewCandidates.adapter = adapter
            recyclerViewCandidates.layoutManager = LinearLayoutManager(requireContext())
            adapter.setOnItemClickedListener(object :
                CandidateRecyclerAdapter.OnItemClickedListener {
                override fun onItemClick(candidate: Candidate, vote: Boolean) {
                    if (vote) {
                        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
                            BiometricManager.BIOMETRIC_SUCCESS -> {
                                val executor = ContextCompat.getMainExecutor(requireContext())
                                val bioMetricPrompt = BiometricPrompt(
                                    requireActivity(),
                                    executor,
                                    object : BiometricPrompt.AuthenticationCallback() {
                                        override fun onAuthenticationError(
                                            errorCode: Int,
                                            errString: CharSequence
                                        ) {
                                            super.onAuthenticationError(errorCode, errString)
                                            showToast("An error occurred: $errString")
                                        }

                                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                            super.onAuthenticationSucceeded(result)
                                            viewModel?.apply {
                                                uiState.set(UIState.LOADING)
                                                castVote(
                                                    candidate.copy(votes = candidate.votes + 1),
                                                    Voter(
                                                        auth.currentUser!!.uid,
                                                        auth.currentUser!!.displayName!!,
                                                        candidate.name
                                                    )
                                                )
                                            }
                                        }

                                        override fun onAuthenticationFailed() {
                                            super.onAuthenticationFailed()
                                            showToast("Authentication failed")
                                        }
                                    })
                                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                    .setTitle("Vote for ${candidate.name}")
                                    .setDescription("Are you sure you want to vote for ${candidate.name}?")
                                    .setNegativeButtonText("Cancel")
                                    .build()
                                bioMetricPrompt.authenticate(promptInfo)
                            }
                            else -> launchConfirmDialog(candidate)
                        }
                    }
                }
            })

            val account_type = preferences.getString(PreferenceKeys.USER_ACCOUNT_TYPE_KEY, "")
            isAdmin = account_type == "admin"
            isUser = account_type == "user"

            btnResultPage.setOnClickListener {
                findNavController().navigate(R.id.resultFragment)
            }

            btnAddCandidate.setOnClickListener {
                findNavController().navigate(VoteFragmentDirections.actionVoteFragmentToAddCandidateFragment(null))
            }
        }
    }

    private fun launchConfirmDialog(candidate: Candidate) {
        val materialDialog = MaterialAlertDialogBuilder(requireContext())
        val alertDialog = materialDialog.create()
        val view = DialogConfirmVoteBinding.inflate(layoutInflater)
        view.apply {
            name = "Are you sure you want to vote for ${candidate.name}?"
            btnCancel.setOnClickListener {
                alertDialog.dismiss()
            }
            btnConfirm.setOnClickListener {
                alertDialog.dismiss()
                viewModel.uiState.set(UIState.LOADING)
                viewModel.castVote(
                    candidate.copy(votes = candidate.votes + 1),
                    Voter(auth.currentUser!!.uid, auth.currentUser!!.displayName!!, candidate.name)
                )
            }
        }
        alertDialog.setView(view.root)
        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}