package com.certified.easyv.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.RenderMode
import com.certified.easyv.data.model.SliderItem
import com.certified.easyv.databinding.ItemViewPagerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewPagerAdapter(private val sliderItems: List<SliderItem>) :
    RecyclerView.Adapter<ViewPagerAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemViewPagerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sliderItem: SliderItem) {
            binding.sliderItem = sliderItem
            CoroutineScope(Dispatchers.IO).launch {
                binding.animationView.apply {
                    setAnimation(sliderItem.animation)
                    playAnimation()
                    setRenderMode(RenderMode.SOFTWARE)
                    enableMergePathsForKitKatAndAbove(true)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemViewPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        Log.d("TAG", "onCreateViewHolder: created")
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sliderItems[position])
    }

    override fun getItemCount(): Int {
        return sliderItems.size
    }
}