package com.certified.easyv.di

import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideFirebase(): Firebase = Firebase

//    @Provides
//    fun provideFirebaseRepository(): FirebaseRepository = FirebaseRepository()
}