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

package com.example.yandexpictures.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yandexpictures.R
import com.example.yandexpictures.databinding.PicItemBinding
import com.example.yandexpictures.model.Repo

/**
 * View Holder for a [Repo] RecyclerView list item.
 */
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
