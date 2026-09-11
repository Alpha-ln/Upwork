package com.example.upwork.view.registration

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.widget.doOnTextChanged
import com.example.upwork.R
import com.example.upwork.databinding.LoginLayoutBinding
import com.example.upwork.view.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: LoginLayoutBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var logInPreferences: SharedPreferences
    private val prefName = "loginPref"

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                binding.googleBtn.isEnabled = true
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            // User cancelled the Google account picker
            binding.googleBtn.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LoginLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        logInPreferences = getSharedPreferences(prefName, 0) // Accesses a local XML file

        // Check if user is already logged in (Auto-login)
        if (auth.currentUser != null) {
            goHome()
            return
        }

        // Pre-fill email from SharedPreferences
        val savedEmail = logInPreferences.getString("email", "")
        if (!savedEmail.isNullOrEmpty()) {
            binding.emailInput.editText?.setText(savedEmail)
        }

        setupGoogleSignIn()

        // Get text when button is clicked
        binding.logInBtn.setOnClickListener {
            val email = binding.emailInput.editText?.text.toString().trim()
            val password = binding.passwordInput.editText?.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailInput.error = "Enter a valid email"
                return@setOnClickListener
            }

            binding.logInBtn.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    // Save email for next time
                    logInPreferences.edit {
                        putString("email", email)
                    }
                    goHome()
                }
                .addOnFailureListener { e ->
                    binding.logInBtn.isEnabled = true
                    Toast.makeText(this, e.message ?: "Login failed", Toast.LENGTH_LONG).show()
                }
        }

        // Respond to text changes in real-time
        binding.emailInput.editText?.doOnTextChanged { text, _, _, _ ->
            binding.emailInput.error = if (text.isNullOrEmpty()) "Email is required" else null
        }

        binding.signUptxt.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        binding.forgetPassTxt.setOnClickListener {
            val email = binding.emailInput.editText?.text?.toString()?.trim() ?: ""
            if (email.isEmpty()) {
                binding.emailInput.error = "Enter your email first"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.emailInput.error = "Enter a valid email"
                return@setOnClickListener
            }

            binding.forgetPassTxt.isEnabled = false
            auth.sendPasswordResetEmail(email)
                .addOnSuccessListener {
                    binding.forgetPassTxt.isEnabled = true
                    Toast.makeText(this, "Password reset email sent", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    binding.forgetPassTxt.isEnabled = true
                    Toast.makeText(this, e.message ?: "Failed to send reset email", Toast.LENGTH_LONG).show()
                }
        }

        binding.googleBtn.setOnClickListener {
            binding.googleBtn.isEnabled = false
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                val user = result.user
                if (user == null) {
                    binding.googleBtn.isEnabled = true
                    Toast.makeText(this, "Something went wrong, try again", Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val userRef = db.collection("users").document(user.uid)

                userRef.get()
                    .addOnSuccessListener { doc ->
                        if (!doc.exists()) {
                            // First-time Google login — create default student profile
                            val batch = db.batch()
                            batch.set(
                                userRef,
                                mapOf(
                                    "email" to user.email,
                                    "role" to "student",
                                    "name" to (user.displayName ?: ""),
                                    "createdAt" to FieldValue.serverTimestamp(),
                                ),
                            )
                            batch.set(
                                db.collection("students").document(user.uid),
                                mapOf(
                                    "major" to "",
                                    "level" to "",
                                    "unlockedVideoIds" to emptyList<String>(),
                                ),
                            )
                            batch.commit()
                                .addOnSuccessListener { goHome() }
                                .addOnFailureListener { e ->
                                    binding.googleBtn.isEnabled = true
                                    Toast.makeText(this, "Setup failed: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            goHome()
                        }
                    }
                    .addOnFailureListener { e ->
                        binding.googleBtn.isEnabled = true
                        Toast.makeText(this, "Failed to load profile: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                binding.googleBtn.isEnabled = true
                Toast.makeText(this, e.message ?: "Google authentication failed", Toast.LENGTH_SHORT).show()
            }
    }

    private fun goHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }


}