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
import com.example.upwork.network.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class HostFragment : Fragment() {

    private val repository = FirestoreRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_host, container, false)

        // Initialize UI components
        val recyclerView = view.findViewById<RecyclerView>(R.id.instructorRecyclerView)
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val sessionIdEditText = view.findViewById<EditText>(R.id.sessionIdEditText)
        val goBtn = view.findViewById<Button>(R.id.goBtn)
        val welcomeText = view.findViewById<TextView>(R.id.welcomeText)

        // Fetch User Name for Greeting
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            FirebaseFirestore.getInstance().collection("Users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    if (!name.isNullOrEmpty()) {
                        welcomeText.text = "Welcome, $name!"
                    }
                }
        }

        // Set up RecyclerView (Horizontal as typical for instructor lists)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        
        // Fetch data from Firestore
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val instructorList = repository.getInstructors()
            progressBar.visibility = View.GONE
            
            if (instructorList.isNotEmpty()) {
                recyclerView.adapter = InstructorAdapter(instructorList) { instructor ->
                    val bundle = Bundle().apply {
                        putString("instructorId", instructor.instructorId)
                    }
                    findNavController().navigate(R.id.action_homeItem_to_instructorPlaylist, bundle)
                }
            } else {
                Toast.makeText(context, "No instructors found", Toast.LENGTH_SHORT).show()
            }
        }

        goBtn.setOnClickListener {
            val sessionId = sessionIdEditText.text.toString().trim()
            if (sessionId.isNotEmpty()) {
                navigateToDetails(sessionId)
            } else {
                Toast.makeText(context, "Please enter a Session ID", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun navigateToDetails(videoId: String) {
        val progressBar = view?.findViewById<ProgressBar>(R.id.progressBar)
        progressBar?.visibility = View.VISIBLE

        lifecycleScope.launch {
            val video = repository.getVideoById(videoId)
            progressBar?.visibility = View.GONE

            if (video != null) {
                val bundle = Bundle().apply {
                    putString("videoId", videoId)
                }
                findNavController().navigate(R.id.videoDetailsFragment, bundle)
            } else {
                Toast.makeText(context, "Invalid Video ID or Session not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
