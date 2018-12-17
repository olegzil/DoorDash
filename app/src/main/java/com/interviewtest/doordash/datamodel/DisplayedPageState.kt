package com.interviewtest.doordash.datamodel

import com.interviewtest.doordash.itemdetail.ItemDetail
import kotlinx.serialization.Serializable

@Serializable
data class DisplayedPageState(val nextPage:Int,
                              val firstVisible:Int,
                              val items:List<ItemDetail>){}
