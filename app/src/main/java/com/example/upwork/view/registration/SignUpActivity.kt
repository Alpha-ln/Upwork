package com.example.upwork.view.registration

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.upwork.databinding.SignUpLayoutBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: SignUpLayoutBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SignUpLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val levels = arrayOf("First level", "Second level", "Third level")
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, levels)
        (binding.studentLevel.editText as? AutoCompleteTextView)?.setAdapter(adapter)

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
            if (password != confirmPassword) {
                binding.confirmPassInput.error = "Passwords not match"
                return@setOnClickListener
            }
            if (password.length < 6) {
                binding.passwordInput.error = "Password must be at least 6 characters"
                return@setOnClickListener
            }

            binding.signUpBtn.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid
                    if (uid == null) {
                        binding.signUpBtn.isEnabled = true
                        Toast.makeText(this, "Something went wrong, try again", Toast.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }

                    val userMap = mapOf(
                        "email" to email,
                        "role" to "student",
                        "name" to name,
                        "createdAt" to FieldValue.serverTimestamp(),
                    )
                    val studentMap = mapOf(
                        "major" to "",              // add a major field to your form if needed
                        "level" to studentLevel,
                        "unlockedVideoIds" to emptyList<String>()
                    )

                    val batch = db.batch()
                    batch.set(db.collection("users").document(uid), userMap)
                    batch.set(db.collection("students").document(uid), studentMap)

                    batch.commit()
                        .addOnSuccessListener {
                            Toast.makeText(this, "Sign up successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            // Auth account exists but Firestore write failed — clean up so they can retry cleanly
                            result.user?.delete()
                            binding.signUpBtn.isEnabled = true
                            Toast.makeText(this, "Failed to save profile: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    binding.signUpBtn.isEnabled = true
                    Toast.makeText(this, e.message ?: "Sign up failed", Toast.LENGTH_LONG).show()
                }
        }

        binding.emailInput.editText?.doOnTextChanged { text, _, _, _ ->
            binding.emailInput.error = if (text.isNullOrEmpty()) "Email is required" else null
        }

        binding.backToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish() // see note below
        }
    }
}