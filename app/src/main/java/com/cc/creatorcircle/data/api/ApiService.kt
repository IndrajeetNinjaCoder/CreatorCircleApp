package com.cc.creatorcircle.data.api

import com.cc.creatorcircle.data.models.AddChatProfileRequest
import com.cc.creatorcircle.data.models.AddChatProfileResponse
import com.cc.creatorcircle.data.models.AddCommentResponse
import com.cc.creatorcircle.data.models.AddReplyResponse
import com.cc.creatorcircle.data.models.AvailabilityResponse
import com.cc.creatorcircle.data.models.BookSlotRequest
import com.cc.creatorcircle.data.models.BookSlotResponse
import com.cc.creatorcircle.data.models.CancelBookingResponse
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
import com.cc.creatorcircle.data.models.ConversationMessage
import com.cc.creatorcircle.data.models.CreateChatSessionRequest
import com.cc.creatorcircle.data.models.CreateChatSessionResponse
import com.cc.creatorcircle.data.models.DeleteChatProfileResponse
import com.cc.creatorcircle.data.models.DeleteChatSessionResponse
import com.cc.creatorcircle.data.models.DeleteMessageResponse
import com.cc.creatorcircle.data.models.DeletePostResponse
import com.cc.creatorcircle.data.models.InfluencerFilterRequest
import com.cc.creatorcircle.data.models.Influencers
import com.cc.creatorcircle.data.models.InfluencersResponse
import com.cc.creatorcircle.data.models.LikeResponse
import com.cc.creatorcircle.data.models.LiveSessionAvailabilityResponse
import com.cc.creatorcircle.data.models.LoginResponse
import com.cc.creatorcircle.data.models.LogoutResponse
import com.cc.creatorcircle.data.models.MentorConfigurationResponse
import com.cc.creatorcircle.data.models.PostResponse
import com.cc.creatorcircle.data.models.PostsResponse
import com.cc.creatorcircle.data.models.RemoveConnectionResponse
import com.cc.creatorcircle.data.models.SendMessageRequest
import com.cc.creatorcircle.data.models.SendMessageResponse
import com.cc.creatorcircle.data.models.Session
import com.cc.creatorcircle.data.models.SessionsRequest
import com.cc.creatorcircle.data.models.SessionsResponse
import com.cc.creatorcircle.data.models.SetProfileActiveRequest
import com.cc.creatorcircle.data.models.SetProfileActiveResponse
import com.cc.creatorcircle.data.models.SignUpRequest
import com.cc.creatorcircle.data.models.SignUpResponse
import com.cc.creatorcircle.data.models.SocialMediaResponse
import com.cc.creatorcircle.data.models.UpdateMentorConfigurationRequest
import com.cc.creatorcircle.data.models.UpdateMentorConfigurationResponse
import com.cc.creatorcircle.data.models.UpdatePostResponse
import com.cc.creatorcircle.data.models.UserDiscoveryResponse
import com.cc.creatorcircle.data.models.UserProfile
import com.cc.creatorcircle.data.repository.GoogleSignUpRequest
import com.example.creatorcircle.models.ApplyToDealRequest
import com.example.creatorcircle.models.ApplyToDealResponse
import com.example.creatorcircle.models.BrandsResponse
import com.example.creatorcircle.models.DealsResponse
import com.example.creatorcircle.models.GenerateEmailRequest
import com.example.creatorcircle.models.GenerateEmailResponse
import com.example.creatorcircle.models.SaveDefaultFiltersRequest
import com.example.creatorcircle.models.SaveDefaultFiltersResponse
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
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
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


    @GET("user/profile/{userId}")
    suspend fun getUserProfileById(
        @Header("Authorization") authorization: String,
        @Path("userId") userId: Int
    ): Response<UserProfile>


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



    @Multipart
    @POST("user/update-user")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Part("full_name") fullName: RequestBody? = null,
        @Part("password") password: RequestBody? = null,
        @Part("mobile_number") mobileNumber: RequestBody? = null,
        @Part("platform_followers") platformFollowers: RequestBody? = null,
        @Part("username") username: RequestBody? = null,
        @Part("social_media_links") socialMediaLinks: RequestBody? = null,
        @Part("onboardingStatus") onboardingStatus: RequestBody? = null,
        @Part("categories") categories: RequestBody? = null,
        @Part("bio") bio: RequestBody? = null,
        @Part("age") age: RequestBody? = null,
        @Part profile_pic: MultipartBody.Part? = null  // For file upload
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




    @POST("live-session/seek/suggest_influencers")
    suspend fun getSuggestedInfluencers(
        @Header("Authorization") token: String
    ): Response<List<Influencers>>

    @POST("live-session/seek/suggest_influencers")
    suspend fun getFilteredInfluencers(
        @Header("Authorization") token: String,
        @Body filterRequest: InfluencerFilterRequest
    ): Response<List<Influencers>>



    @POST("live-session/seek/book-slot")
    suspend fun bookLiveSessionSlot(
        @Body request: BookSlotRequest,
        @Header("Authorization") token: String
    ): Response<BookSlotResponse>


    @GET("live-session/seek/{userId}/availability")
    suspend fun getMentorAvailability(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): Response<AvailabilityResponse>


    @POST("sessions")
    suspend fun getSessions(
        @Body request: SessionsRequest,
        @Header("Authorization") token: String
    ): Response<List<Session>>

    @PATCH("live-session/seek/cancel-booking/{bookingId}")
    suspend fun cancelBooking(
        @Path("bookingId") bookingId: Int,
        @Header("Authorization") token: String
    ): Response<CancelBookingResponse>



    @GET("live-session/provide-guidance/configuration")
    suspend fun getGuidanceConfiguration(
        @Header("Authorization") token: String
    ): Response<MentorConfigurationResponse>

    @PUT("live-session/provide-guidance/configuration/{configId}")
    suspend fun updateGuidanceConfiguration(
        @Path("configId") configId: Int,
        @Header("Authorization") token: String,
        @Body request: UpdateMentorConfigurationRequest
    ): Response<UpdateMentorConfigurationResponse>


    @POST("api/messages/send")
    suspend fun sendMessage(
        @Body request: SendMessageRequest,
        @Header("Authorization") token: String
    ): Response<SendMessageResponse>


    @GET("api/messages/conversation/{userId}")
    suspend fun getConversation(
        @Path("userId") userId: Int,
        @Header("Authorization") token: String
    ): Response<List<ConversationMessage>>


    @DELETE("api/messages/{messageId}")
    suspend fun deleteMessage(
        @Path("messageId") messageId: Int,
        @Header("Authorization") token: String
    ): Response<DeleteMessageResponse>


    @GET("brands/deals")
    suspend fun getDeals(
        @Header("Authorization") token: String,
        @Query("category") category: String? = null,
        @Query("collaboration_type") collaborationType: String? = null,
        @Query("location") location: String? = null,
        @Query("min_amount") minAmount: String? = null,
        @Query("max_amount") maxAmount: String? = null,
        @Query("status") status: String? = null,
        @Query("brand_id") brandId: Int? = null
//        @Query("limit") limit: Int? = 20,
//        @Query("offset") offset: Int? = 0
    ): Response<DealsResponse>

    @GET("brands/")
    suspend fun getBrands(
        @Header("Authorization") token: String,
        @Query("category") category: String? = null,
        @Query("states") states: List<String>? = null,
        @Query("country") country: String? = null,
        @Query("profile_id") profileId: Int? = null,
        @Query("limit") limit: Int? = 20,
        @Query("offset") offset: Int? = 0
    ): Response<BrandsResponse>


    @POST("brands/default-filters")
    suspend fun saveDefaultFilters(
        @Body request: SaveDefaultFiltersRequest,
        @Header("Authorization") token: String
    ): Response<SaveDefaultFiltersResponse>


    @POST("brands/set_email_texts/")
    suspend fun generateEmail(
        @Body request: GenerateEmailRequest,
        @Header("Authorization") token: String
    ): Response<GenerateEmailResponse>

    @POST("brands/deals/{dealId}/apply")
    suspend fun applyToDeal(
        @Path("dealId") dealId: Int,
        @Body request: ApplyToDealRequest,
        @Header("Authorization") token: String
    ): Response<ApplyToDealResponse>

    @GET("live-session/seek/{sessionId}/availability")
    suspend fun getLiveSessionAvailability(
        @Path("sessionId") sessionId: Int,
        @Header("Authorization") token: String
    ): Response<LiveSessionAvailabilityResponse>


    @DELETE("chat/profiles/remove/{profileId}")
    suspend fun deleteChatProfile(
        @Path("profileId") profileId: Int,
        @Query("user_id") userId: Int,
        @Header("Authorization") token: String
    ): Response<DeleteChatProfileResponse>

    @DELETE("chat/{sessionId}")
    suspend fun deleteChatSession(
        @Path("sessionId") sessionId: Int,
        @Header("Authorization") token: String
    ): Response<DeleteChatSessionResponse>

    @GET("posts/user_posts")
    suspend fun getUserPosts(
        @Query("target_user_id") targetUserId: Int,
        @Header("Authorization") token: String
    ): Response<PostsResponse>



    @DELETE("posts/{postId}")
    suspend fun deletePost(
        @Path("postId") postId: String,
        @Header("Authorization") token: String
    ): Response<DeletePostResponse>


//    @Multipart
//    @PUT("posts/{post_id}")
//    suspend fun updatePost(
//        @Header("Authorization") authorization: String,
//        @Path("post_id") postId: String,
//        @Part("content") content: RequestBody,
//        @Part("existing_media_urls") existingMediaUrls: List<RequestBody>? = null,
//        @Part new_media_files: List<MultipartBody.Part>? = null
//    ): Response<UpdatePostResponse>

//    @Multipart
//    @PUT("posts/{post_id}")
//    suspend fun updatePost(
//        @Header("Authorization") authorization: String,
//        @Path("post_id") postId: String,
//        @Part("content") content: RequestBody,
//        @PartMap existingMediaUrls: Map<String, RequestBody>,
//        @Part new_media_files: List<MultipartBody.Part>? = null
//    ): Response<UpdatePostResponse>


//    @Multipart
//    @PUT("posts/{post_id}")
//    suspend fun updatePost(
//        @Header("Authorization") authorization: String,
//        @Path("post_id") postId: String,
//        @Part("content") content: RequestBody,
//        @PartMap existingMediaUrls: Map<String, @JvmSuppressWildcards RequestBody>, // Add @JvmSuppressWildcards
//        @Part new_media_files: List<MultipartBody.Part>? = null
//    ): Response<UpdatePostResponse>


    @Multipart
    @PUT("posts/{post_id}")
    suspend fun updatePost(
        @Header("Authorization") authorization: String,
        @Path("post_id") postId: String,
        @Part("content") content: RequestBody,
        @Part("existing_media_urls") existingMediaUrls: RequestBody?, // Send as JSON array string
        @Part new_media_files: List<MultipartBody.Part>? = null
    ): Response<UpdatePostResponse>



}
