package com.example.upwork.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.upwork.R

class CodeAdapter(private var codes: List<String>) : RecyclerView.Adapter<CodeAdapter.CodeViewHolder>() {

    class CodeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val codeTextView: TextView = view.findViewById(R.id.codeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CodeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_code, parent, false)
        return CodeViewHolder(view)
    }

    override fun onBindViewHolder(holder: CodeViewHolder, position: Int) {
        holder.codeTextView.text = codes[position]
    }

    override fun getItemCount(): Int = codes.size

    fun updateCodes(newCodes: List<String>) {
        codes = newCodes
        notifyDataSetChanged()
    }
}
