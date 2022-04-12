package com.certified.easyv.ui.vote

import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.certified.easyv.data.model.Candidate
import com.certified.easyv.data.model.Voter
import com.certified.easyv.data.repository.FirebaseRepository
import com.certified.easyv.util.UIState
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class VoteViewModel @Inject constructor(private val repository: FirebaseRepository) : ViewModel() {

    val uiState = ObservableField(UIState.EMPTY)

    private val _candidates = MutableLiveData<List<Candidate>>()
    val candidates: LiveData<List<Candidate>> get() = _candidates

    private val _voted = MutableLiveData(false)
    val voted: LiveData<Boolean> get() = _voted

    val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> get() = _success

    init {
        getCandidates()
        getVoters()
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

    private fun getVoters() {
        viewModelScope.launch {
            val query = Firebase.firestore.collection("voter")
                .orderBy("id", Query.Direction.DESCENDING)
            query.addSnapshotListener { value, _ ->
                if (value != null && !value.isEmpty) {
                    val votes = value.toObjects(Voter::class.java)
                    for (vote in votes) {
                        if (vote.id == Firebase.auth.currentUser!!.uid) {
                            _voted.value = true
                            break
                        }
                    }
                }
            }
        }
    }

    fun castVote(candidate: Candidate, voter: Voter) {
        viewModelScope.launch {
            try {
                repository.updateCandidateVoteCount(candidate).await()
                repository.uploadVoter(voter).await()
                uiState.set(UIState.SUCCESS)
                _success.value = true
                _message.value = "Your vote has been casted successfully"
            } catch (e: Exception) {
                uiState.set(UIState.FAILURE)
                _success.value = false
                _message.value = "An error occurred: ${e.localizedMessage}"
                Log.d("TAG", "castVote: Error: ${e.localizedMessage}")
            }
        }
    }
}