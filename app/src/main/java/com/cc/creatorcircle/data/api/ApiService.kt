//package com.cc.creatorcircle.data.api
//
//import com.cc.creatorcircle.data.models.LoginResponse
//import com.cc.creatorcircle.data.models.SignUpRequest
//import com.cc.creatorcircle.data.models.SignUpResponse
//import com.cc.creatorcircle.data.repository.GoogleSignUpRequest
//import retrofit2.Response
//import retrofit2.http.Body
//import retrofit2.http.Field
//import retrofit2.http.FormUrlEncoded
//import retrofit2.http.POST
//
//interface ApiService {
//
//    @POST("auth/register")
//    suspend fun registerUser(
//        @Body request: SignUpRequest
//    ): Response<SignUpResponse>
//
//    @FormUrlEncoded
//    @POST("auth/login")
//    suspend fun login(
//        @Field("username") username: String,
//        @Field("password") password: String
//    ): Response<LoginResponse>
//
//    @POST("auth/google-register")
//    suspend fun registerWithGoogle(@Body googleSignUpRequest: GoogleSignUpRequest): Response<Any>
//}







package com.cc.creatorcircle.data.api

import com.cc.creatorcircle.data.models.LoginResponse
import com.cc.creatorcircle.data.models.SignUpRequest
import com.cc.creatorcircle.data.models.SignUpResponse
import com.cc.creatorcircle.data.repository.GoogleSignUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @POST("auth/register")
    suspend fun registerUser(
        @Body request: SignUpRequest
    ): Response<SignUpResponse>

    @FormUrlEncoded
    @POST("auth/login")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String
    ): Response<LoginResponse>

    @POST("auth/google-register")
    suspend fun registerWithGoogle(@Body googleSignUpRequest: GoogleSignUpRequest): Response<Any>
}