package com.example.upwork.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.upwork.R
import com.example.upwork.model.Video

class VideoAdapter(
    private val layoutResId: Int = R.layout.row_item_layout,
    private var videos: List<Video> = emptyList(),
    private val onItemClick: (Video) -> Unit,
    private val onDeleteVideo: ((Video) -> Unit)? = null
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    fun updateVideos(newVideos: List<Video>) {
        this.videos = newVideos
        notifyDataSetChanged()
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoCard: View = itemView.findViewById(R.id.videoCard)
        val sessionImageView: ImageView = itemView.findViewById(R.id.sessionImageView)
        val sessionTitle: TextView = itemView.findViewById(R.id.sessionTitle)
        val deleteVideoBtn: View? = itemView.findViewById(R.id.deleteVideoBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layoutResId, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videos[position]
        
        holder.sessionTitle.text = video.title

        // Automatically generate the YouTube thumbnail URL from the videoId
        val thumbnailUrl = "https://img.youtube.com/vi/${video.videoId}/maxresdefault.jpg"

        Glide.with(holder.itemView.context)
            .load(thumbnailUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background) // Fallback if maxres is not available
            .into(holder.sessionImageView)

        // Handle click on the whole card
        holder.videoCard.setOnClickListener {
            onItemClick(video)
        }
        
        if (onDeleteVideo != null) {
            holder.deleteVideoBtn?.visibility = View.VISIBLE
            holder.deleteVideoBtn?.setOnClickListener {
                onDeleteVideo.invoke(video)
            }
        } else {
            holder.deleteVideoBtn?.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = videos.size



}