package com.example.upwork.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upwork.R
import com.example.upwork.network.FirestoreRepository
import kotlinx.coroutines.launch

class InstructorPlaylist : Fragment() {

    private val repository = FirestoreRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_instructor_playlist, container, false)

        // Initialize UI components
        val playlistRV = view.findViewById<RecyclerView>(R.id.playlistRecyclerView)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)

        val instructorId = arguments?.getString("instructorId") ?: return view

        // Set up RecyclerView
        playlistRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        // Fetch data from Firestore
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val playlist = repository.getVideosForInstructor(instructorId)
            progressBar.visibility = View.GONE
            
            if (playlist.isNotEmpty()) {
                playlistRV.adapter = VideoAdapter(playlist) { video ->
                    val bundle = Bundle().apply {
                        putString("videoId", video.videoId)
                    }
                    findNavController().navigate(R.id.action_instructorPlaylist_to_videoDetailsFragment, bundle)
                }
            } else {
                Toast.makeText(context, "No videos found for this instructor", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
