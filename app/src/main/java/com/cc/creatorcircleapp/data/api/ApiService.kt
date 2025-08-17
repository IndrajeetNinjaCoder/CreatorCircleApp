package com.cc.creatorcircleapp.data.api


import com.cc.creatorcircleapp.data.models.LoginResponse
import com.cc.creatorcircleapp.data.models.SignUpRequest
import com.cc.creatorcircleapp.data.models.SignUpResponse
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

}
