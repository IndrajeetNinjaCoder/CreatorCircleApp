//package com.cc.creatorcircle.data.repository
//
//import com.cc.creatorcircle.data.api.ApiService
//import com.cc.creatorcircle.data.models.SignUpRequest
//import com.cc.creatorcircle.data.models.SignUpResponse
//import retrofit2.Response
//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory
//
//class AuthRepository {
//
//    // Create a proper ApiService instance
//    private val apiService: ApiService by lazy {
//        Retrofit.Builder()
//            .baseUrl("https://creatorcircle.in") // Replace with your actual base URL
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(ApiService::class.java)
//    }
//
//    suspend fun registerUser(signUpRequest: SignUpRequest): Response<SignUpResponse> {
//        return apiService.registerUser(signUpRequest)
//    }
//
//    suspend fun registerWithGoogle(idToken: String): Response<Any> {
//        val googleSignUpRequest = GoogleSignUpRequest(idToken = idToken)
//        return apiService.registerWithGoogle(googleSignUpRequest)
//    }
//}
//
//// Keep this data class for Google signup request
//data class GoogleSignUpRequest(
//    val idToken: String
//)










package com.cc.creatorcircle.data.repository

import com.cc.creatorcircle.data.api.ApiService
import com.cc.creatorcircle.data.models.SignUpRequest
import com.cc.creatorcircle.data.models.SignUpResponse
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepository {

    // Create a proper ApiService instance
    private val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://creatorcircle.in/api/") // Updated base URL to match your API
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    suspend fun registerUser(signUpRequest: SignUpRequest): Response<SignUpResponse> {
        return apiService.registerUser(signUpRequest)
    }

    suspend fun registerWithGoogle(idToken: String): Response<Any> {
        val googleSignUpRequest = GoogleSignUpRequest(idToken = idToken)
        return apiService.registerWithGoogle(googleSignUpRequest)
    }
}

// Keep this data class for Google signup request
data class GoogleSignUpRequest(
    val idToken: String
)