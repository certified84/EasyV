package com.certified.easyv.data.repository

import android.net.Uri
import com.certified.easyv.data.model.Candidate
import com.certified.easyv.data.model.User
import com.certified.easyv.data.model.Voter
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import javax.inject.Inject

class FirebaseRepository @Inject constructor() {

    fun createUserWithEmailAndPassword(email: String, password: String) =
        Firebase.auth.createUserWithEmailAndPassword(email, password)

    fun uploadDetails(userID: String, newUser: User): Task<Void> {
        return Firebase.firestore.collection("users").document(userID).set(newUser)
    }

    fun signInWithEmailAndPassword(email: String, password: String) =
        Firebase.auth.signInWithEmailAndPassword(email, password)

    fun sendPasswordResetEmail(email: String) =
        Firebase.auth.sendPasswordResetEmail(email)

    fun uploadImage(uri: Uri?): Task<Void>? {
        val profileChangeRequest = userProfileChangeRequest { photoUri = uri }
        return Firebase.auth.currentUser?.updateProfile(profileChangeRequest)
    }

    fun updateProfile(userID: String, uri: String): Task<Void> {
        return Firebase.firestore.collection("users").document(userID)
            .update("profile_image", uri)
    }

    fun uploadCandidate(candidate: Candidate): Task<Void> {
        return Firebase.firestore.collection("candidate").document(candidate.name).set(candidate)
    }

    fun updateCandidateImage(candidateID: String, uri: String): Task<Void> {
        return Firebase.firestore.collection("candidate").document(candidateID)
            .update("profile_image", uri)
    }

    fun updateCandidateVoteCount(candidate: Candidate): Task<Void> {
        return Firebase.firestore.collection("candidate").document(candidate.name)
            .update("votes", candidate.votes)
    }

    fun uploadVoter(voter: Voter): Task<Void> {
        return Firebase.firestore.collection("voter").document(voter.id).set(voter)
    }
}