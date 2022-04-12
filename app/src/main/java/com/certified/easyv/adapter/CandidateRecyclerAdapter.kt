package com.certified.easyv.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.certified.easyv.data.model.Candidate
import com.certified.easyv.databinding.ItemCandidateHomeBinding
import com.certified.easyv.databinding.ItemCandidateResultBinding
import com.certified.easyv.databinding.ItemCandidateVoteBinding

class CandidateRecyclerAdapter(private val where: String) :
    ListAdapter<Candidate, RecyclerView.ViewHolder>(diffCallback) {

    private lateinit var listener: OnItemClickedListener

    inner class CandidateResultViewHolder(val binding: ItemCandidateResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(candidate: Candidate) {
            binding.candidate = candidate
        }
    }

    inner class CandidateVoteViewHolder(val binding: ItemCandidateVoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(candidate: Candidate) {
            binding.candidate = candidate
        }

        init {
            binding.checkboxCandidate.setOnCheckedChangeListener { _, isChecked ->
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position), isChecked)
                }
            }
        }
    }

    inner class CandidateHomeViewHolder(val binding: ItemCandidateHomeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(candidate: Candidate) {
            binding.candidate = candidate
        }

        init {
            itemView.setOnClickListener {
                val position = absoluteAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position), false)
                }
            }
        }
    }

    interface OnItemClickedListener {
        fun onItemClick(candidate: Candidate, vote: Boolean)
    }

    fun setOnItemClickedListener(listener: OnItemClickedListener) {
        this.listener = listener
    }

    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Candidate>() {
            override fun areItemsTheSame(oldItem: Candidate, newItem: Candidate) =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: Candidate, newItem: Candidate) =
                oldItem == newItem
        }
    }

    override fun getItemViewType(position: Int): Int {
        val currentItem = getItem(position)
        return if (where == "result") 0 else if (where == "vote") 1 else 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val binding =
                    ItemCandidateResultBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                CandidateResultViewHolder(binding)
            }
            1 -> {
                val binding =
                    ItemCandidateVoteBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                CandidateVoteViewHolder(binding)
            }
            else -> {
                val binding =
                    ItemCandidateHomeBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                CandidateHomeViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentItem = getItem(position)
        when (where) {
            "result" -> (holder as CandidateResultViewHolder).bind(currentItem)
            "vote" -> (holder as CandidateVoteViewHolder).bind(currentItem)
            else -> (holder as CandidateHomeViewHolder).bind(currentItem)
        }
    }
}