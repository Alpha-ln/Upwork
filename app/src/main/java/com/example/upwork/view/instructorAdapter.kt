package com.example.upwork.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.upwork.R
import com.example.upwork.model.Instructor

class InstructorAdapter(
    private var instructors: List<Instructor>,
    private val onInstructorClick: (Instructor) -> Unit
) : RecyclerView.Adapter<InstructorAdapter.InstructorViewHolder>() {

    inner class InstructorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val instructorImage: ImageView = itemView.findViewById(R.id.instructorImageView)
        private val instructorName: TextView = itemView.findViewById(R.id.instructorName)
        private val instructorSpecialty: TextView = itemView.findViewById(R.id.instructorSpecialty)

        fun bind(instructor: Instructor) {
            instructorName.text = instructor.name
            instructorSpecialty.text = instructor.speciality
            
            Glide.with(itemView.context)
                .load(instructor.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(instructorImage)

            itemView.setOnClickListener { onInstructorClick(instructor) }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): InstructorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_instructor, parent, false)
        return InstructorViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: InstructorViewHolder,
        position: Int
    ) {
        holder.bind(instructors[position])
    }

    override fun getItemCount(): Int = instructors.size
}