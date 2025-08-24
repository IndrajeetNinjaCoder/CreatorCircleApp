package com.cc.creatorcircle.data.api

import com.cc.creatorcircle.data.models.LikeResponse
import com.cc.creatorcircle.data.models.LoginResponse
import com.cc.creatorcircle.data.models.PostsResponse
import com.cc.creatorcircle.data.models.SignUpRequest
import com.cc.creatorcircle.data.models.SignUpResponse
import com.cc.creatorcircle.data.repository.GoogleSignUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
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


    @GET("posts")
    suspend fun getPosts(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<PostsResponse>


    @POST("posts/{postId}/like-toggle")
    suspend fun toggleLike(
        @Header("Authorization") token: String,
        @Path("postId") postId: String
    ): Response<LikeResponse>


}