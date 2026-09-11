package com.example.upwork.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upwork.R
import com.example.upwork.model.Video
import com.example.upwork.network.FirestoreRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class InstructorPlaylist : Fragment() {

    private lateinit var videoAdapter: VideoAdapter
    private lateinit var playlistProgressBar: ProgressBar
    private lateinit var addNewVideoBtn: FloatingActionButton
    private val repository = FirestoreRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.fragment_instructor_playlist, container, false)

        val playlistRV = view.findViewById<RecyclerView>(R.id.playlistRecyclerView)
        playlistProgressBar = view.findViewById(R.id.playlistProgressBar)
        addNewVideoBtn = view.findViewById(R.id.addNewVideoBtn)

        videoAdapter = VideoAdapter(
            onItemClick = { video ->
                val bundle = Bundle().apply {
                    putSerializable("video", video)
                }
                findNavController().navigate(R.id.action_profileItem_to_videoDetailsFragment, bundle)
            },
            onDeleteVideo = { video ->
                showDeleteVideoDialog(video)
            }
        )

        playlistRV.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            adapter = videoAdapter
        }

        addNewVideoBtn.setOnClickListener {
            showAddVideoDialog()
        }

        loadVideos()

        return view
    }

    private fun showDeleteVideoDialog(video: Video) {
            AlertDialog.Builder(requireContext())
                .setTitle("Delete Video")
                .setMessage("Are you sure to delete this session?")
                .setPositiveButton("Delete") { _, _ ->
                    deleteVideo(video)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

    private fun deleteVideo(video: Video) {
        lifecycleScope.launch {
            playlistProgressBar.visibility = View.VISIBLE
            val success = repository.deleteVideo(video)
            if (success) {
                Toast.makeText(requireContext(), "Video deleted successfully", Toast.LENGTH_SHORT).show()
                loadVideos()
            } else {
                Toast.makeText(requireContext(), "Failed to delete video", Toast.LENGTH_SHORT).show()
            }
            playlistProgressBar.visibility = View.GONE
        }
    }

    private fun loadVideos() {
        playlistProgressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val videos = repository.getAllVideos()
            videoAdapter.updateVideos(videos)
            playlistProgressBar.visibility = View.GONE
        }
    }

    private fun showAddVideoDialog() {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 10)
        }

        val titleInput = EditText(requireContext()).apply {
            hint = "Video Title"
        }
        val urlInput = EditText(requireContext()).apply {
            hint = "YouTube URL"
        }

        layout.addView(titleInput)
        layout.addView(urlInput)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add New Video")
            .setView(layout)
            .setPositiveButton("Add") { _, _ ->
                val title = titleInput.text.toString().trim()
                val url = urlInput.text.toString().trim()
                val videoId = extractYoutubeVideoId(url)

                if (title.isNotEmpty() && videoId != null) {
                    saveVideo(title, videoId)
                } else {
                    Toast.makeText(requireContext(), "Please provide a valid title and YouTube URL", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun extractYoutubeVideoId(url: String): String? {
        val pattern = "^.*(?:(?:youtu\\.be/|v/|vi/|u/\\w/|embed/|shorts/)|(?:(?:watch)?\\?vi?=|&vi?=))([^#&?]*).*"
        val compiledPattern = Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(url)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun saveVideo(title: String, videoId: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val video = Video(
            videoId = videoId,
            title = title,
            instructorId = currentUser?.uid ?: "unknown",
            createdAt = Timestamp.now()
        )

        lifecycleScope.launch {
            playlistProgressBar.visibility = View.VISIBLE
            val success = repository.addNewVideo(video)
            if (success) {
                Toast.makeText(requireContext(), "Video added successfully", Toast.LENGTH_SHORT).show()
                loadVideos()
            } else {
                Toast.makeText(requireContext(), "Failed to add video", Toast.LENGTH_SHORT).show()
            }
            playlistProgressBar.visibility = View.GONE
        }
    }
}
