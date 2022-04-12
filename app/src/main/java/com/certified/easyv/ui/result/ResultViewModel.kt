package com.certified.easyv.ui.result

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.certified.easyv.data.model.Candidate
import com.certified.easyv.util.UIState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class ResultViewModel : ViewModel() {

    val uiState = ObservableField(UIState.EMPTY)

    private val _candidates = MutableLiveData<List<Candidate>>()
    val candidates: LiveData<List<Candidate>> get() = _candidates

    init {
        getCandidates()
    }

    private fun getCandidates() {
        viewModelScope.launch {
            val query = Firebase.firestore.collection("candidate")
                .orderBy("id", Query.Direction.DESCENDING)
            query.addSnapshotListener { value, error ->
                if (value != null && !value.isEmpty) {
                    uiState.set(UIState.HAS_DATA)
                    _candidates.value = value.toObjects(Candidate::class.java)
                } else
                    uiState.set(UIState.EMPTY)
                if (error != null) {
                    uiState.set(UIState.EMPTY)
                    error.printStackTrace()
                }
            }
        }
    }
}