package com.example.yandexpictures

import android.widget.ImageView
import androidx.databinding.BindingAdapter

@BindingAdapter("setImage")
fun ImageView.setImage(picture: Picture) {
    setImageResource(R.mipmap.ic_launcher)
}