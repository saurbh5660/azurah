package com.live.azurah.model

data class CategoryModel(val textColor: Int?=null, val backgroundColor:Int? = null, val name:String,
                         var isSelected : Boolean = false,var id : String ? = null)
