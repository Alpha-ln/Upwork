package com.example.upwork.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.upwork.R
import com.example.upwork.databinding.FragmentVideoDetailsBinding
import com.example.upwork.model.Video
import com.example.upwork.network.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.launch


class VideoDetailsFragment : Fragment() {
    private var _binding: FragmentVideoDetailsBinding? = null
    private val binding get() = _binding!!

    private val repository = FirestoreRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVideoDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val verifyBtn = binding.verifyBtn
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val video = arguments?.getSerializable("video") as? Video
        val videoId = video?.videoId?.trim() ?: return

        lifecycleScope.launch {
            val role = repository.getUserRole(uid) ?: "student"
            val hasAccess = repository.checkSessionActivation(uid, videoId)
            
            if (role == "admin" || role == "instructor") {
                setVideoVisibility(true)
            } else {
                setVideoVisibility(hasAccess)
            }
        }
        verifyBtn.setOnClickListener {
            val codeInput = binding.codeEditText.text.toString().trim()
            if(codeInput.isEmpty()){
                Toast.makeText(context, "Enter a valid code", Toast.LENGTH_SHORT).show()
            }else {
                lifecycleScope.launch {
                    val result = repository.activateCode(codeInput, uid, videoId)
                    result.onSuccess {
                        setVideoVisibility(true)
                        Toast.makeText(context, "Code verified successfully!", Toast.LENGTH_SHORT).show()
                    }.onFailure { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Set video details
        binding.videoDescription.text = video?.title ?: getString(R.string.video_desc_placeholder)

        viewLifecycleOwner.lifecycle.addObserver(binding.youtubePlayerView)
        // Use ViewBinding instead of findViewById

        binding.youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                videoId?.let {
                    youTubePlayer.cueVideo(it, 0f)
                }
            }
        })
    }

    private fun setVideoVisibility(isVisible: Boolean) {
        if (isVisible) {
            binding.youtubePlayerView.visibility = View.VISIBLE
            binding.verifyBtn.visibility = View.GONE
            binding.codeInputLayout.visibility = View.GONE
        }else{
            binding.youtubePlayerView.visibility = View.GONE
            binding.verifyBtn.visibility = View.VISIBLE
            binding.codeInputLayout.visibility = View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
