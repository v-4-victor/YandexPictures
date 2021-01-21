package com.example.yandexpictures.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.yandexpictures.data.YaRepository
import com.example.yandexpictures.model.Repo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL

@ExperimentalCoroutinesApi
class ViewModel: ViewModel() {
    val ans = MutableLiveData<String>()
    val text = MutableLiveData("hello")
    val repository = YaRepository()
    fun answer() =   viewModelScope.launch { withContext(Dispatchers.IO) { ans.postValue( URL("https://yandex.ru/images/search?text=${text.value}&p=4&from=tabbar&format=json&request={%22blocks%22:[{%22block%22:%22serp-list_infinite_yes%22,%22params%22:{%22pageNum%22:0},%22version%22:2}]}").readText())} }
    private var currentQueryValue: String? = null

    private var currentSearchResult: Flow<PagingData<Repo>>? = null

    fun searchRepo(queryString: String): Flow<PagingData<Repo>> {
        val lastResult = currentSearchResult
        if (queryString == currentQueryValue && lastResult != null) {
            return lastResult
        }
        currentQueryValue = queryString
        val newResult: Flow<PagingData<Repo>> = repository.getSearchResultStream(queryString)
            .cachedIn(viewModelScope)
        currentSearchResult = newResult
        return newResult
    }
}