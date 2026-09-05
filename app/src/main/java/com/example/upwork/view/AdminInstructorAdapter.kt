package com.example.upwork.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.upwork.R
import com.example.upwork.model.Instructor

class AdminInstructorAdapter(
    private var instructors: List<Instructor>,
    private val onApproveClick: (Instructor) -> Unit
) : RecyclerView.Adapter<AdminInstructorAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.pendingInstructorImage)
        val name: TextView = view.findViewById(R.id.pendingInstructorName)
        val specialty: TextView = view.findViewById(R.id.pendingInstructorSpecialty)
        val approveBtn: Button = view.findViewById(R.id.approveBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pending_instructor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val instructor = instructors[position]
        holder.name.text = instructor.name
        holder.specialty.text = instructor.speciality
        
        Glide.with(holder.itemView.context)
            .load(instructor.imageUrl)
            .placeholder(R.drawable.ic_launcher_background)
            .into(holder.image)

        holder.approveBtn.setOnClickListener { onApproveClick(instructor) }
    }

    override fun getItemCount(): Int = instructors.size

    fun updateData(newList: List<Instructor>) {
        instructors = newList
        notifyDataSetChanged()
    }
}
