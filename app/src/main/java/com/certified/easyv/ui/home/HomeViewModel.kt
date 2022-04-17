package com.certified.easyv.ui.home

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.certified.easyv.data.model.Candidate
import com.certified.easyv.data.repository.FirebaseRepository
import com.certified.easyv.util.UIState
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: FirebaseRepository) : ViewModel() {

    val uiState = ObservableField(UIState.EMPTY)

    private val _candidates = MutableLiveData<List<Candidate>>()
    val candidates: LiveData<List<Candidate>> get() = _candidates

    val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

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

    fun deleteCandidate(candidate: Candidate) {
        viewModelScope.launch {
            try {
                repository.deleteCandidate(candidate).await()
                uiState.set(UIState.SUCCESS)
                getCandidates()
                _message.value =
                    "You have successfully deleted ${candidate.name} from the candidate list"
            } catch (e: Exception) {
                uiState.set(UIState.FAILURE)
                _message.value = "An error occurred: ${e.localizedMessage}"
            }
        }
    }
}