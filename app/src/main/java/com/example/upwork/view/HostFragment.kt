package com.example.upwork.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upwork.R
import com.example.upwork.model.Video
import com.example.upwork.network.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class HostFragment : Fragment() {

    private val repository = FirestoreRepository()
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var progressBar: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_host, container, false)

        // Initialize UI components
        val recyclerView = view.findViewById<RecyclerView>(R.id.recentVideosRV)
        progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val welcomeText = view.findViewById<TextView>(R.id.welcomeText)

        // Fetch User Name for Greeting
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    if (!name.isNullOrEmpty()) {
                        welcomeText.text = getString(R.string.welcome_user, name)
                    }
                }
        }

        videoAdapter = VideoAdapter(
            layoutResId = R.layout.recent_instructor_videos,
            onItemClick = { video ->
                val bundle = Bundle().apply {
                    putSerializable("video", video)
                }
                findNavController().navigate(R.id.action_homeItem_to_videoDetailsFragment, bundle)
            }
        )

        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = videoAdapter
        }
        loadRecentVideos()

        return view
    }
    private fun loadRecentVideos() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val video = repository.getRecentVideos()
            videoAdapter.updateVideos(video)
            progressBar.visibility = View.GONE
        }
    }


}
