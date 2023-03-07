package com.kunkunapp.allvideodowloader.adapters

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.text.TextUtils
import android.text.format.Formatter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kunkunapp.allvideodowloader.R
import com.kunkunapp.allvideodowloader.browser.BrowserWindow
import com.kunkunapp.allvideodowloader.model.VidInfoItem
import com.kunkunapp.allvideodowloader.utils.NumberUtils
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.android.synthetic.main.vid_format.view.*
import kotlinx.android.synthetic.main.vid_header.view.*
import kotlinx.android.synthetic.main.video_found_item_lay.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

private const val ITEM_VIEW_TYPE_HEADER = 0
private const val ITEM_VIEW_TYPE_ITEM = 1

class VidInfoAdapter(private val clickListener: VidInfoListener) :
    ListAdapter<VidInfoItem, RecyclerView.ViewHolder>(
        VidInfoDiffCallback()
    ) {
    private lateinit var mImageView: ImageView
    private lateinit var tittle: EditText

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    fun fill(vidInfo: VideoInfo?, imageView: ImageView, title: EditText) {
        adapterScope.launch {
            if (vidInfo == null) {
                submitList(emptyList())
                return@launch
            }
            val items = mutableListOf<VidInfoItem>()
            withContext(Dispatchers.Default) {
                items.add(VidInfoItem.VidHeaderItem(vidInfo))
                vidInfo.formats?.forEach { format ->
                    items.add(
                        VidInfoItem.VidFormatItem(
                            vidInfo,
                            format.formatId
                        )
                    )
                }
            }

            withContext(Dispatchers.Main) {
                submitList(items.toList())
            }
        }
        mImageView = imageView
        tittle = title
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val vidItem = getItem(position) as VidInfoItem.VidFormatItem
                val vidFormat = vidItem.vidFormat
                with(holder.itemView) {
                    videoFoundName.text = vidFormat.format
//                    format_tv.text = vidFormat.format
//                    ext_tv.text = vidFormat.ext
//                    size_tv.text = Formatter.formatShortFileSize(context, vidFormat.fileSize)
//                    fps_tv.text = context.getString(R.string.fps_value, vidFormat.fps)
//                    abr_tv.text = context.getString(R.string.abr_value, vidFormat.abr)
//                    if (vidFormat.acodec != "none" && vidFormat.vcodec == "none") {
//                        format_ic.setImageResource(R.drawable.ic_baseline_audiotrack_24)
//                    } else {
//                        format_ic.setImageResource(R.drawable.ic_baseline_video_library_24)
//                    }
//                    item_share.setOnClickListener {
//                        shareUrl(vidFormat.url, context)
//                    }
//                    setOnClickListener { clickListener.onClick(vidItem) }

                    try {
                        txtQuality.text = BrowserWindow.convertSolution(vidFormat.format)
                    } catch (e: IllegalArgumentException) {
                        e.printStackTrace()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    if (position == 0) {
                        mImageView.visibility = View.VISIBLE
                        if (vidItem.vidInfo.title != null && vidItem.vidInfo.title.isNotEmpty()) {
                            tittle.visibility = View.VISIBLE
                            tittle.setText(vidItem.vidInfo.title)
                        } else {
                            tittle.visibility = View.INVISIBLE
                        }
                        Glide.with(context)
                            .load(vidItem.vidInfo.url)
                            .thumbnail(0.5f)
                            .into(mImageView)
                    }

                    if (TextUtils.isEmpty(vidItem.vidInfo.fileSize.toString())) {
                        val sizeFormatted = Formatter.formatShortFileSize(context, vidItem.vidInfo.fileSize)
                        videoFoundSize.text = sizeFormatted
                    } else {
                        videoFoundSize.text = " "
                    }

                    Log.d("TAG", "onBindViewHolder: "+vidFormat.format)

//                    if (selectedVideo == position) {
//                        imgSelected.visibility = View.VISIBLE
//                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//                            llMain.setBackgroundDrawable(
//                                ContextCompat.getDrawable(
//                                    context,
//                                    R.drawable.bg_round_quality_select
//                                )
//                            )
//                        } else {
//                            llMain.setBackground(
//                                ContextCompat.getDrawable(
//                                    context,
//                                    R.drawable.bg_round_quality_select
//                                )
//                            )
//                        }
//                    } else {
//                        imgSelected.setVisibility(View.GONE)
//                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
//                            llMain.setBackgroundDrawable(
//                                ContextCompat.getDrawable(
//                                    context,
//                                    R.drawable.bg_round_quality_unselect
//                                )
//                            )
//                        } else {
//                            llMain.background = ContextCompat.getDrawable(
//                                context,
//                                R.drawable.bg_round_quality_unselect
//                            )
//                        }
//                    }
                }
            }
            else -> {
                val vidItem = getItem(position) as VidInfoItem.VidHeaderItem
                val vidInfo = vidItem.vidInfo
                with(holder.itemView) {
                    title_tv.text = vidInfo.title
                    uploader_tv.text = vidInfo.uploader
                    uploader_tv.isSelected = true
                    views_tv.text = vidInfo.viewCount?.toLongOrNull()?.let {
                        NumberUtils.format(it)
                    } ?: vidInfo.viewCount
                    likes_tv.text = vidInfo.likeCount?.toLongOrNull()?.let {
                        NumberUtils.format(it)
                    } ?: vidInfo.likeCount
                    dislikes_tv.text = vidInfo.dislikeCount?.toLongOrNull()?.let {
                        NumberUtils.format(it)
                    } ?: vidInfo.dislikeCount
                    upload_date_tv.text = vidInfo.uploadDate
                    vidInfo.duration.toLong().apply {
                        val minutes = TimeUnit.SECONDS.toMinutes(this)
                        val seconds = this - TimeUnit.MINUTES.toSeconds(minutes)
                        duration_tv.text = context.getString(R.string.duration, minutes, seconds)
                    }
                }
            }
        }
    }

    private fun shareUrl(url: String, context: Context) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, url)
        startActivity(context, Intent.createChooser(intent, null), null)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_HEADER -> HeaderViewHolder.from(
                parent
            )
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(
                parent
            )
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is VidInfoItem.VidHeaderItem -> ITEM_VIEW_TYPE_HEADER
            is VidInfoItem.VidFormatItem -> ITEM_VIEW_TYPE_ITEM
        }
    }

    class HeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.vid_header, parent, false)
                return HeaderViewHolder(
                    view
                )
            }
        }
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.video_found_item_lay, parent, false)
                return ViewHolder(view)
            }
        }
    }
}

class VidInfoDiffCallback : DiffUtil.ItemCallback<VidInfoItem>() {
    override fun areItemsTheSame(oldItem: VidInfoItem, newItem: VidInfoItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: VidInfoItem, newItem: VidInfoItem): Boolean {
        return oldItem == newItem
    }
}


class VidInfoListener(val clickListener: (VidInfoItem.VidFormatItem) -> Unit) {
    fun onClick(vidInfo: VidInfoItem.VidFormatItem) = clickListener(vidInfo)
}
