package com.live.azurah.model

data class UserModel(
    var first_name:String? = null,
    var last_name:String? = null,
    var profile_image:String? = null,
    var location:String? = null,
    var latitude:String? = null,
    var longitude:String? = null,
    var dob:String? = null,
    var username:String? = null,
    var christian_journey:String? = null,
    var interest_ids:String? = null,
    var is_newsletter:String? = null,
    var country:String? = null
)