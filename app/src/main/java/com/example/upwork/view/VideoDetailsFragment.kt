package com.example.upwork.view

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.upwork.R
import com.example.upwork.databinding.FragmentVideoDetailsBinding
import com.example.upwork.model.Video
import com.example.upwork.network.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth

import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.pierfrancescosoffritti.androidyoutubeplayer.core.customui.DefaultPlayerUiController
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.FullscreenListener
import kotlinx.coroutines.launch


class VideoDetailsFragment : Fragment() {
    private var _binding: FragmentVideoDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var youTubePlayer: YouTubePlayer
    private var isFullscreen = false
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

        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val video = arguments?.getSerializable("video") as? Video
        val videoId = video?.videoId?.trim() ?: return
        val youTubePlayerView = binding.youtubePlayerView

        val iFramePlayerOptions = IFramePlayerOptions.Builder(requireContext())
            .controls(0)
            .ccLoadPolicy(0)
            .fullscreen(1)
            .build()
        youTubePlayerView.enableAutomaticInitialization = false

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (isFullscreen) {
                        exitCustomFullscreen()
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )

        lifecycleScope.launch {
            val role = repository.getUserRole(uid) ?: "student"
            val hasAccess = repository.checkSessionActivation(uid, videoId)

            if (role == "admin" || role == "instructor") {
                setVideoVisibility(true)
            } else {
                setVideoVisibility(hasAccess)
            }
        }
        
        binding.verifyBtn.setOnClickListener {
            val codeInput = binding.codeEditText.text.toString().trim()
            if (codeInput.isEmpty()) {
                Toast.makeText(context, getString(R.string.enter_valid_code), Toast.LENGTH_SHORT).show()
            } else {
                lifecycleScope.launch {
                    val result = repository.activateCode(codeInput, uid, videoId)
                    result.onSuccess {
                        setVideoVisibility(true)
                        Toast.makeText(context, getString(R.string.code_verified), Toast.LENGTH_SHORT).show()
                    }.onFailure { e ->
                        Toast.makeText(context, getString(R.string.error_prefix) + e.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        binding.videoDescription.text = video?.title ?: getString(R.string.video_desc_placeholder)
        viewLifecycleOwner.lifecycle.addObserver(binding.youtubePlayerView)

        binding.youtubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                this@VideoDetailsFragment.youTubePlayer = youTubePlayer

                val controller = DefaultPlayerUiController(youTubePlayerView, youTubePlayer)
                controller.showYouTubeButton(false)
                controller.showVideoTitle(false)
                
                // Manually handle the fullscreen button click
                controller.setFullscreenButtonClickListener {
                    if (!isFullscreen) {
                        enterCustomFullscreen()
                    } else {
                        exitCustomFullscreen()
                    }
                }
                
                controller.showFullscreenButton(true)
                controller.showUi(true)

                youTubePlayerView.setCustomPlayerUi(controller.rootView)
                youTubePlayer.loadVideo(videoId, 0f)
            }
        }, iFramePlayerOptions)
    }

    private fun enterCustomFullscreen() {
        isFullscreen = true
        
        // change orientation to horizontal
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        // hide app UI
        requireActivity().findViewById<View>(R.id.bottomNavigation)?.visibility = View.GONE
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.hide()
        
        // Immersive Mode
        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        // 4. Expand Player
        binding.youtubePlayerView.matchParent()
    }

    private fun exitCustomFullscreen() {
        isFullscreen = false
        
        // Restore orientation
        requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        
        // Show App UI
        requireActivity().findViewById<View>(R.id.bottomNavigation)?.visibility = View.VISIBLE
        requireActivity().findViewById<DrawerLayout>(R.id.drawer_layout)?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.show()
        
        // Exit Immersive
        val window = requireActivity().window
        WindowCompat.setDecorFitsSystemWindows(window, true)
        WindowInsetsControllerCompat(window, window.decorView).show(WindowInsetsCompat.Type.systemBars())
        
        // Shrink Player
        binding.youtubePlayerView.wrapContent()
    }

    private fun setVideoVisibility(isVisible: Boolean) {
        if (isVisible) {
            binding.youtubePlayerView.visibility = View.VISIBLE
            binding.verifyBtn.visibility = View.GONE
            binding.codeInputLayout.visibility = View.GONE
        } else {
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