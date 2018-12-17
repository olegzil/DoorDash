package com.interviewtest.doordash.fragments

import android.os.Bundle
import com.interviewtest.doordash.MainActivity.Companion.getNotifier
import com.interviewtest.doordash.datamodel.BaseFragmentInitializer
import com.interviewtest.doordash.datamodel.DisplayedPageState
import com.interviewtest.doordash.interfaces.FragmentCreationInterface
import com.interviewtest.doordash.recyclerviewadapters.GenericImageAdapter
import com.interviewtest.doordash.server.NetworkServiceInitializer
import com.interviewtest.doordash.server.RetrofitNetworkService
import com.interviewtest.doordash.utilities.populateRestaurantData
import io.reactivex.Single

class RestaurantListFragment : BaseFragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            super.onCreate(it)
        } ?: run {
            setBaseFragmentState(getBaseFragmentInitializer())
            super.onCreate(savedInstanceState)
        }
    }

    override fun fetchServerData(): Single<DisplayedPageState>? {
        return parentState.serverCall.getApi()?.run {
            val result: Single<String>? =
                fetchRestaurantList(parentState.location.first, parentState.location.second, GenericImageAdapter.maxAdapterSize)
            result?.run {
                flatMap { items ->
                        populateRestaurantData(items)?.let {itemList ->
                            Single.just(DisplayedPageState(0, 0, itemList))
                        } ?: let {
                            Single.error<DisplayedPageState>(Throwable("Empty Payload"))
                        }
                }
            }
        }
    }

    override fun getFragmentId() = RestaurantListFragment.fragmentID

    companion object {
        const val fragmentID = "5a5e870f-01db-4e44-977e-0212501177a5"
        @JvmStatic
        fun getBaseFragmentInitializer(payLoad: String? = null) =
            BaseFragmentInitializer(
                GenericImageAdapter(),
                getNotifier(),
                "RestaurantList",
                RetrofitNetworkService(NetworkServiceInitializer("https://api.doordash.com/v2/restaurant/")),
                RestaurantListFragment.fragmentID,
                Pair<Float,Float>(37.422740f, -122.139956f),
                payLoad
            )

        @JvmStatic
        fun newInstance(initialData: String?=null): FragmentCreationInterface {
            Bundle().apply {
                putSerializable(BaseFragment.BASE_FRAGMENT_INITIAL_DATA_KEY, getBaseFragmentInitializer(initialData))
            }
            return RestaurantListFragment()
        }
    }
}