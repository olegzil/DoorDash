package com.interviewtest.doordash.fragments

import android.os.Bundle
import com.interviewtest.doordash.datamodel.FragmentSaveState
import com.interviewtest.doordash.server.NetworkServiceInitializer
import com.interviewtest.doordash.server.RetrofitNetworkService
import kotlinx.serialization.json.JSON

class RestaurantDetailFragment : BaseFragment() {
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

    companion object {
        const val fragmentID = "5df86723-6e72-4ea1-9a68-c0c119723521"
        fun newInstance(): RestaurantDetailFragment {
            val bundle = Bundle().apply {
                val state =
                    FragmentSaveState(
                        RestaurantListFragment.fragmentID,
                        "https://api.doordash.com/v2/restaurant/",
                        37.422740f,
                        -122.139956f,
                        2
                    )
                putSerializable(
                    BaseFragment.BASE_FRAGMENT_INITIAL_DATA_KEY,
                    JSON.stringify(FragmentSaveState.serializer(), state)
                )
            }

            val fragment = RestaurantDetailFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}