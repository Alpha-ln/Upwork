package com.example.upwork.view

import android.content.Context
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.upwork.R
import com.example.upwork.model.ActivationCode
import com.example.upwork.model.Video
import com.example.upwork.network.FirestoreRepository
import kotlinx.coroutines.launch
import java.util.UUID

class CodeGeneratingFragment : Fragment() {

    private lateinit var codeAdapter: CodeAdapter
    private val repository = FirestoreRepository()
    private var generatedCodesList = mutableListOf<String>()
    private var videos = listOf<Video>()
    private var selectedVideoId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_code_generating, container, false)

        val displayCodeRV = view.findViewById<RecyclerView>(R.id.displayCodeRecyclerView)
        val numOfCodeEditText = view.findViewById<EditText>(R.id.numOfCodeEditText)
        val genBtn = view.findViewById<Button>(R.id.generateBtn)
        val printBtn = view.findViewById<Button>(R.id.printBtn)
        val videoSpinner = view.findViewById<Spinner>(R.id.videoSpinner) // add this to your XML

        codeAdapter = CodeAdapter(generatedCodesList)
        displayCodeRV.adapter = codeAdapter
        displayCodeRV.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        loadVideosIntoSpinner(videoSpinner)

        genBtn.setOnClickListener {
            val countStr = numOfCodeEditText.text.toString().trim()
            val videoId = selectedVideoId

            if (videoId.isNullOrEmpty()) {
                Toast.makeText(context, getString(R.string.select_video_first), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (countStr.isEmpty()) {
                Toast.makeText(context, getString(R.string.enter_num_codes), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val count = countStr.toIntOrNull() ?: 0
            if (count <= 0) {
                Toast.makeText(context, getString(R.string.enter_valid_num), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            generateCodes(count, videoId)
        }

        printBtn.setOnClickListener {
            if (generatedCodesList.isNotEmpty()) {
                doPrint()
            } else {
                Toast.makeText(context, getString(R.string.generate_codes_first), Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun loadVideosIntoSpinner(spinner: Spinner) {
        lifecycleScope.launch {
            videos = repository.getAllVideos()
            val titles = videos.map { it.title }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, titles)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter

            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedVideoId = videos.getOrNull(position)?.videoId
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    selectedVideoId = null
                }
            }
        }
    }

    private fun generateCodes(count: Int, videoId: String) {
        generatedCodesList.clear()
        val codeMap = mutableMapOf<String, ActivationCode>()

        repeat(count) {
            val codeStr = UUID.randomUUID().toString().substring(0, 6).uppercase()
            generatedCodesList.add(codeStr)
            codeMap[codeStr] = ActivationCode(
                status = "active",
                usedAt = null,
                usedByStudentId = null,
                videoId = videoId
            )
        }
        codeAdapter.updateCodes(generatedCodesList)

        lifecycleScope.launch {
            val success = repository.saveCodes(codeMap)
            val msg = if (success) getString(R.string.codes_saved) else getString(R.string.failed_save_codes)
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun doPrint() {
        val printManager = requireContext().getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "${getString(R.string.app_name)} Document"

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