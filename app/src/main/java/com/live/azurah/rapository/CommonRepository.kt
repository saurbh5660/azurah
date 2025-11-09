package com.live.azurah.rapository


import android.app.Activity
import com.live.azurah.retrofit.ApiServiceInterface
import com.live.azurah.retrofit.Resource
import com.live.azurah.retrofit.ResponseHandler
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject


class CommonRepository @Inject constructor(
    private val apiService: ApiServiceInterface,
    private val responseHandler: ResponseHandler
) {

    suspend fun signUp(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.signUp(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun verifyOtp(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.verifyOtp(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun resendOtp(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.resendOtp(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun checkUsername(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.createUsername(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun editProfile(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.editProfile(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun login(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.login(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun fileUpload(
        map: HashMap<String, RequestBody>,
        images: ArrayList<MultipartBody.Part>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.fileUpload(map, images))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun postFileUpload(
        map: HashMap<String, RequestBody>,
        images: MultipartBody.Part,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.postFileUpload(map, images))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun interest(activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getInterests())
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun question(activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getQuestion())
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getProfile(activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getProfile())
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun changePassword(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.changePassword(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun updateNotification(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.updateNotification(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun logOut(activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.logOut())
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun deleteAccount(
        id: String,
        jsonObject: HashMap<String, Any>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.deleteAccount(id, jsonObject))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun sendDeleteAccount(
        jsonObject: HashMap<String, Any>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.sendDeleteMail(jsonObject))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun reportFeedBack(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.reportFeedback(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun contactUs(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.contactUs(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun termCondition(activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getTermsCondition())
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun privacyPolicy(activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.privacyPolicy())
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun shopBanner(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.shopBanner(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getShopCategory(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getShopCategory(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getProduct(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getProductList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun addWishList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.addWishList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun changeEmailRequest(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.changeEmailRequest(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getProductDetail(id: String, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getProductDetail(id))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getWishList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getWishList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun eventCategoryList(activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.eventCategoryList())
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun eventList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.eventList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun eventList2(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.eventList2(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun eventBookmark(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.eventBookmark(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun eventBookmarkList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.eventBookmarkList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun viewEvent(id: String, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.viewEvent(id))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun eventBuyTicket(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.eventBuyTicket(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun addPost(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.addPost(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun dashboardList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.dashboardList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun dashboardData(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.dashboardData(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getPostList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getPostList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun postLikeUnlike(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.postLikeUnlike(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun postBookmark(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.postBookmark(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getPostLikes(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getPostLikes(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }


    suspend fun followUnfollow(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.followUnfollow(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getCommunityCategory(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getCommunityCategory(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getCommunityForumList(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getCommunityForumList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun communityLikeUnlike(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.communityLikeUnlike(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getCommunityLikeList(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getCommunityLikeList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun addCommunity(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.addCommunity(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getPrayerCategoryList(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getPrayerCategoryList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getPrayerList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getPrayerList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun prayerLikeUnlike(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.prayerLikeUnlike(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getPrayerLikeList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getPrayerLikeList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getPraiseLikeList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getPraiseLikeList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getTestimonyPraiseList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getTestimonyPraiseList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun addPrayer(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.addPrayer(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getTestimonyCategoryList(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getTestimonyCategoryList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getTestimonyList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getTestimonyList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun testimonyLikeUnlike(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.testimonyLikeUnlike(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getTestimonyLikeList(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getTestimonyLikeList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun addTestimony(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.addTestimony(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun prayerView(id: String, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.prayerView(id))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun testimonyView(id: String, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.testimonyView(id))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun communityView(id: String, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.communityView(id))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun prayerPriseUnpraise(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.prayerPriseUnpraise(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun testimonyPriseUnpraise(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.testimonyPriseUnpraise(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun deleteTestimony(id: String, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.deleteTestimony(id))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun deletePrayer(id: String, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.deletePrayer(id))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun prayerReport(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.prayerReport(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun testimonyReport(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.testimonyReport(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun userReport(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.userReport(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun communityReport(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.communityReport(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun deleteCommunity(id: String, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.deleteCommunity(id))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun userBlock(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.userBlock(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getBlockList(activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getBlockList())
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun postReport(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.postReport(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun suggestionList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.suggestionList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }


    suspend fun postDelete(id: String, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.postDelete(id))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getPostBookmark(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getPostBookmark(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun userFollowFollowingList(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.userFollowFollowingList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }


    suspend fun otherUserProfile(id: String, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.otherUserProfile(id))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun addPostComment(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.addPostComment(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun postCommentList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.postCommentList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun postCommentLikeUnlike(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.postCommentLikeUnlike(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun postCommentEdit(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.postCommentEdit(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun postCommentDelete(
        id: String,
        activity: Activity,
        notificationId: String
    ): Resource<Any> {
        return try {
            val body = mapOf(
                "notification_id" to notificationId
            )
            responseHandler.handleResponse(apiService.postCommentDelete(id, body))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun postCommentReport(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.postCommentReport(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun addPrayerComment(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.addPrayerComment(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun prayerCommentList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.prayerCommentList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun prayerCommentLikeUnlike(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.prayerCommentLikeUnlike(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun prayerCommentEdit(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.prayerCommentEdit(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun prayerCommentDelete(
        id: String,
        activity: Activity,
        notificationId: String
    ): Resource<Any> {
        return try {
            val body = mapOf(
                "notification_id" to notificationId
            )
            responseHandler.handleResponse(apiService.prayerCommentDelete(id, body))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun prayerCommentReport(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.prayerCommentReport(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun addTestimonyComment(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.addTestimonyComment(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun testimonyCommentList(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.testimonyCommentList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun testimonyCommentLikeUnlike(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.testimonyCommentLikeUnlike(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun testimonyCommentEdit(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.testimonyCommentEdit(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun testimonyCommentDelete(
        id: String,
        activity: Activity,
        notificationId: String
    ): Resource<Any> {
        return try {
            val body = mapOf(
                "notification_id" to notificationId
            )
            responseHandler.handleResponse(apiService.testimonyCommentDelete(id, body))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun testimonyCommentReport(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.testimonyCommentReport(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun addCommunityComment(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.addCommunityComment(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun communityCommentList(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.communityCommentList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun communityCommentLikeUnlike(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.communityCommentLikeUnlike(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun communityCommentEdit(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.communityCommentEdit(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun communityCommentDelete(
        id: String,
        activity: Activity,
        notificationId: String
    ): Resource<Any> {
        return try {
            val body = mapOf(
                "notification_id" to notificationId
            )
            responseHandler.handleResponse(apiService.communityCommentDelete(id, body))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun communityCommentReport(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.communityCommentReport(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getGroupCategoryList(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.groupCategoryList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getGroupList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getGroupList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getGroupView(id: String, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getGroupView(id))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }


    suspend fun bibleQuestCategoryList(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.bibleQuestCategoryList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getBibleQuestList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getBibleQuestList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getBibleView(id: String, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getBibleView(id))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun markAsComplete(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.markAsComplete(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun markAsRestart(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.markAsRestart(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getNotification(activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getNotifications())
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getHomeSearch(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getHomeSearch(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getRecentSearch(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getRecentSearch(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun deleteRecentSearch(
        map: HashMap<String, String>,
        activity: Activity
    ): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.deleteRecentSearch(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun reportPrompt(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.reportPrompt(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getMuteStatus(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getMuteStatus(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getPostView(id: String, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.viewPostView(id))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun forgotPassword(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.forgotPassword(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun newPassword(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.newPassword(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun verifyPassword(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.verifyPassword(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun addRating(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.addRating(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun productMayLike(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.productMayLike(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getCounts(activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getCounts())
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getMyReferralCode(activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getMyReferralCode())
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun getHashTagList(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.getHashTagList(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun checkReferralCode(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.checkReferralCode(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun updateSubscription(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.updateSubscription(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

    suspend fun removeSuggestion(map: HashMap<String, String>, activity: Activity): Resource<Any> {
        return try {
            responseHandler.handleResponse(apiService.removeSuggestion(map))
        } catch (e: Exception) {
            responseHandler.handleException(e, activity)
        }
    }

}