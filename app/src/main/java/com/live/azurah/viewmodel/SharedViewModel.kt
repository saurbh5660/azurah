package com.live.azurah.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.live.azurah.model.BibleQuestViewModel
import com.live.azurah.model.BlockResposne
import com.live.azurah.model.CountResponse
import com.live.azurah.model.InterestResponse
import com.live.azurah.model.PostResponse
import com.live.azurah.model.ProfileResponse

class SharedViewModel: ViewModel() {
    private val _walkThrough = MutableLiveData<Int>()
    private val _profile = MutableLiveData<ProfileResponse.Body?>()
    private val _interest = MutableLiveData<ArrayList<InterestResponse.Body>>()
    private val _categoryId = MutableLiveData<Pair<String,String>>()
    private val _isPrivate = MutableLiveData<Pair<String,String>>()
    private val _postData = MutableLiveData<PostResponse.Body?>()
    private val _postSearchData = MutableLiveData<ArrayList<PostResponse.Body.Data>>()
    private val _userSearchData = MutableLiveData<ArrayList<BlockResposne.Body.Data>>()
    private val _search = MutableLiveData<String>()
    private val _recyclerScroll = MutableLiveData<String>()
    private val _searchPair = MutableLiveData<Triple<String,Int,Int>>()
    private val _bibleQuestDetail = MutableLiveData<BibleQuestViewModel.Body>()
    private val _count = MutableLiveData<CountResponse.Body?>()

    val walkThrough: LiveData<Int> get() = _walkThrough
    val profile: LiveData<ProfileResponse.Body?> get() = _profile
    val interest: LiveData<ArrayList<InterestResponse.Body>> get() = _interest
    val getCategoryId: LiveData<Pair<String,String>> get() = _categoryId
    val getPrivate: LiveData<Pair<String,String>> get() = _isPrivate
    val getPostData: LiveData<PostResponse.Body?> get() = _postData
    val getSearchPostData: LiveData<ArrayList<PostResponse.Body.Data>> get() = _postSearchData
    val getSearchUserData: LiveData<ArrayList<BlockResposne.Body.Data>> get() = _userSearchData
    val search: LiveData<String> get() = _search
    val recyclerScroll: LiveData<String> get() = _recyclerScroll
    val searchPair: LiveData<Triple<String,Int,Int>> get() = _searchPair
    val bibleQuestDetail: LiveData<BibleQuestViewModel.Body> get() = _bibleQuestDetail
    val count: LiveData<CountResponse.Body?> get() = _count

    fun getWalkthrough(type:Int) {
        _walkThrough.value = type
    }
    fun setProfileData(data:ProfileResponse.Body?) {
        _profile.value = data
    }

    fun setInterestData(data:ArrayList<InterestResponse.Body>) {
        _interest.value = data
    }

    fun setCategoryId(pair: Pair<String,String>) {
        _categoryId.value = pair
    }

    fun setPrivate(pair: Pair<String,String>) {
        _isPrivate.value = pair
    }

    fun setPostData(data: PostResponse.Body?) {
        _postData.value = data
    }

    fun setPostSearchData(data: ArrayList<PostResponse.Body.Data>) {
        _postSearchData.value = data
    }

    fun setUserSearchData(data: ArrayList<BlockResposne.Body.Data>) {
        _userSearchData.value = data
    }

    fun setSearchChat(message :String) {
        _search.value = message
    }
    fun setChallengeData(data: BibleQuestViewModel.Body) {
        _bibleQuestDetail.value = data
    }

    fun setRecyclerScroll() {
        _recyclerScroll.value = ""
    }

    fun setSearchPair(pair: Triple<String,Int,Int>) {
        _searchPair.value = pair
    }
    fun setCount(data: CountResponse.Body?) {
        _count.value = data
    }

}