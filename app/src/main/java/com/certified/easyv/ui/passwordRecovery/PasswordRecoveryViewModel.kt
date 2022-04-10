package com.certified.easyv.ui.passwordRecovery

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.certified.easyv.data.repository.FirebaseRepository
import com.certified.easyv.util.UIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class PasswordRecoveryViewModel @Inject constructor(private val repository: FirebaseRepository) :
    ViewModel() {

    val uiState = ObservableField(UIState.EMPTY)

    private var _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private var _success = MutableLiveData<Boolean>()
    val success: LiveData<Boolean> get() = _success

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                repository.sendPasswordResetEmail(email).await()
                uiState.set(UIState.SUCCESS)
                _success.value = true
                _message.value = "An email reset link has been to sent to $email"
            } catch (e: Exception) {
                uiState.set(UIState.FAILURE)
                e.printStackTrace()
                _success.value = false
                _message.value = "An error occurred: ${e.localizedMessage}"
            }
        }
    }
}