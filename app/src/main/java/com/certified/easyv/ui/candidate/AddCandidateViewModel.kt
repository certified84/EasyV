package com.certified.easyv.ui.candidate

import android.net.Uri
import android.util.Log
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.certified.easyv.data.model.Candidate
import com.certified.easyv.data.repository.FirebaseRepository
import com.certified.easyv.util.UIState
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class AddCandidateViewModel @Inject constructor(private val repository: FirebaseRepository) :
    ViewModel() {

    val uiState = ObservableField(UIState.EMPTY)

    val _message = MutableLiveData<String?>()
    val message: LiveData<String?> get() = _message

    val _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> get() = _success

    fun uploadCandidate(uri: Uri?, path: String, storage: FirebaseStorage, candidate: Candidate) {
        viewModelScope.launch {
            try {
                val profileImageRef = storage.reference.child(path)
                profileImageRef.putFile(uri!!).await()
                val downloadUrl = profileImageRef.downloadUrl.await()
                repository.uploadCandidate(candidate).await()
                repository.updateCandidateImage(candidate.name, downloadUrl.toString()).await()
                uiState.set(UIState.SUCCESS)
                _success.value = true
                _message.value = "Candidate uploaded successfully"
            } catch (e: Exception) {
                uiState.set(UIState.FAILURE)
                _success.value = false
                _message.value = "An error occurred: ${e.localizedMessage}"
                Log.d("TAG", "uploadImage: Error: ${e.localizedMessage}")
            }
        }
    }
}