package com.cc.creatorcircle.data.api

import com.cc.creatorcircle.data.models.AddChatProfileRequest
import com.cc.creatorcircle.data.models.AddChatProfileResponse
import com.cc.creatorcircle.data.models.AddCommentResponse
import com.cc.creatorcircle.data.models.AddReplyResponse
import com.cc.creatorcircle.data.models.CancelConnectionResponse
import com.cc.creatorcircle.data.models.ChatHistoryResponse
import com.cc.creatorcircle.data.models.ChatMessagesResponse
import com.cc.creatorcircle.data.models.ChatSessionResponse
import com.cc.creatorcircle.data.models.ChatUserProfile
import com.cc.creatorcircle.data.models.ChatUserProfilesResponse
import com.cc.creatorcircle.data.models.CommentLikeResponse
import com.cc.creatorcircle.data.models.CommentResponse
import com.cc.creatorcircle.data.models.ConnectionActionResponse
import com.cc.creatorcircle.data.models.ConnectionResponse
import com.cc.creatorcircle.data.models.CreateChatSessionRequest
import com.cc.creatorcircle.data.models.CreateChatSessionResponse
import com.cc.creatorcircle.data.models.LikeResponse
import com.cc.creatorcircle.data.models.LoginResponse
import com.cc.creatorcircle.data.models.LogoutResponse
import com.cc.creatorcircle.data.models.PostResponse
import com.cc.creatorcircle.data.models.PostsResponse
import com.cc.creatorcircle.data.models.RemoveConnectionResponse
import com.cc.creatorcircle.data.models.SetProfileActiveRequest
import com.cc.creatorcircle.data.models.SetProfileActiveResponse
import com.cc.creatorcircle.data.models.SignUpRequest
import com.cc.creatorcircle.data.models.SignUpResponse
import com.cc.creatorcircle.data.models.SocialMediaResponse
import com.cc.creatorcircle.data.models.UserDiscoveryResponse
import com.cc.creatorcircle.data.models.UserProfile
import com.cc.creatorcircle.data.repository.GoogleSignUpRequest
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
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


    @POST("auth/logout")
    suspend fun logout(
        @Header("Authorization") token: String
    ): Response<LogoutResponse>


    @GET("posts")
    suspend fun getPosts(
        @Header("Authorization") token: String,
        @Query("post_type") postType: String = "",
        @Query("limit") limit: Int = 10000000,
        @Query("offset") offset: Int = 0
    ): Response<PostsResponse>

    @GET("posts")
    suspend fun getAllPosts(
        @Header("Authorization") token: String,
        @Query("limit") limit: Int = 10000000,
        @Query("offset") offset: Int = 0
    ): Response<PostsResponse>


    @POST("posts/{postId}/like-toggle")
    suspend fun toggleLike(
        @Header("Authorization") token: String,
        @Path("postId") postId: String
    ): Response<LikeResponse>


    @POST("posts/comments/{commentId}/like-toggle")
    suspend fun toggleCommentLike(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Int
    ): Response<CommentLikeResponse>


    // Updated: Added limit and nested query parameters with default values
    @GET("posts/{postId}/comments")
    suspend fun getComments(
        @Header("Authorization") token: String,
        @Path("postId") postId: String,
        @Query("limit") limit: Int = 20,
        @Query("nested") nested: Boolean = true
    ): Response<CommentResponse>

    // Optional: Add method to post a new comment
    @FormUrlEncoded
    @POST("posts/{postId}/comments")
    suspend fun addComment(
        @Header("Authorization") token: String,
        @Path("postId") postId: String,
        @Field("content") content: String
    ): Response<AddCommentResponse>


    @FormUrlEncoded
    @POST("posts/{postId}/comments")
    suspend fun addReply(
        @Header("Authorization") token: String,
        @Path("postId") postId: String,
        @Field("content") content: String,
        @Field("parent_id") parentId: Int
    ): Response<AddReplyResponse>


    @GET("user/get-user")
    suspend fun getUserProfile(
        @Header("Authorization") token: String
    ): Response<UserProfile>

    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") authorization: String,
        @Part("content") content: RequestBody,
        @Part media_files: List<MultipartBody.Part>? = null
    ): Response<PostResponse>


    @Multipart
    @POST("posts/resource")
    suspend fun createPostResource(
        @Header("Authorization") authorization: String,
        @Part("content") content: RequestBody,
        @Part media_files: List<MultipartBody.Part>? = null
    ): Response<PostResponse>


    @POST("user/connect/send/{userId}")
    suspend fun sendConnectionRequest(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<ConnectionResponse>


    @DELETE("user/connect/remove/{userId}")
    suspend fun removeConnection(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<RemoveConnectionResponse>


    @DELETE("user/connect/cancel/{userId}")
    suspend fun cancelConnectionRequest(
        @Header("Authorization") token: String,
        @Path("userId") userId: Int
    ): Response<CancelConnectionResponse>


    @FormUrlEncoded
    @POST("user/connect/respond/{connectionId}")
    suspend fun RespondConnectionRequest(
        @Header("Authorization") token: String,
        @Path("connectionId") connectionId: Int,
        @Field("action") action: String  // Changed from @Part to @Field for form data
    ): Response<ConnectionActionResponse>


    @GET("recommendations")
    suspend fun getRecommendations(
        @Header("Authorization") token: String,
        @Query("limit") nested: Int = 50
    ): Response<UserDiscoveryResponse>




    @GET("user/connect")
    suspend fun getUserConnections(
        @Header("Authorization") token: String
    ): Response<SocialMediaResponse>

    @FormUrlEncoded
    @POST("user/update-user")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Field("full_name") fullName: String? = null,
        @Field("password") password: String? = null,
        @Field("mobile_number") mobileNumber: String? = null,
        @Field("platform_followers") platformFollowers: String? = null,
        @Field("username") username: String? = null,
        @Field("social_media_links") socialMediaLinks: String? = null,
        @Field("onboardingStatus") onboardingStatus: Boolean? = false,
        @Field("categories") categories: String? = null,
        @Field("bio") bio: String? = null,
        @Field("age") age: Int? = null
    ): Response<SocialMediaResponse>




    // Chat API endpoints
    @GET("chat/profiles/{user_id}")
    suspend fun getChatUserProfile(
        @Path("user_id") userId: Int,
        @Header("Authorization") token: String
    ): Response<ChatUserProfilesResponse>

    @POST("chat/new-session")
    suspend fun createNewChatSession(
        @Body request: CreateChatSessionRequest,
        @Header("Authorization") token: String
    ): Response<CreateChatSessionResponse>

    @GET("chat/history/{user_id}")
    suspend fun getChatHistory(
        @Path("user_id") userId: Int,
        @Header("Authorization") token: String,
        @Query("platform") platform: String? = null,
        @Query("profile_id") profileId: Int? = null
    ): Response<ChatHistoryResponse>

    @GET("chat/session/{session_id}")
    suspend fun getChatSession(
        @Path("session_id") sessionId: Int,
        @Header("Authorization") token: String
    ): Response<ChatSessionResponse>

    @GET("chat/{session_id}/messages")
    suspend fun getChatMessages(
        @Path("session_id") sessionId: Int,
        @Header("Authorization") token: String
    ): Response<ChatMessagesResponse>


    // New method for setting profile as active
    @PUT("chat/profiles/{profile_id}/set-active")
    suspend fun setProfileActive(
        @Path("profile_id") profileId: Int,
        @Body request: SetProfileActiveRequest,
        @Header("Authorization") token: String
    ): Response<SetProfileActiveResponse>


    // New method for adding/creating a chat profile
    @POST("chat/profiles/add")
    suspend fun addChatProfile(
        @Body request: AddChatProfileRequest,
        @Header("Authorization") token: String
    ): Response<AddChatProfileResponse>












}