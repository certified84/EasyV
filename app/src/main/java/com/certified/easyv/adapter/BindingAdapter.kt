package com.certified.easyv.adapter

import android.os.Build
import android.text.Html
import android.view.View
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.certified.easyv.R
import com.certified.easyv.data.model.Candidate
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.textview.MaterialTextView
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@BindingAdapter("visible")
fun View.setVisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("parse_html")
fun MaterialTextView.parseHtml(htmlText: String) {
    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        Html.fromHtml(htmlText, Html.FROM_HTML_MODE_COMPACT)
    else
        Html.fromHtml(htmlText)
}

@BindingAdapter("server_time")
fun MaterialTextView.parseServerTime(time: String) {
    text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        ZonedDateTime.parse(time)
            .withZoneSameInstant(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("dd/MM/YYYY hh:mm a")).toString()
    else
        time
}

@BindingAdapter("load_image")
fun ShapeableImageView.loadImage(image: String?) {
    if (image != null) this.load(image)
    else this.load(R.drawable.no_profile_image)
}

@BindingAdapter("listCandidates")
fun bindUCandidatesRecyclerView(
    recyclerView: RecyclerView,
    data: List<Candidate>?
) {
    val adapter = recyclerView.adapter as CandidateRecyclerAdapter
    adapter.submitList(data)
}