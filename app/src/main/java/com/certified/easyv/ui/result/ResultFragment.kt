package com.certified.easyv.ui.result

import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.certified.easyv.adapter.CandidateRecyclerAdapter
import com.certified.easyv.databinding.FragmentResultBinding
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ResultFragment : Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResultViewModel by viewModels()
    private lateinit var preferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        binding.uiState = viewModel.uiState

        binding.apply {
            val adapter = CandidateRecyclerAdapter("result")
            recyclerViewCandidates.adapter = adapter
            recyclerViewCandidates.layoutManager = LinearLayoutManager(requireContext())

            val pieEntry = mutableListOf<PieEntry>()
            viewModel?.candidates?.observe(viewLifecycleOwner) {
                for (candidate in it) {
                    pieEntry.add(PieEntry(candidate.votes.toFloat(), candidate.name.substringBefore(" ")))
                }
            }
            val pieDataSet = PieDataSet(pieEntry, "")
            pieDataSet.apply {
                sliceSpace = 2f
                valueTextSize = 0f
                colors = listOf(
                    Color.RED,
                    Color.BLUE,
                    Color.MAGENTA,
                    Color.GRAY,
                    Color.CYAN,
                    Color.YELLOW,
                    Color.GREEN,
                    Color.DKGRAY,
                    Color.LTGRAY,
                )
            }
            val descrip = Description()
            descrip.text = ""
            pieChart.apply {
                isRotationEnabled = false
                holeRadius = 2f
                description = descrip
                setTransparentCircleAlpha(0)
                data = PieData(pieDataSet)
                invalidate()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}