package com.interviewtest.doordash.datamodel

import kotlinx.serialization.Serializable

@Serializable
class FragmentSaveState(val fragmentId:String, val baseURL:String, val longitude:Float, val latitude:Float, val nextPage:Int)
