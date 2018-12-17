package com.interviewtest.doordash.datamodel

import kotlinx.serialization.SerialId

data class MainAppSaveState(@SerialId(1) val fragmentID:String,
                            @SerialId(2)val payload:String)
