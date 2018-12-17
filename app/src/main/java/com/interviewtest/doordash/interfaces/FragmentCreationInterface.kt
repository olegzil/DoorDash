package com.interviewtest.doordash.interfaces

import android.support.v4.app.Fragment
import com.interviewtest.doordash.datamodel.FragmentCreationDescriptor
import io.reactivex.subjects.PublishSubject

interface FragmentCreationInterface {
    fun callbackSubject(): PublishSubject<FragmentCreationDescriptor>
    fun fragment(): Fragment
    fun getFragmentId(): String
}