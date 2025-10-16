package com.live.azurah.retrofit

import com.live.azurah.model.AddBookmarkResponse
import com.live.azurah.model.AddWishlistResponse
import com.live.azurah.model.BibleQuestListResponse
import com.live.azurah.model.BibleQuestViewModel
import com.live.azurah.model.BlockResposne
import com.live.azurah.model.CheckUsernameResponse
import com.live.azurah.model.CommentCommonResponse
import com.live.azurah.model.CommonResponse
import com.live.azurah.model.CommunityCategoryResponse
import com.live.azurah.model.CommunityForumResponse
import com.live.azurah.model.ContentResponse
import com.live.azurah.model.CountResponse
import com.live.azurah.model.DashBoardResponse
import com.live.azurah.model.DashboardDataResposne
import com.live.azurah.model.DetailResponse
import com.live.azurah.model.EventCategoryResponse
import com.live.azurah.model.EventDetailResponse
import com.live.azurah.model.EventListResponse
import com.live.azurah.model.EventResponse
import com.live.azurah.model.ExploreGroupResponse
import com.live.azurah.model.FileUploadResponse
import com.live.azurah.model.FollowFollowingResponse
import com.live.azurah.model.HomeSearchResposne
import com.live.azurah.model.InterestResponse
import com.live.azurah.model.LoginResponse
import com.live.azurah.model.MuteResponse
import com.live.azurah.model.NotificationListingResponse
import com.live.azurah.model.PostCommentListResposne
import com.live.azurah.model.PostLikesResposne
import com.live.azurah.model.PostResponse
import com.live.azurah.model.ProductDetailResponse
import com.live.azurah.model.ProductResponse
import com.live.azurah.model.ProfileResponse
import com.live.azurah.model.QuestionResponse
import com.live.azurah.model.RecentSearchResposne
import com.live.azurah.model.ReportFeedback
import com.live.azurah.model.SavedEventResponse
import com.live.azurah.model.SavedPostResponse
import com.live.azurah.model.ShopBannerResponse
import com.live.azurah.model.ShopCategoryResponse
import com.live.azurah.model.SignUpResponse
import com.live.azurah.model.ViewGroupResponse
import com.live.azurah.model.ViewPostResponse
import com.live.azurah.model.WishlistResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Path
import retrofit2.http.QueryMap


interface ApiServiceInterface {

    @FormUrlEncoded
    @POST(ApiConstants.SIGNUP)
    suspend fun signUp(@FieldMap map: HashMap<String, String>): SignUpResponse

    @FormUrlEncoded
    @POST(ApiConstants.VERIFY_OTP)
    suspend fun verifyOtp(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @PUT(ApiConstants.RESEND_OTP)
    suspend fun resendOtp(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.CREATE_USERNAME)
    suspend fun createUsername(@QueryMap map: HashMap<String, String>): CheckUsernameResponse

    @FormUrlEncoded
    @PUT(ApiConstants.EDIT_PROFILE)
    suspend fun editProfile(@FieldMap map: HashMap<String, String>): SignUpResponse

    @FormUrlEncoded
    @POST(ApiConstants.LOGIN)
    suspend fun login(@FieldMap map: HashMap<String, String>): LoginResponse

    @Multipart
    @POST(ApiConstants.FILE_UPLOAD)
    suspend fun fileUpload(
        @PartMap map: HashMap<String, RequestBody>, @Part images: ArrayList<MultipartBody.Part>
    ): FileUploadResponse

    @Multipart
    @POST(ApiConstants.FILE_UPLOAD)
    suspend fun postFileUpload(
        @PartMap map: HashMap<String, RequestBody>, @Part images: MultipartBody.Part
    ): FileUploadResponse

    @GET(ApiConstants.INTEREST_LIST)
    suspend fun getInterests(): InterestResponse

    @GET(ApiConstants.QUESTION_LIST)
    suspend fun getQuestion(): QuestionResponse

    @GET(ApiConstants.PROFILE)
    suspend fun getProfile(): ProfileResponse

    @FormUrlEncoded
    @POST(ApiConstants.CHANGE_PASSWORD)
    suspend fun changePassword(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @PUT(ApiConstants.UPDATE_NOTIFICATIONS)
    suspend fun updateNotification(@FieldMap map: HashMap<String, String>): CommonResponse

    @PUT(ApiConstants.LOGOUT)
    suspend fun logOut(): CommonResponse

    /*  @DELETE(ApiConstants.DELETE_ACCOUNT+"/{id}")
      suspend fun deleteAccount(id: String,@Body jsonObject: JSONObject): CommonResponse
  */
    @HTTP(method = "DELETE", path = ApiConstants.DELETE_ACCOUNT + "/{id}", hasBody = true)
    suspend fun deleteAccount(
        @Path("id") id: String, @Body jsonObject: HashMap<String, Any>
    ): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.SEND_ACCOUNT_DELETION_API)
    suspend fun sendDeleteMail(@FieldMap map: HashMap<String, Any>): CommonResponse


    @FormUrlEncoded
    @POST(ApiConstants.REPORT_FEEDBACK)
    suspend fun reportFeedback(@FieldMap map: HashMap<String, String>): ReportFeedback

    @FormUrlEncoded
    @POST(ApiConstants.CONTACT_US)
    suspend fun contactUs(@FieldMap map: HashMap<String, String>): ReportFeedback

    @GET(ApiConstants.TERMS_CONDITION)
    suspend fun getTermsCondition(): ContentResponse

    @GET(ApiConstants.PRIVACY_POLICY)
    suspend fun privacyPolicy(): ContentResponse

    @GET(ApiConstants.SHOP_BANNER)
    suspend fun shopBanner(@QueryMap map: HashMap<String, String>): ShopBannerResponse

    @GET(ApiConstants.SHOP_CATEGORY_LIST)
    suspend fun getShopCategory(@QueryMap map: HashMap<String, String>): ShopCategoryResponse

    @GET(ApiConstants.SHOP_PRODUCT_LIST)
    suspend fun getProductList(@QueryMap map: HashMap<String, String>): ProductResponse

    @FormUrlEncoded
    @PUT(ApiConstants.ADD_WISHLIST)
    suspend fun addWishList(@FieldMap map: HashMap<String, String>): AddWishlistResponse

    @FormUrlEncoded
    @PUT(ApiConstants.CHANGE_EMAIL_REQUEST)
    suspend fun changeEmailRequest(@FieldMap map: HashMap<String, String>): CommonResponse


    @GET(ApiConstants.PRODUCT_DETAIL + "/{id}")
    suspend fun getProductDetail(@Path("id") id: String): ProductDetailResponse

    @GET(ApiConstants.WISHLIST)
    suspend fun getWishList(@QueryMap map: HashMap<String, String>): WishlistResponse

    @GET(ApiConstants.EVENT_CATEGORY)
    suspend fun eventCategoryList(): EventCategoryResponse

    @GET(ApiConstants.EVENT_LIST)
    suspend fun eventList(@QueryMap map: HashMap<String, String>): EventListResponse

    @GET(ApiConstants.EVENT_LIST2)
    suspend fun eventList2(@QueryMap map: HashMap<String, String>): EventResponse

    @FormUrlEncoded
    @PUT(ApiConstants.EVENT_BOOKMARK)
    suspend fun eventBookmark(@FieldMap map: HashMap<String, String>): AddBookmarkResponse

    @GET(ApiConstants.EVENT_BOOKMARK_LIST)
    suspend fun eventBookmarkList(@QueryMap map: HashMap<String, String>): SavedEventResponse

    @GET(ApiConstants.EVENT_VIEW + "/{id}")
    suspend fun viewEvent(@Path("id") id: String): EventDetailResponse

    @FormUrlEncoded
    @POST(ApiConstants.EVENT_BUY_TICKET)
    suspend fun eventBuyTicket(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.ADD_POST)
    suspend fun addPost(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.DASHBOARD_LIST)
    suspend fun dashboardList(@QueryMap map: HashMap<String, String>): DashBoardResponse

    @GET(ApiConstants.DASHBOARD_DATA)
    suspend fun dashboardData(@QueryMap map: HashMap<String, String>): DashboardDataResposne

    @GET(ApiConstants.POST_LIST)
    suspend fun getPostList(@QueryMap map: HashMap<String, String>): PostResponse

    @FormUrlEncoded
    @POST(ApiConstants.POST_LIKE_UNLIKE)
    suspend fun postLikeUnlike(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @PUT(ApiConstants.POST_BOOKMARK)
    suspend fun postBookmark(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.POST_LIKE_LIST)
    suspend fun getPostLikes(@QueryMap map: HashMap<String, String>): PostLikesResposne


    @FormUrlEncoded
    @POST(ApiConstants.FOLLOW_UNFOLLOW)
    suspend fun followUnfollow(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.COMMUNITY_CATEGORY_LIST)
    suspend fun getCommunityCategory(@QueryMap map: HashMap<String, String>): CommunityCategoryResponse

    @GET(ApiConstants.COMMUNITY_FORUM_LIST)
    suspend fun getCommunityForumList(@QueryMap map: HashMap<String, String>): CommunityForumResponse

    @FormUrlEncoded
    @POST(ApiConstants.COMMUNITY_LIKE_UNLIKE)
    suspend fun communityLikeUnlike(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.COMMUNITY_LIKE_LIST)
    suspend fun getCommunityLikeList(@QueryMap map: HashMap<String, String>): PostLikesResposne

    @FormUrlEncoded
    @POST(ApiConstants.ADD_COMMUNITY)
    suspend fun addCommunity(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.PRAYER_CATEGORY_LIST)
    suspend fun getPrayerCategoryList(@QueryMap map: HashMap<String, String>): CommunityCategoryResponse

    @GET(ApiConstants.PRAYER_LIST)
    suspend fun getPrayerList(@QueryMap map: HashMap<String, String>): CommunityForumResponse

    @FormUrlEncoded
    @POST(ApiConstants.PRAYER_LIKE_UNLIKE)
    suspend fun prayerLikeUnlike(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.PRAYER_LIKE_LIST)
    suspend fun getPrayerLikeList(@QueryMap map: HashMap<String, String>): PostLikesResposne

    @GET(ApiConstants.PRAISE_LIKE_LIST)
    suspend fun getPraiseLikeList(@QueryMap map: HashMap<String, String>): PostLikesResposne

    @GET(ApiConstants.TESTIMONY_PRAISE_LIST)
    suspend fun getTestimonyPraiseList(@QueryMap map: HashMap<String, String>): PostLikesResposne
    @FormUrlEncoded
    @POST(ApiConstants.PRAYER_ADD)
    suspend fun addPrayer(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.TESTIMONY_CATEGORY_LIST)
    suspend fun getTestimonyCategoryList(@QueryMap map: HashMap<String, String>): CommunityCategoryResponse

    @GET(ApiConstants.TESTIMONY_LIST)
    suspend fun getTestimonyList(@QueryMap map: HashMap<String, String>): CommunityForumResponse

    @FormUrlEncoded
    @POST(ApiConstants.TESTIMONY_LIKE_UNLIKE)
    suspend fun testimonyLikeUnlike(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.TESTIMONY_LIKE_LIST)
    suspend fun getTestimonyLikeList(@QueryMap map: HashMap<String, String>): PostLikesResposne

    @FormUrlEncoded
    @POST(ApiConstants.TESTIMONY_ADD)
    suspend fun addTestimony(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.PRAYER_VIEW + "/{id}")
    suspend fun prayerView(@Path("id") id: String): DetailResponse

    @GET(ApiConstants.TESTIMONY_VIEW + "/{id}")
    suspend fun testimonyView(@Path("id") id: String): DetailResponse

    @GET(ApiConstants.COMMUNITY_VIEW + "/{id}")
    suspend fun communityView(@Path("id") id: String): DetailResponse

    @FormUrlEncoded
    @POST(ApiConstants.PRAYER_PRAISE_UNPRAISE)
    suspend fun prayerPriseUnpraise(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.TESTIMONY_PRAISE_UNPRAISE)
    suspend fun testimonyPriseUnpraise(@FieldMap map: HashMap<String, String>): CommonResponse

    @DELETE(ApiConstants.TESTIMONY_DELETE + "/{id}")
    suspend fun deleteTestimony(@Path("id") id: String): CommonResponse

    @DELETE(ApiConstants.PRAYER_DELETE + "/{id}")
    suspend fun deletePrayer(@Path("id") id: String): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.PRAYER_REPORT)
    suspend fun prayerReport(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.TESTIMONY_REPORT)
    suspend fun testimonyReport(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.USER_REPORT)
    suspend fun userReport(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.COMMUNITY_REPORT)
    suspend fun communityReport(@FieldMap map: HashMap<String, String>): CommonResponse

    @DELETE(ApiConstants.COMMUNITY_DELETE + "/{id}")
    suspend fun deleteCommunity(@Path("id") id: String): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.USER_BLOCK)
    suspend fun userBlock(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.BLOCK_LIST)
    suspend fun getBlockList(): BlockResposne

    @FormUrlEncoded
    @POST(ApiConstants.POST_REPORT)
    suspend fun postReport(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.SUGGESTION_LIST)
    suspend fun suggestionList(@QueryMap map: HashMap<String, String>): PostLikesResposne

    @DELETE(ApiConstants.POST_DELETE + "/{id}")
    suspend fun postDelete(@Path("id") id: String): CommonResponse

    @GET(ApiConstants.POST_BOOKMARK_LIST)
    suspend fun getPostBookmark(@QueryMap map: HashMap<String, String>): SavedPostResponse

    @GET(ApiConstants.USER_FOLLOW_FOLLOWING_LIST)
    suspend fun userFollowFollowingList(@QueryMap map: HashMap<String, String>): FollowFollowingResponse

    @GET(ApiConstants.OTHER_USER_PROFILE + "/{id}")
    suspend fun otherUserProfile(@Path("id") id: String): ProfileResponse

    @FormUrlEncoded
    @POST(ApiConstants.ADD_POST_COMMENT)
    suspend fun addPostComment(@FieldMap map: HashMap<String, String>): CommentCommonResponse

    @GET(ApiConstants.POST_COMMENT_LIST)
    suspend fun postCommentList(@QueryMap map: HashMap<String, String>): PostCommentListResposne

    @FormUrlEncoded
    @POST(ApiConstants.POST_COMMENT_LIKE_UNLIKE)
    suspend fun postCommentLikeUnlike(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.POST_COMMENT_EDIT)
    suspend fun postCommentEdit(@FieldMap map: HashMap<String, String>): CommentCommonResponse

    /*   @DELETE(ApiConstants.POST_COMMENT_DELETE+"/{id}")
       suspend fun postCommentDelete(@Path("id") id: String): CommonResponse*/
    @HTTP(method = "DELETE", path = ApiConstants.POST_COMMENT_DELETE + "/{id}", hasBody = true)
    suspend fun postCommentDelete(
        @Path("id") id: String, @Body body: Map<String, String>
    ): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.POST_COMMENT_REPORT)
    suspend fun postCommentReport(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.ADD_PRAYER_COMMENT)
    suspend fun addPrayerComment(@FieldMap map: HashMap<String, String>): CommentCommonResponse

    @GET(ApiConstants.PRAYER_COMMENT_LIST)
    suspend fun prayerCommentList(@QueryMap map: HashMap<String, String>): PostCommentListResposne

    @FormUrlEncoded
    @POST(ApiConstants.PRAYER_COMMENT_LIKE_UNLIKE)
    suspend fun prayerCommentLikeUnlike(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.PRAYER_COMMENT_EDIT)
    suspend fun prayerCommentEdit(@FieldMap map: HashMap<String, String>): CommentCommonResponse


    @HTTP(method = "DELETE", path = ApiConstants.PRAYER_COMMENT_DELETE + "/{id}", hasBody = true)
    suspend fun prayerCommentDelete(
        @Path("id") id: String, @Body body: Map<String, String>
    ): CommonResponse

    /* @DELETE(ApiConstants.PRAYER_COMMENT_DELETE+"/{id}")
     suspend fun prayerCommentDelete(@Path("id") id: String, body: Map<String, String>): CommonResponse*/

    @FormUrlEncoded
    @POST(ApiConstants.PRAYER_COMMENT_REPORT)
    suspend fun prayerCommentReport(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.ADD_TESTIMONY_COMMENT)
    suspend fun addTestimonyComment(@FieldMap map: HashMap<String, String>): CommentCommonResponse

    @GET(ApiConstants.TESTIMONY_COMMENT_LIST)
    suspend fun testimonyCommentList(@QueryMap map: HashMap<String, String>): PostCommentListResposne

    @FormUrlEncoded
    @POST(ApiConstants.TESTIMONY_COMMENT_LIKE_UNLIKE)
    suspend fun testimonyCommentLikeUnlike(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.TESTIMONY_COMMENT_EDIT)
    suspend fun testimonyCommentEdit(@FieldMap map: HashMap<String, String>): CommentCommonResponse

    /* @DELETE(ApiConstants.TESTIMONY_COMMENT_DELETE+"/{id}")
     suspend fun testimonyCommentDelete(@Path("id") id: String): CommonResponse*/
    @HTTP(method = "DELETE", path = ApiConstants.TESTIMONY_COMMENT_DELETE + "/{id}", hasBody = true)
    suspend fun testimonyCommentDelete(
        @Path("id") id: String, @Body body: Map<String, String>
    ): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.TESTIMONY_COMMENT_REPORT)
    suspend fun testimonyCommentReport(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.ADD_COMMUNITY_COMMENT)
    suspend fun addCommunityComment(@FieldMap map: HashMap<String, String>): CommentCommonResponse

    @GET(ApiConstants.COMMUNITY_COMMENT_LIST)
    suspend fun communityCommentList(@QueryMap map: HashMap<String, String>): PostCommentListResposne

    @FormUrlEncoded
    @POST(ApiConstants.COMMUNITY_COMMENT_LIKE_UNLIKE)
    suspend fun communityCommentLikeUnlike(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.COMMUNITY_COMMENT_EDIT)
    suspend fun communityCommentEdit(@FieldMap map: HashMap<String, String>): CommentCommonResponse

    @HTTP(method = "DELETE", path = ApiConstants.COMMUNITY_COMMENT_DELETE + "/{id}", hasBody = true)
    suspend fun communityCommentDelete(
        @Path("id") id: String, @Body body: Map<String, String>
    ): CommonResponse

    /* @DELETE(ApiConstants.COMMUNITY_COMMENT_DELETE+"/{id}")
     suspend fun communityCommentDelete(@Path("id") id: String): CommonResponse
 */
    @FormUrlEncoded
    @POST(ApiConstants.COMMUNITY_COMMENT_REPORT)
    suspend fun communityCommentReport(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.GROUP_CATEGORY_LIST)
    suspend fun groupCategoryList(@QueryMap map: HashMap<String, String>): CommunityCategoryResponse

    @GET(ApiConstants.GROUP_LIST)
    suspend fun getGroupList(@QueryMap map: HashMap<String, String>): ExploreGroupResponse

    @GET(ApiConstants.GROUP_VIEW + "/{id}")
    suspend fun getGroupView(@Path("id") id: String): ViewGroupResponse

    @GET(ApiConstants.BIBLE_QUEST_CATEGORY_LIST)
    suspend fun bibleQuestCategoryList(@QueryMap map: HashMap<String, String>): CommunityCategoryResponse

    @GET(ApiConstants.BIBLE_QUEST_LIST)
    suspend fun getBibleQuestList(@QueryMap map: HashMap<String, String>): BibleQuestListResponse

    @GET(ApiConstants.BIBLE_QUEST_VIEW + "/{id}")
    suspend fun getBibleView(@Path("id") id: String): BibleQuestViewModel

    @FormUrlEncoded
    @POST(ApiConstants.BIBLE_CHALLENGE_COMPLETE)
    suspend fun markAsComplete(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.BIBLE_CHALLENGE_RESTART)
    suspend fun markAsRestart(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.NOTIFICATION_LISTING)
    suspend fun getNotifications(): NotificationListingResponse

    @GET(ApiConstants.HOME_SEARCH)
    suspend fun getHomeSearch(@QueryMap map: HashMap<String, String>): HomeSearchResposne

    @GET(ApiConstants.RECENT_SEARCH)
    suspend fun getRecentSearch(@QueryMap map: HashMap<String, String>): RecentSearchResposne

    @FormUrlEncoded
    @PUT(ApiConstants.RECENT_SEARCH_DELETE)
    suspend fun deleteRecentSearch(@FieldMap map: HashMap<String, String>): CommonResponse

    @FormUrlEncoded
    @POST(ApiConstants.REPORT_PROMPT)
    suspend fun reportPrompt(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.GET_MUTE_STATUS)
    suspend fun getMuteStatus(@QueryMap map: HashMap<String, String>): MuteResponse

    @GET(ApiConstants.VIEW_POST + "/{id}")
    suspend fun viewPostView(@Path("id") id: String): ViewPostResponse

    @FormUrlEncoded
    @POST(ApiConstants.FORGOT_PASSWORD)
    suspend fun forgotPassword(@FieldMap map: HashMap<String, String>): LoginResponse

    @FormUrlEncoded
    @PUT(ApiConstants.NEW_PASSWORD)
    suspend fun newPassword(@FieldMap map: HashMap<String, String>): LoginResponse

    @FormUrlEncoded
    @POST(ApiConstants.VERIFY_PASSWORD)
    suspend fun verifyPassword(@FieldMap map: HashMap<String, String>): LoginResponse

    @FormUrlEncoded
    @POST(ApiConstants.ADD_RATING)
    suspend fun addRating(@FieldMap map: HashMap<String, String>): CommonResponse

    @GET(ApiConstants.PRODUCT_MAY_LIKE)
    suspend fun productMayLike(@QueryMap map: HashMap<String, String>): ProductResponse

    @GET(ApiConstants.GET_COUNTS)
    suspend fun getCounts(): CountResponse


}

