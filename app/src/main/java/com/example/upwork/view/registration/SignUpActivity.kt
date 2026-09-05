package com.example.upwork.view.registration

import android.R
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.upwork.databinding.SignUpLayoutBinding
import com.google.firebase.auth.FirebaseAuth

class SignUpActivity: AppCompatActivity() {
    private lateinit var binding: SignUpLayoutBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SignUpLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Setup student level dropdown
        val levels = arrayOf("First level", "Second level", "Third level")
        val adapter = ArrayAdapter(this, R.layout.simple_list_item_1, levels)
        (binding.studentLevel.editText as? AutoCompleteTextView)?.setAdapter(adapter)

        // Get text when button is clicked
        binding.signUpBtn.setOnClickListener {
            val email = binding.emailInput.editText?.text.toString().trim()
            val password = binding.passwordInput.editText?.text.toString().trim()
            val confirmPassword = binding.confirmPassInput.editText?.text.toString().trim()
            val name = binding.nameInput.editText?.text.toString().trim()
            val studentLevel = binding.studentLevel.editText?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty() || studentLevel.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password != confirmPassword){
                binding.confirmPassInput.error = "Passwords not match"
                return@setOnClickListener
            }
            if (password.length < 6){
                binding.passwordInput.error  =  "Password must be at least 6 characters"
                return@setOnClickListener
            }

            binding.signUpBtn.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }

                .addOnFailureListener { e ->
                    binding.signUpBtn.isEnabled = true
                    Toast.makeText(this, e.message ?: "Sign up failed", Toast.LENGTH_LONG).show()
                }

        }

        // Respond to text changes in real-time
        binding.emailInput.editText?.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                binding.emailInput.error = "Email is required"
            } else {
                binding.emailInput.error = null
            }
        }

        //back to  login page with arrow back btn
        binding.backToLogin.setOnClickListener {
            val logInIntent = Intent(this, LoginActivity::class.java)
            startActivity(logInIntent)
        }
    }

}