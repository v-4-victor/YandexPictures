/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.codelabs.paging.api

import android.util.Log
import com.example.yandexpictures.model.Repo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URL
import java.text.FieldPosition


/**
 * Github API communication setup via Retrofit.
 */
class YaService {
    companion object {
        suspend fun searchRepos(query: String, position: Int, loadSize: Int): List<Repo> {
            lateinit var list: List<Repo>
            Log.d("CORUTINE", "1 ($query, startPosition = $position, loadSize = ")
            withContext(Dispatchers.IO) {

                val data =
                    URL(
                        "https://yandex.ru/images/search?text=$query&p=$position&" +
                                "from=tabbar&format=json&request={%22blocks%22:[{%22block%22:%22serp-" +
                                "list_infinite_yes%22,%22params%22:{%22pageNum%22:0}," +
                                "%22version%22:2}]}"
                    ).readText()
                val doc = Jsoup.parse(
                    JSONObject(data).getJSONArray("blocks").getJSONObject(0).getString("html")
                )

                doc.let {
                    list = it.select(".serp-item.serp-item_type_search").map {
                        var ans = ""
                        var desc = ""
                        val obj = JSONObject(it.attr("data-bem"))
                            .getJSONObject("serp-item")
                            .getJSONArray("preview")
                            .getJSONObject(0)
                        if (obj.has("origin"))
                            ans = obj.getJSONObject("origin")
                                .getString("url")
                        desc = JSONObject(it.attr("data-bem"))
                            .getJSONObject("serp-item")
                            .getJSONObject("snippet")
                            .getString("title")
                        Repo(ans,desc)
                    }
                }
                Log.d("CORUTINE", "2 ($query, startPosition = $position, loadSize = ${list.size}")
            }
            Log.d("CORUTINE", "3 ($query, startPosition = $position, loadSize = ")
            return list
        }
    }
}