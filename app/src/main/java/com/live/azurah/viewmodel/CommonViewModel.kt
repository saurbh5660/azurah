package com.live.azurah.viewmodel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.live.azurah.rapository.CommonRepository
import com.live.azurah.retrofit.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

@HiltViewModel
class CommonViewModel @Inject constructor(private val commonRepository: CommonRepository) :
    ViewModel() {

    fun signUp(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.signUp(map, activity)
            emit(response)
        }
    }

    fun verifyOtp(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.verifyOtp(map, activity)
            emit(response)
        }
    }

    fun resendOtp(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.resendOtp(map, activity)
            emit(response)
        }
    }

    fun checkUserName(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.checkUsername(map, activity)
            emit(response)
        }
    }

    fun editProfile(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.editProfile(map, activity)
            emit(response)
        }
    }

    fun login(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.login(map, activity)
            emit(response)
        }
    }

    fun fileUpload(
        map: HashMap<String, RequestBody>,
        images: ArrayList<MultipartBody.Part>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.fileUpload(map, images, activity)
            emit(response)
        }
    }

    fun postFileUpload(
        map: HashMap<String, RequestBody>,
        images: MultipartBody.Part,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.postFileUpload(map, images, activity)
            emit(response)
        }
    }

    fun getInterest(activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.interest(activity)
            emit(response)
        }
    }

    fun getQuestion(activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.question(activity)
            emit(response)
        }
    }

    fun getProfile(activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getProfile(activity)
            emit(response)
        }
    }

    fun changePassword(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.changePassword(map, activity)
            emit(response)
        }
    }

    fun updateNotification(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.updateNotification(map, activity)
            emit(response)
        }
    }

    fun logOut(activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.logOut(activity)
            emit(response)
        }
    }

    fun deleteAccount(
        id: String,
        jsonObject: HashMap<String, Any>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.deleteAccount(id, jsonObject, activity)
            emit(response)
        }
    }

    fun sendDeleteAccount(
        jsonObject: HashMap<String, Any>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.sendDeleteAccount(jsonObject, activity)
            emit(response)
        }
    }

    fun reportFeedBack(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.reportFeedBack(map, activity)
            emit(response)
        }
    }

    fun contactUs(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.contactUs(map, activity)
            emit(response)
        }
    }

    fun termsCondition(activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.termCondition(activity)
            emit(response)
        }
    }

    fun privacyPolicy(activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.privacyPolicy(activity)
            emit(response)
        }
    }

    fun getBanner(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.shopBanner(map, activity)
            emit(response)
        }
    }

    fun getShopCategory(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getShopCategory(map, activity)
            emit(response)
        }
    }

    fun getProduct(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getProduct(map, activity)
            emit(response)
        }
    }

    fun addWishList(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.addWishList(map, activity)
            emit(response)
        }
    }

    fun changeEmailRequest(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.changeEmailRequest(map, activity)
            emit(response)
        }
    }

    fun getProductDetail(id: String, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getProductDetail(id, activity)
            emit(response)
        }
    }

    fun getWishList(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getWishList(map, activity)
            emit(response)
        }
    }

    fun eventCategoryList(activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.eventCategoryList(activity)
            emit(response)
        }
    }

    fun eventList(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.eventList(map, activity)
            emit(response)
        }
    }

    fun eventList2(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.eventList2(map, activity)
            emit(response)
        }
    }

    fun eventBookmark(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.eventBookmark(map, activity)
            emit(response)
        }
    }

    fun eventBookmarkList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.eventBookmarkList(map, activity)
            emit(response)
        }
    }

    fun viewEvent(id: String, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.viewEvent(id, activity)
            emit(response)
        }
    }

    fun eventBuyTicket(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.eventBuyTicket(map, activity)
            emit(response)
        }
    }

    fun addPost(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.addPost(map, activity)
            emit(response)
        }
    }

    fun dashboardList(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.dashboardList(map, activity)
            emit(response)
        }
    }

    fun dashboardData(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.dashboardData(map, activity)
            emit(response)
        }
    }

    fun getPostList(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getPostList(map, activity)
            emit(response)
        }
    }

    fun postLikeUnlike(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.postLikeUnlike(map, activity)
            emit(response)
        }
    }

    fun postBookmark(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.postBookmark(map, activity)
            emit(response)
        }
    }

    fun getPostLikes(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getPostLikes(map, activity)
            emit(response)
        }
    }


    fun followUnfollow(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.followUnfollow(map, activity)
            emit(response)
        }
    }

    fun getCommunityCategory(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getCommunityCategory(map, activity)
            emit(response)
        }
    }

    fun getCommunityForumList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getCommunityForumList(map, activity)
            emit(response)
        }
    }

    fun communityLikeUnlike(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.communityLikeUnlike(map, activity)
            emit(response)
        }
    }

    fun getCommunityLikeList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getCommunityLikeList(map, activity)
            emit(response)
        }
    }

    fun addCommunity(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.addCommunity(map, activity)
            emit(response)
        }
    }

    fun getPrayerCategoryList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getPrayerCategoryList(map, activity)
            emit(response)
        }
    }

    fun getPrayerList(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getPrayerList(map, activity)
            emit(response)
        }
    }

    fun prayerLikeUnlike(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.prayerLikeUnlike(map, activity)
            emit(response)
        }
    }

    fun getPrayerLikeList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getPrayerLikeList(map, activity)
            emit(response)
        }
    }

    fun getPraiseLikeList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getPraiseLikeList(map, activity)
            emit(response)
        }
    }

    fun getTestimonyPraiseList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getTestimonyPraiseList(map, activity)
            emit(response)
        }
    }

    fun addPrayer(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.addPrayer(map, activity)
            emit(response)
        }
    }

    fun getTestimonyCategoryList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getTestimonyCategoryList(map, activity)
            emit(response)
        }
    }

    fun getTestimonyList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getTestimonyList(map, activity)
            emit(response)
        }
    }

    fun testimonyLikeUnlike(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.testimonyLikeUnlike(map, activity)
            emit(response)
        }
    }

    fun getTestimonyLikeList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getTestimonyLikeList(map, activity)
            emit(response)
        }
    }

    fun addTestimony(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.addTestimony(map, activity)
            emit(response)
        }
    }

    fun prayerView(id: String, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.prayerView(id, activity)
            emit(response)
        }
    }

    fun testimonyView(id: String, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.testimonyView(id, activity)
            emit(response)
        }
    }

    fun communityView(id: String, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.communityView(id, activity)
            emit(response)
        }
    }

    fun prayerPriseUnpraise(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.prayerPriseUnpraise(map, activity)
            emit(response)
        }
    }

    fun testimonyPriseUnpraise(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.testimonyPriseUnpraise(map, activity)
            emit(response)
        }
    }

    fun deleteTestimony(id: String, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.deleteTestimony(id, activity)
            emit(response)
        }
    }

    fun deletePrayer(id: String, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.deletePrayer(id, activity)
            emit(response)
        }
    }

    fun prayerReport(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.prayerReport(map, activity)
            emit(response)
        }
    }

    fun testimonyReport(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.testimonyReport(map, activity)
            emit(response)
        }
    }

    fun userReport(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.userReport(map, activity)
            emit(response)
        }
    }

    fun communityReport(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.communityReport(map, activity)
            emit(response)
        }
    }

    fun deleteCommunity(id: String, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.deleteCommunity(id, activity)
            emit(response)
        }
    }

    fun userBlock(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.userBlock(map, activity)
            emit(response)
        }
    }

    fun getBlockList(activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getBlockList(activity)
            emit(response)
        }
    }

    fun postReport(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.postReport(map, activity)
            emit(response)
        }
    }

    fun suggestionList(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.suggestionList(map, activity)
            emit(response)
        }
    }

    fun postDelete(id: String, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.postDelete(id, activity)
            emit(response)
        }
    }

    fun getPostBookmark(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getPostBookmark(map, activity)
            emit(response)
        }
    }

    fun userFollowFollowingList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.userFollowFollowingList(map, activity)
            emit(response)
        }
    }

    fun otherUserProfile(id: String, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.otherUserProfile(id, activity)
            emit(response)
        }
    }

    fun addPostComment(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.addPostComment(map, activity)
            emit(response)
        }
    }

    fun postCommentList(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.postCommentList(map, activity)
            emit(response)
        }
    }

    fun postCommentLikeUnlike(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.postCommentLikeUnlike(map, activity)
            emit(response)
        }
    }

    fun postCommentEdit(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.postCommentEdit(map, activity)
            emit(response)
        }
    }

    fun postCommentDelete(
        id: String,
        activity: Activity,
        notificationId: String
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.postCommentDelete(id, activity, notificationId)
            emit(response)
        }
    }

    fun postCommentReport(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.postCommentReport(map, activity)
            emit(response)
        }
    }

    fun addPrayerComment(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.addPrayerComment(map, activity)
            emit(response)
        }
    }

    fun prayerCommentList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.prayerCommentList(map, activity)
            emit(response)
        }
    }

    fun prayerCommentLikeUnlike(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.prayerCommentLikeUnlike(map, activity)
            emit(response)
        }
    }

    fun prayerCommentEdit(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.prayerCommentEdit(map, activity)
            emit(response)
        }
    }

    fun prayerCommentDelete(
        id: String,
        activity: Activity,
        notificationId: String
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.prayerCommentDelete(id, activity, notificationId)
            emit(response)
        }
    }

    fun prayerCommentReport(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.prayerCommentReport(map, activity)
            emit(response)
        }
    }

    fun addTestimonyComment(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.addTestimonyComment(map, activity)
            emit(response)
        }
    }

    fun testimonyCommentList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.testimonyCommentList(map, activity)
            emit(response)
        }
    }

    fun testimonyCommentLikeUnlike(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.testimonyCommentLikeUnlike(map, activity)
            emit(response)
        }
    }

    fun testimonyCommentEdit(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.testimonyCommentEdit(map, activity)
            emit(response)
        }
    }

    fun testimonyCommentDelete(
        id: String,
        activity: Activity,
        notificationId: String
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.testimonyCommentDelete(id, activity, notificationId)
            emit(response)
        }
    }

    fun testimonyCommentReport(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.testimonyCommentReport(map, activity)
            emit(response)
        }
    }


    fun addCommunityComment(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.addCommunityComment(map, activity)
            emit(response)
        }
    }

    fun communityCommentList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.communityCommentList(map, activity)
            emit(response)
        }
    }

    fun communityCommentLikeUnlike(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.communityCommentLikeUnlike(map, activity)
            emit(response)
        }
    }

    fun communityCommentEdit(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.communityCommentEdit(map, activity)
            emit(response)
        }
    }

    fun communityCommentDelete(
        id: String,
        activity: Activity,
        notificationId: String
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.communityCommentDelete(id, activity, notificationId)
            emit(response)
        }
    }

    fun communityCommentReport(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.communityCommentReport(map, activity)
            emit(response)
        }
    }

    fun getGroupCategoryList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getGroupCategoryList(map, activity)
            emit(response)
        }
    }

    fun getGroupList(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getGroupList(map, activity)
            emit(response)
        }
    }

    fun getGroupView(id: String, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getGroupView(id, activity)
            emit(response)
        }
    }


    fun bibleQuestCategoryList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.bibleQuestCategoryList(map, activity)
            emit(response)
        }
    }

    fun getBibleQuestList(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getBibleQuestList(map, activity)
            emit(response)
        }
    }

    fun getBibleView(id: String, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getBibleView(id, activity)
            emit(response)
        }
    }

    fun markAsComplete(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.markAsComplete(map, activity)
            emit(response)
        }
    }

    fun markAsRestart(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.markAsRestart(map, activity)
            emit(response)
        }
    }

    fun getNotification(activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getNotification(activity)
            emit(response)
        }
    }

    fun getHomeSearch(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getHomeSearch(map, activity)
            emit(response)
        }
    }

    fun getRecentSearch(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getRecentSearch(map, activity)
            emit(response)
        }
    }

    fun deleteRecentSearch(
        map: HashMap<String, String>,
        activity: Activity
    ): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.deleteRecentSearch(map, activity)
            emit(response)
        }
    }

    fun reportPrompt(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.reportPrompt(map, activity)
            emit(response)
        }
    }

    fun getMuteStatus(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getMuteStatus(map, activity)
            emit(response)
        }
    }

    fun getPostView(id: String, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getPostView(id, activity)
            emit(response)
        }
    }

    fun forgotPassword(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.forgotPassword(map, activity)
            emit(response)
        }
    }

    fun newPassword(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.newPassword(map, activity)
            emit(response)
        }
    }

    fun verifyPassword(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.verifyPassword(map, activity)
            emit(response)
        }
    }

    fun addRating(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.addRating(map, activity)
            emit(response)
        }
    }

    fun productMayLike(map: HashMap<String, String>, activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.productMayLike(map, activity)
            emit(response)
        }
    }

    fun getCounts(activity: Activity): LiveData<Resource<Any>> {
        return liveData(Dispatchers.IO) {
            emit(Resource.loading(null))
            val response = commonRepository.getCounts(activity)
            emit(response)
        }
    }

}