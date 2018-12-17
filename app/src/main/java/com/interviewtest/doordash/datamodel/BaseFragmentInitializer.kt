package com.interviewtest.doordash.datamodel

import com.interviewtest.doordash.recyclerviewadapters.GenericImageAdapter
import com.interviewtest.doordash.server.RetrofitNetworkService
import io.reactivex.subjects.PublishSubject
import java.io.Serializable

data class BaseFragmentInitializer(
    val genericAdapter: GenericImageAdapter,
    val fragmentSubject: PublishSubject<FragmentCreationDescriptor>,
    val fragmentTag: String,
    val serverCall: RetrofitNetworkService,
    val fragmentID: String,
    val location:Pair<Float,Float>,
    val initialData: String? = null
) : Serializable
