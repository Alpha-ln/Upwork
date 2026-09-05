package com.example.upwork.view.registration

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.upwork.databinding.SignUpLayoutBinding
import com.example.upwork.model.Instructor
import com.example.upwork.model.Student
import com.example.upwork.model.User
import com.example.upwork.view.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpActivity: AppCompatActivity() {
    private lateinit var binding: SignUpLayoutBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SignUpLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Get text when button is clicked
        binding.signUpBtn.setOnClickListener {
            val email = binding.emailInput.editText?.text.toString().trim()
            val password = binding.passwordInput.editText?.text.toString().trim()
            val confirmPassword = binding.confirmPassInput.editText?.text.toString().trim()
            val name = binding.nameInput.editText?.text.toString().trim()
            val role = if (binding.instructorRadio.isChecked) "instructor" else "student"

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
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
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    val user = User(
                        uid = uid,
                        name = name,
                        email = email,
                        role = role
                    )
                    FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(uid)
                        .set(user)
                        .addOnSuccessListener {
                            // ALSO save to Students collection if the role is student
                            if (role == "student") {
                                val studentData = Student(
                                    studentId = uid,
                                    studentName = name,
                                    major = "General"
                                )
                                FirebaseFirestore.getInstance()
                                    .collection("Students")
                                    .document(uid)
                                    .set(studentData)
                                Toast.makeText(this, "Account created successfully.", Toast.LENGTH_LONG).show()
                                startActivity(Intent(this, HomeActivity::class.java))
                                finish()
                            } else if (role == "instructor") {
                                // Create a pending instructor profile
                                val instructorData = Instructor(
                                    instructorId = uid,
                                    name = name,
                                    speciality = "Pending Approval",
                                    imageUrl = "https://cdn-icons-png.flaticon.com/512/3135/3135715.png", // Placeholder
                                    verified = false
                                )
                                FirebaseFirestore.getInstance()
                                    .collection("Instructors")
                                    .document(uid)
                                    .set(instructorData)
                                Toast.makeText(this, "Account created! Waiting for admin approval.", Toast.LENGTH_LONG).show()
                                finish()
                            }

                        }
                        .addOnFailureListener { e ->
                            binding.signUpBtn.isEnabled = true
                            Toast.makeText(this, "Database error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
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