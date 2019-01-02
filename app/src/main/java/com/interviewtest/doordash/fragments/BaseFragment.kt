package com.interviewtest.doordash.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.interviewtest.doordash.R
import com.interviewtest.doordash.datamodel.DisplayedPageState
import com.interviewtest.doordash.datamodel.FragmentCreationDescriptor
import com.interviewtest.doordash.datamodel.FragmentSaveState
import com.interviewtest.doordash.fragmentCallback
import com.interviewtest.doordash.recyclerviewadapters.GenericImageAdapter
import com.interviewtest.doordash.server.NetworkServiceInitializer
import com.interviewtest.doordash.server.RetrofitNetworkService
import com.interviewtest.doordash.touchhandlers.RecyclerItemClickListener
import com.interviewtest.doordash.touchhandlers.RecyclerViewScrollHandler
import com.interviewtest.doordash.utilities.printLog
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers
import kotlinx.serialization.json.JSON


open class BaseFragment : Fragment() {
    private lateinit var mRecyclerView: RecyclerView
    private val disposables = CompositeDisposable()
    private var nextPage = 2
    private lateinit var savedState: FragmentSaveState
    lateinit var adapter: GenericImageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        /*
        * If context is null, the show is over. Otherwise, we test savedInstanceState. If that value is not null
        * we use the previously saved state. If that value is null, then the run clause is executed and we try
        * to use the global state that was saved by the caller. This bit of complexity allows the class inheriting
        * from BaseFragment to be used in the xml file, i.e. <fragment android:name="com.interviewtest.doordash.fragments.RestaurantListFragment"/>
        * Without the global state BaseFragment code will be constructed without initial data.
        * */
        super.onCreate(savedInstanceState)
        savedInstanceState?.let { bundle ->
            val savedState =
                bundle.getString(BaseFragment.BASE_FRAGMENT_INITIAL_DATA_KEY)
            savedState?.let {
                this.savedState = JSON.parse(FragmentSaveState.serializer(), savedState)
                nextPage = this.savedState.nextPage
            } ?: let {
                this.savedState = FragmentSaveState(
                    PhotoFragment.fragmentID,
                    "https://api.doordash.com/v2/restaurant/",
                    37.422740f,
                    -122.139956f,
                    nextPage
                )
            }
        } ?: let {
            savedState = FragmentSaveState(
                PhotoFragment.fragmentID,
                "https://api.doordash.com/v2/restaurant/",
                37.422740f,
                -122.139956f,
                nextPage
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // If there is no saved state, initiate a server request to populate the recycler view adapter
        val view = inflater.inflate(R.layout.fragment_recycler, container, false)
        initializeFragment(view)

        super.onCreateView(inflater, container, savedInstanceState)
        return view
    }

    override fun onResume() {
        super.onResume()
        populateAdapterFromSavedState()?.run {
            val disposable = observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribeWith(object : DisposableSingleObserver<DisplayedPageState>() {
                    override fun onSuccess(itemDetails: DisplayedPageState) {
                        nextPage = GenericImageAdapter.maxAdapterSize / GenericImageAdapter.maxPageSize
                        adapter.update(itemDetails.items)
                        mRecyclerView.layoutManager?.scrollToPosition(itemDetails.firstVisible)
                        adapter.update()
                        printLog("initial count = ${adapter.itemCount}")
                    }

                    override fun onError(e: Throwable) {
                        printLog(e.localizedMessage)
                        // Do Nothing. Should never reach this logic
                    }
                })
            disposable.run {
                disposables.add(this)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        disposables.clear()
    }

    override fun onDetach() {
        super.onDetach()
        disposables.clear()
    }

    protected open fun fetchServerData(): Single<DisplayedPageState>? {
        return Single.never()
    }

    private fun populateAdapterFromSavedState(): Single<DisplayedPageState>? = fetchServerDataAsSingle()
    private fun fetchServerDataAsSingle(): Single<DisplayedPageState>? {
        return fetchServerData()
    }

    protected fun displayDetailPhoto(url: String) {
        val photoView = PhotoFragment.newInstance(url)
        fragmentCallback.onNext(FragmentCreationDescriptor(photoView, PhotoFragment.fragmentID))
    }

    private fun initializeFragment(view: View) {
        view.let { recyclerView ->
            adapter = GenericImageAdapter()
            mRecyclerView = recyclerView.findViewById(R.id.recycler_view)
            val mLayoutManager = LinearLayoutManager(context)
            mRecyclerView.layoutManager = mLayoutManager
            mRecyclerView.itemAnimator = DefaultItemAnimator()
            mRecyclerView.adapter = adapter
            val divider = DividerItemDecoration(recyclerView.context, DividerItemDecoration.VERTICAL)
            divider.setDrawable(ContextCompat.getDrawable(context!!, R.drawable.custom_devider)!!)
            mRecyclerView.addItemDecoration(divider)
            context?.let { context ->
                mRecyclerView.addOnScrollListener(
                    RecyclerViewScrollHandler(
                        context,
                        adapter,
                        RetrofitNetworkService(NetworkServiceInitializer("https://api.doordash.com/v2/restaurant/")),
                        Pair(savedState.latitude, savedState.latitude),
                        nextPage
                    )
                )

                mRecyclerView.addOnItemTouchListener(
                    RecyclerItemClickListener(
                        context,
                        mRecyclerView,
                        object : RecyclerItemClickListener.OnItemClickListener {
                            override fun onItemClick(view: View, position: Int) {
                                displayDetailPhoto(adapter.getItemDetailByPosition(position).restaurantThumbnailUrl)
                            }

                            override fun onItemLongClick(view: View?, position: Int) {
                                Toast.makeText(
                                    context,
                                    "got long click position $position item count ${adapter.itemCount}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                )
            }
        }
    }

    companion object {
        const val BASE_FRAGMENT_INITIAL_DATA_KEY = "19056c4f-5406-4505-9192-164e4b1cbd04"
    }
}