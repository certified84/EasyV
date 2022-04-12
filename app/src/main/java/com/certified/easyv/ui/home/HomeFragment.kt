package com.certified.easyv.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.certified.easyv.R
import com.certified.easyv.adapter.CandidateRecyclerAdapter
import com.certified.easyv.data.model.Candidate
import com.certified.easyv.databinding.FragmentHomeBinding
import me.ibrahimsn.lib.SmoothBottomBar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.uiState = viewModel.uiState

        binding.apply {
            val adapter = CandidateRecyclerAdapter("home")
            recyclerViewCandidates.adapter = adapter
            recyclerViewCandidates.apply {
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                clipToPadding = false
                clipChildren = false
            }
            adapter.setOnItemClickedListener(object :
                CandidateRecyclerAdapter.OnItemClickedListener {
                override fun onItemClick(candidate: Candidate, vote: Boolean) {
//                    TODO("Not yet implemented")
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        requireActivity().findViewById<SmoothBottomBar>(R.id.bottomBar).visibility = View.VISIBLE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}