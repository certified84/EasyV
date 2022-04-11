package com.certified.easyv.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.certified.easyv.R
import com.certified.easyv.adapter.ViewPagerAdapter
import com.certified.easyv.data.model.SliderItem
import com.certified.easyv.databinding.FragmentOnboardingBinding
import com.certified.easyv.util.Extensions.openBrowser
import me.ibrahimsn.lib.SmoothBottomBar

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!
    private val sliderItems = mutableListOf<SliderItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUpSliderItem()
        setUpViewPager()

        binding.apply {
            btnGetStarted.setOnClickListener { findNavController().navigate(R.id.action_onboardingFragment_to_signupFragment) }
            btnPrivacyPolicy.setOnClickListener {
                requireContext().openBrowser("https://github.com/certified84")
            }
            btnTerms.setOnClickListener {
                requireContext().openBrowser("https://github.com/certified84/AudioNote")
            }
        }
    }

    private fun setUpSliderItem() {
        sliderItems.add(
            SliderItem(
                R.raw.lottie_vote, getString(R.string.vote_from_comfort),
                getString(R.string.vote_from_comfort_description)
            )
        )
        sliderItems.add(
            SliderItem(
                R.raw.lottie_process_result, getString(R.string.realtime_result),
                getString(R.string.realtime_result_description)
            )
        )
        sliderItems.add(
            SliderItem(
                R.raw.lottie_profile_user_card, getString(R.string.see_candidate_profile),
                getString(R.string.see_candidate_profile_description)
            )
        )
//        sliderItems.add(
//            SliderItem(
//                R.raw.animation_report, getString(R.string.view_pager_title_report),
//                getString(R.string.view_pager_description_report)
//            )
//        )
    }

    private fun setUpViewPager() {
        binding.apply {
            val viewPagerAdapter = ViewPagerAdapter(sliderItems)
            viewPager.adapter = viewPagerAdapter
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    indicator.selection = position
                    if (position == sliderItems.size - 1) {
                        indicator.count = sliderItems.size
                        indicator.selection = position
                    }
                }
            })
        }
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