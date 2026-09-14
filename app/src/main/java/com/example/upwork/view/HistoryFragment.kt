package com.example.upwork.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.upwork.R
import com.example.upwork.databinding.FragmentHistoryBinding
import com.example.upwork.network.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val repository = FirestoreRepository()
    private lateinit var videoAdapter: VideoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecyclerView()
        loadVideos()
    }

    private fun setUpRecyclerView() {
        videoAdapter = VideoAdapter(onItemClick = { video ->
            val bundle = Bundle().apply {
                putSerializable("video", video)
            }
            findNavController().navigate(R.id.action_historyItem_to_videoDetailsFragment, bundle)
        })
        binding.historyRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = videoAdapter
        }
    }
    private fun loadVideos() {
        viewLifecycleOwner.lifecycleScope.launch {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            val role = repository.getUserRole(uid)
            val videos = if (role == "instructor"|| role == "admin"){
                repository.getAllVideos()
            } else {
                repository.getUnlockedVideos(uid)
            }
            videoAdapter.updateVideos(videos)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
