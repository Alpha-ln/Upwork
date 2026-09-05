package com.example.upwork.view

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
    private val videos: List<Video>,
    private val onItemClick: (Video) -> Unit
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val videoCard: View = itemView.findViewById(R.id.videoCard)
        val sessionImageView: ImageView = itemView.findViewById(R.id.sessionImageView)
        val sessionTitle: TextView = itemView.findViewById(R.id.sessionTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item_layout, parent, false)
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
    }

    override fun getItemCount(): Int = videos.size



}