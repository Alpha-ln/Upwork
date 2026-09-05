package com.example.upwork.view

import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upwork.R
import com.example.upwork.model.Code
import com.example.upwork.network.FirestoreRepository
import kotlinx.coroutines.launch
import java.util.UUID

class CodeGeneratingFragment : Fragment() {

    private lateinit var codeAdapter: CodeAdapter
    private val repository = FirestoreRepository()
    private var generatedCodesList = mutableListOf<String>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_code_generating, container, false)

        val displayCodeRV = view.findViewById<RecyclerView>(R.id.displayCodeRecyclerView)
        val numOfCodeEditText = view.findViewById<EditText>(R.id.numOfCodeEditText)
        val genBtn = view.findViewById<Button>(R.id.generateBtn)
        val printBtn = view.findViewById<Button>(R.id.printBtn)

        codeAdapter = CodeAdapter(generatedCodesList)
        displayCodeRV.adapter = codeAdapter
        displayCodeRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        genBtn.setOnClickListener {
            val countStr = numOfCodeEditText.text.toString().trim()
            if (countStr.isNotEmpty()) {
                val count = countStr.toIntOrNull() ?: 0
                if (count > 0) {
                    generateCodes(count)
                } else {
                    Toast.makeText(context, "Enter a valid number", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Enter number of codes", Toast.LENGTH_SHORT).show()
            }
        }

        printBtn.setOnClickListener {
            if (generatedCodesList.isNotEmpty()) {
                doPrint()
            } else {
                Toast.makeText(context, "Generate codes first", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }

    private fun generateCodes(count: Int) {
        generatedCodesList.clear()
        val codesToSave = mutableListOf<Code>()
        
        for (i in 1..count) {
            val codeStr = UUID.randomUUID().toString().substring(0, 6).uppercase()
            generatedCodesList.add(codeStr)
            codesToSave.add(Code(code = codeStr, status = false))
        }
        codeAdapter.updateCodes(generatedCodesList)

        // Save to Firestore
        lifecycleScope.launch {
            val success = repository.saveCodes(codesToSave)
            if (success) {
                Toast.makeText(context, "Codes saved to database", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun doPrint() {
        val printManager = requireContext().getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "${getString(R.string.app_name)} Document"
        
        // Use a simple HTML-based print approach
        val webView = WebView(requireContext())
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                val printAdapter = webView.createPrintDocumentAdapter(jobName)
                printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
            }
        }

        val htmlContent = StringBuilder("<html><body><h1>Generated Access Codes</h1><ul>")
        for (code in generatedCodesList) {
            htmlContent.append("<li><h2>$code</h2></li>")
        }
        htmlContent.append("</ul></body></html>")

        webView.loadDataWithBaseURL(null, htmlContent.toString(), "text/html", "utf-8", null)
    }
}