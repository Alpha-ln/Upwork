package com.example.upwork.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upwork.R
import com.example.upwork.network.FirestoreRepository
import kotlinx.coroutines.launch

class AdminPanelFragment : Fragment() {

    private val repository = FirestoreRepository()
    private lateinit var adapter: AdminInstructorAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_panel, container, false)

        recyclerView = view.findViewById(R.id.pendingRecyclerView)
        progressBar = view.findViewById(R.id.adminProgressBar)
        emptyText = view.findViewById(R.id.emptyStateText)

        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = AdminInstructorAdapter(emptyList()) { instructor ->
            approveInstructor(instructor.instructorId)
        }
        recyclerView.adapter = adapter

        loadPendingInstructors()

        return view
    }

    private fun loadPendingInstructors() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            val pending = repository.getPendingInstructors()
            progressBar.visibility = View.GONE
            
            if (pending.isEmpty()) {
                emptyText.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                emptyText.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
                adapter.updateData(pending)
            }
        }
    }

    private fun approveInstructor(instructorId: String) {
        lifecycleScope.launch {
            val success = repository.approveInstructor(instructorId)
            if (success) {
                Toast.makeText(context, "Instructor Approved!", Toast.LENGTH_SHORT).show()
                loadPendingInstructors()
            } else {
                Toast.makeText(context, "Failed to approve", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
