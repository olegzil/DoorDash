package com.interviewtest.doordash

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.interviewtest.doordash.cachemanager.CacheManager
import com.interviewtest.doordash.datamodel.FragmentCreationDescriptor
import com.interviewtest.doordash.datamodel.MainActivityData
import com.interviewtest.doordash.datamodelloader.MainAppStateLoader
import com.interviewtest.doordash.fragments.RestaurantListFragment
import com.interviewtest.doordash.interfaces.FragmentCreationInterface
import com.interviewtest.doordash.utilities.printLog
import io.reactivex.disposables.Disposables
import io.reactivex.observers.DisposableObserver
import io.reactivex.subjects.PublishSubject

private var fragmentCallback = PublishSubject.create<FragmentCreationDescriptor>()

enum class ViewIdentifier {
    RESTAURANT_LIST_FRAGMENT
}

class MainActivity : AppCompatActivity() {

    private var disposable = Disposables.disposed()
    override fun onCreate(savedInstanceState: Bundle?) {
        if (savedInstanceState == null){
            Bundle().run {
                putSerializable(MAIN_APP_ACTIVITY_KEY, MainActivityData(fragmentCallback))
                super.onCreate(this)
            }
        } else {
            super.onCreate(savedInstanceState)
        }
        setContentView(R.layout.activitymain)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.colorRed)))
        CacheManager.initialize(applicationContext, "masterDB")
        displayNextFragment(RestaurantListFragment.newInstance() as Fragment, RestaurantListFragment.fragmentID)
    }

    private fun displayNextFragment(fragment: Fragment, fragmentID: String) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment, fragmentID)
            .addToBackStack(null)
            .commit()
    }

    override fun onStart() {
        super.onStart()
        disposable = fragmentCallback.subscribeWith(object : DisposableObserver<FragmentCreationDescriptor>() {
            override fun onComplete() {}
            override fun onNext(fragmentDescriptor: FragmentCreationDescriptor) {
                displayNextFragment(fragmentDescriptor.fragment, fragmentDescriptor.fragmentTag)
            }

            override fun onError(e: Throwable) {
                printLog("MainActivity: ${e.localizedMessage}")
            }
        })
    }

    override fun onStop() {
        super.onStop()
        disposable.dispose()
    }
    companion object {
        const val MAIN_APP_ACTIVITY_KEY="'bf0fc40e-891d-431a-b59d-735f0b72420d"
        fun getNotifier() = fragmentCallback
    }
}
