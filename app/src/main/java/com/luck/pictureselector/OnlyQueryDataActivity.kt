package com.luck.pictureselector

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import com.luck.picture.lib.basic.PictureSelector
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.config.SelectMimeType
import com.luck.picture.lib.decoration.GridSpacingItemDecoration
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.entity.LocalMediaFolder
import com.luck.picture.lib.utils.DateUtils
import com.luck.picture.lib.utils.DensityUtil
import com.luck.picture.lib.widget.RecyclerPreloadView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import kotlin.concurrent.thread

/**
 * @author：luck
 * @date：2022/2/17 6:24 下午
 * @describe：OnlyQueryDataActivity
 */
class OnlyQueryDataActivity() : AppCompatActivity() {
    private val mData: MutableList<LocalMedia> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_only_query_data)
        val mRecycler = findViewById<RecyclerPreloadView>(R.id.recycler)
        mRecycler.addItemDecoration(
            GridSpacingItemDecoration(
                4,
                DensityUtil.dip2px(this, 1f), false
            )
        )
        mRecycler.layoutManager = GridLayoutManager(this, 4)
        val itemAnimator = mRecycler.itemAnimator
        if (itemAnimator != null) {
            (itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
            mRecycler.itemAnimator = null
        }
        val adapter = GridAdapter(mData)
        mRecycler.adapter = adapter
//        PictureSelector.create(this)
//            .dataSource(SelectMimeType.ofAll())
//            .setQuerySortOrder(MediaStore.MediaColumns.DATE_MODIFIED + " DESC")
//            .obtainMediaData { result ->
//                mData.addAll(result)
//                adapter.notifyDataSetChanged()
//            }
        findViewById<View>(R.id.tv_build_loader).setOnClickListener(
            View.OnClickListener { v ->
                fun onResult(
                    result: List<LocalMediaFolder>,
                    type: String,
                    isPageStrategy: Boolean
                ) =
                    run {
                        val toJson = Gson().toJson(result)
                        thread {
                            val downloadCacheDirectory =
                                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                            val loadAllAlbum = File(
                                downloadCacheDirectory,
                                "loadAllAlbum$isPageStrategy"
                            )
                            if (!loadAllAlbum.exists()) {
                                loadAllAlbum.mkdir()
                            }
                            val file = File(loadAllAlbum, "$type.json")
                            Log.d("loadAllAlbum", file.absolutePath + " ${result.size}")
                            file.writeText(toJson)
                        }
                    }

                suspend fun loaderData(isPageStrategy: Boolean) {
                    val loader1 = PictureSelector.create(v.context)
                        .dataSource(SelectMimeType.ofDocument())
                        .isPageStrategy(isPageStrategy)
                        .setQueryOnlyMimeType(PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_PDF)
                        .buildMediaLoader()
                    loader1.loadAllAlbum { result ->
                        onResult(result, "pdf", isPageStrategy)
                    }
                    delay(3000)
                    val loader2 = PictureSelector.create(v.context)
                        .dataSource(SelectMimeType.ofDocument())
                        .isPageStrategy(isPageStrategy)
                        .setQueryOnlyMimeType(PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_DOC)
                        .buildMediaLoader()
                    loader2.loadAllAlbum { result ->
                        onResult(result, "doc", isPageStrategy)
                    }
                    delay(3000)
                    val loader3 = PictureSelector.create(v.context)
                        .dataSource(SelectMimeType.ofDocument())
                        .isPageStrategy(isPageStrategy)
                        .setQueryOnlyMimeType(PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_XLS)
                        .buildMediaLoader()
                    loader3.loadAllAlbum { result ->
                        onResult(result, "xls", isPageStrategy)
                    }
                    delay(3000)
                    val loader4 = PictureSelector.create(v.context)
                        .dataSource(SelectMimeType.ofDocument())
                        .isPageStrategy(isPageStrategy)
                        .setQueryOnlyMimeType(PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_PPT)
                        .buildMediaLoader()
                    loader4.loadAllAlbum { result ->
                        onResult(result, "ppt", isPageStrategy)
                    }
                    delay(3000)
                    val loader5 = PictureSelector.create(v.context)
                        .dataSource(SelectMimeType.ofDocument())
                        .isPageStrategy(isPageStrategy)
                        .setQueryOnlyMimeType(PictureMimeType.MIME_TYPE_PREFIX_DOCUMENT_TXT)
                        .buildMediaLoader()
                    loader5.loadAllAlbum { result ->
                        onResult(result, "txt", isPageStrategy)
                    }
                    delay(3000)
                    val loader = PictureSelector.create(v.context)
                        .dataSource(SelectMimeType.ofDocument())
                        .isPageStrategy(isPageStrategy)
                        .buildMediaLoader()
                    loader.loadAllAlbum { result ->
                        onResult(result, "all", isPageStrategy)
                    }
                }
                lifecycleScope.launch {
                    loaderData(false)
                    delay(3000)
                    loaderData(true)
                }
            })
    }

    class GridAdapter(private val list: List<LocalMedia>) :
        RecyclerView.Adapter<GridAdapter.ViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view =
                LayoutInflater.from(parent.context).inflate(R.layout.gv_filter_image, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            viewHolder.mIvDel.visibility = View.GONE
            val media = list[position]
            val chooseModel = media.chooseModel
            val path = media.path
            val duration = media.duration
            viewHolder.tvDuration.visibility =
                if (PictureMimeType.isHasVideo(media.mimeType)) View.VISIBLE else View.GONE
            if (chooseModel == SelectMimeType.ofAudio()) {
                viewHolder.tvDuration.visibility = View.VISIBLE
                viewHolder.tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ps_ic_audio,
                    0,
                    0,
                    0
                )
            } else {
                viewHolder.tvDuration.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    R.drawable.ps_ic_video,
                    0,
                    0,
                    0
                )
            }
            viewHolder.tvDuration.text = DateUtils.formatDurationTime(duration)
            if (chooseModel == SelectMimeType.ofAudio()) {
                viewHolder.mImg.setImageResource(R.drawable.ps_audio_placeholder)
            } else {
                Glide.with(viewHolder.itemView.context)
                    .load(if (PictureMimeType.isContent(path)) Uri.parse(path) else path)
                    .centerCrop()
                    .placeholder(R.drawable.ps_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .into(viewHolder.mImg)
            }
        }

        override fun getItemCount(): Int {
            return list.size
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            var mImg: ImageView
            var mIvDel: ImageView
            var tvDuration: TextView

            init {
                mImg = view.findViewById(R.id.fiv)
                mIvDel = view.findViewById(R.id.iv_del)
                tvDuration = view.findViewById(R.id.tv_duration)
            }
        }
    }
}