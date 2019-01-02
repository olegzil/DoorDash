package com.interviewtest.doordash.fragments

import android.os.Bundle
import com.interviewtest.doordash.datamodel.DisplayedPageState
import com.interviewtest.doordash.datamodel.FragmentSaveState
import com.interviewtest.doordash.recyclerviewadapters.GenericImageAdapter
import com.interviewtest.doordash.server.NetworkServiceInitializer
import com.interviewtest.doordash.server.RetrofitNetworkService
import com.interviewtest.doordash.utilities.populateRestaurantData
import io.reactivex.Single
import kotlinx.serialization.json.JSON

class RestaurantListFragment : BaseFragment() {
    private lateinit var serverCall: RetrofitNetworkService
    private lateinit var initialLocation: Pair<Float, Float>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.let { bundle ->
            val jsonString = bundle.getString(BaseFragment.BASE_FRAGMENT_INITIAL_DATA_KEY)
            jsonString?.let {
                val savedState = JSON.parse(FragmentSaveState.serializer(), jsonString)
                serverCall =
                        RetrofitNetworkService(NetworkServiceInitializer(savedState.baseURL))
                initialLocation = Pair(savedState.latitude, savedState.longitude)

            } ?: run {
                serverCall =
                        RetrofitNetworkService(NetworkServiceInitializer("https://api.doordash.com/v2/restaurant/"))
                initialLocation = Pair(37.422740f, -122.139956f)
            }
        } ?: run {
            serverCall =
                    RetrofitNetworkService(NetworkServiceInitializer("https://api.doordash.com/v2/restaurant/"))
            initialLocation = Pair(37.422740f, -122.139956f)
        }
    }

    override fun fetchServerData(): Single<DisplayedPageState>? {
        return serverCall.getApi()?.run {
            val result: Single<String>? =
                fetchRestaurantList(initialLocation.first, initialLocation.second, GenericImageAdapter.maxAdapterSize)
            result?.run {
                flatMap { items ->
                    populateRestaurantData(items)?.let { itemList ->
                        Single.just(DisplayedPageState(0, 0, itemList))
                    } ?: let {
                        Single.error<DisplayedPageState>(Throwable("Empty Payload"))
                    }
                }
            }
        }
    }

    companion object {
        const val fragmentID = "5a5e870f-01db-4e44-977e-0212501177a5"
        @JvmStatic
        fun newInstance(): RestaurantListFragment {
            Bundle().apply {
                val state =
                    FragmentSaveState(fragmentID, "https://api.doordash.com/v2/restaurant/", 37.422740f, -122.139956f, 2)
                putSerializable(
                    BaseFragment.BASE_FRAGMENT_INITIAL_DATA_KEY,
                    JSON.stringify(FragmentSaveState.serializer(), state)
                )
            }
            return RestaurantListFragment()
        }
    }
}