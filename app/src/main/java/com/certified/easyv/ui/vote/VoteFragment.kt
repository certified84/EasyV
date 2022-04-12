package com.certified.easyv.ui.vote

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.certified.easyv.R
import com.certified.easyv.adapter.CandidateRecyclerAdapter
import com.certified.easyv.data.model.Candidate
import com.certified.easyv.databinding.FragmentVoteBinding
import com.certified.easyv.util.PreferenceKeys

class VoteFragment : Fragment() {

    private var _binding: FragmentVoteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VoteViewModel by viewModels()
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentVoteBinding.inflate(inflater, container, false)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.uiState = viewModel.uiState

        binding.apply {
            val adapter = CandidateRecyclerAdapter("vote")
            recyclerViewCandidates.adapter = adapter
            recyclerViewCandidates.layoutManager = LinearLayoutManager(requireContext())
            adapter.setOnItemClickedListener(object : CandidateRecyclerAdapter.OnItemClickedListener {
                override fun onItemClick(candidate: Candidate, vote: Boolean) {
//                    TODO("Not yet implemented")
                }
            })

            val account_type = preferences.getString(PreferenceKeys.USER_ACCOUNT_TYPE_KEY, "")
            isAdmin = account_type == "admin"
            isUser = account_type == "user"
            isVoted = false

            btnResultPage.setOnClickListener {
                findNavController().navigate(R.id.resultFragment)
            }

            btnAddCandidate.setOnClickListener {
                findNavController().navigate(VoteFragmentDirections.actionVoteFragmentToAddCandidateFragment())
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}