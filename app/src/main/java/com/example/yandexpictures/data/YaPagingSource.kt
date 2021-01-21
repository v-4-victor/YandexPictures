package com.example.yandexpictures.data

import android.util.Log
import androidx.paging.PagingSource
import com.bumptech.glide.load.HttpException
import com.example.android.codelabs.paging.api.YaService
import com.example.yandexpictures.model.Repo
import java.io.IOException

// GitHub page API is 1 based: https://developer.github.com/v3/#pagination
private const val GITHUB_STARTING_PAGE_INDEX = 0

class YaPagingSource(
        private val query: String
) : PagingSource<Int, Repo>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> {
        val position = params.key ?: GITHUB_STARTING_PAGE_INDEX
        val apiQuery = query
        return try {
            val response = YaService.searchRepos(apiQuery, position, params.loadSize)
            Log.d("PAGINATON", "loadRange, startPosition = " + position + ", loadSize = " + params.loadSize)
            LoadResult.Page(
                    data = response,
                    prevKey = if (position == GITHUB_STARTING_PAGE_INDEX) null else position - 1,
                    nextKey = if (response.isEmpty()) null else position + 1
            )
        } catch (exception: IOException) {
            return LoadResult.Error(exception)
        } catch (exception: HttpException) {
            return LoadResult.Error(exception)
        }
    }
}