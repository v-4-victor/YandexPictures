package com.example.yandexpictures

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.paging.filter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.ListPreloader.PreloadModelProvider
import com.bumptech.glide.RequestBuilder
import com.example.yandexpictures.databinding.ActivityMainBinding
import com.example.yandexpictures.databinding.PicItemBinding
import com.example.yandexpictures.model.Repo
import com.example.yandexpictures.ui.ReposAdapter
import com.example.yandexpictures.ui.ViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import java.util.*


@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity() {
    var list = mutableListOf<String>()
    private val ViewModel: ViewModel by viewModels()
    lateinit var imageView: ImageView
    private val imageWidthPixels = 1024;
    private val imageHeightPixels = 768;
    private lateinit var binder: ActivityMainBinding
    private var searchJob: Job? = null
    private val adapter = ReposAdapter()


    private fun search(query: String) {
        // Make sure we cancel the previous job before creating a new one
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            ViewModel.searchRepo(query).collect {

                adapter.submitData(it.filter { it.url !="" })
            }
        }
    }

    // You will need to populate these urls somewhere...
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = DataBindingUtil.setContentView(this, R.layout.activity_main)

        binder.lifecycleOwner = this
        binder.myViewModel = ViewModel
        ViewModel.text.observe(this)
        {
            //ViewModel.answer()
        }
        binder.recyclerView.adapter = adapter
        search(ViewModel.text.value?:"android")
        initSearch(ViewModel.text.value?:"android")
//        ViewModel.ans.observe(this) {
//            val doc = Jsoup.parse(JSONObject(it).getJSONArray("blocks").getJSONObject(0).getString("html"))
//            doc.let {
//                list = it.select(".serp-item.serp-item_type_search").map {
//                    var ans = ""
//                    val obj = JSONObject(it.attr("data-bem"))
//                        .getJSONObject("serp-item")
//                        .getJSONArray("preview")
//                        .getJSONObject(0)
//                    if (obj.has("origin"))
//                        ans = obj.getJSONObject("origin")
//                            .getString("url")
//                    ans
//                }.toMutableList()
//            }
//            list.removeAll { it == "" }
//            binder.textView.text = list.toString()
//            binder.recyclerView.adapter = PicAdapter(list.map { Picture(it) })
//        }
//        val sizeProvider: ListPreloader.PreloadSizeProvider<Any?> =
//            FixedPreloadSizeProvider<Any?>(imageWidthPixels, imageHeightPixels)
//        val modelProvider: PreloadModelProvider<Any?> = MyPreloadModelProvider()
//        val preloader: RecyclerViewPreloader<Any?> = RecyclerViewPreloader<Any?>(this, modelProvider, sizeProvider, 10 /*maxPreload*/)
//        binder.recyclerView.addOnScrollListener(preloader)
    }

    private fun initSearch(query: String) {
        binder.editTextTextPersonName.setText(query)

        binder.editTextTextPersonName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                updateRepoListFromInput()
                true
            } else {
                false
            }
        }
        binder.editTextTextPersonName.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                updateRepoListFromInput()
                true
            } else {
                false
            }
        }

        // Scroll to top when the list is refreshed from network.
        lifecycleScope.launch {
            adapter.loadStateFlow
                // Only emit when REFRESH LoadState for RemoteMediator changes.
                .distinctUntilChangedBy { it.refresh }
                .filter { it.refresh is LoadState.NotLoading }
                .collect { binder.recyclerView.scrollToPosition(0) }
        }
    }

    private fun updateRepoListFromInput() {
        binder.editTextTextPersonName.text.trim().let {
            if (it.isNotEmpty()) {
                binder.recyclerView.scrollToPosition(0)
                search(it.toString())
            }
        }
    }


    class PicAdapter(val data: List<Picture>) : RecyclerView.Adapter<PicAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder.from(parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = data[position]
            holder.bind(Repo(item.url, ""))
        }

        class ViewHolder private constructor(val binding: PicItemBinding, val context: Context) :
            RecyclerView.ViewHolder(binding.root) {
            private val imageWidthPixels = 1024;
            private val imageHeightPixels = 768;
            fun bind(item: Repo) {
                binding.pic = item
                Glide.with(binding.imageView.context)
                    .load(item.url)
                    .error(R.drawable.ic_launcher_background)
                    .into(binding.imageView)

                binding.executePendingBindings()
            }

            companion object {
                fun from(parent: ViewGroup): ViewHolder {
                    val layoutInflater = LayoutInflater.from(parent.context)
                    val binding =
                        PicItemBinding.inflate(layoutInflater, parent, false)
                    return ViewHolder(binding, parent.context)
                }
            }
        }

        override fun getItemCount(): Int {
            return data.size
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Picture>() {
        override fun areItemsTheSame(oldItem: Picture, newItem: Picture): Boolean {
            return oldItem.url == newItem.url
        }

        override fun areContentsTheSame(oldItem: Picture, newItem: Picture): Boolean {
            return oldItem.url == newItem.url
        }

    }

    private inner class MyPreloadModelProvider : PreloadModelProvider<Any?> {
        override fun getPreloadItems(position: Int): MutableList<String?> {
            val url: String = list[position]
            return if (TextUtils.isEmpty(url)) {
                Collections.emptyList()
            } else Collections.singletonList(url)
        }

        override fun getPreloadRequestBuilder(item: Any): RequestBuilder<*> {
            return Glide.with(this@MainActivity)
                .load(item)
                .override(imageWidthPixels, imageHeightPixels)
        }


    }

    companion object {
        private const val LAST_SEARCH_QUERY: String = "last_search_query"
        private const val DEFAULT_QUERY = "Android"
    }

}