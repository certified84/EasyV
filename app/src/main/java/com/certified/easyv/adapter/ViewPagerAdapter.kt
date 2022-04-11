package com.certified.easyv.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.RenderMode
import com.certified.easyv.data.model.SliderItem
import com.certified.easyv.databinding.ItemViewPagerBinding

class ViewPagerAdapter(private val sliderItem: List<SliderItem>) :
    ListAdapter<SliderItem, ViewPagerAdapter.ViewHolder>(
        diffCallback
    ) {

    inner class ViewHolder(val binding: ItemViewPagerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(sliderItem: SliderItem) {
            binding.sliderItem = sliderItem
            binding.animationView.setRenderMode(RenderMode.SOFTWARE)
            binding.animationView.setAnimation(sliderItem.animation)
        }
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<SliderItem>() {
            override fun areItemsTheSame(oldItem: SliderItem, newItem: SliderItem) =
                oldItem.title == newItem.title

            override fun areContentsTheSame(oldItem: SliderItem, newItem: SliderItem) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemViewPagerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(sliderItem[position])
    }
}