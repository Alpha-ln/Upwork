package com.example.upwork.view

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.upwork.R
import com.example.upwork.view.registration.LoginActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_home_view)
        val bottomNavView: BottomNavigationView = findViewById(R.id.bottomNavigation)

        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeItem, R.id.historyItem, R.id.profileItem),
            drawerLayout
        )

        navView.setupWithNavController(navController)
        bottomNavView.setupWithNavController(navController)

        // HIDE special menus by default before checking role
        navView.menu.findItem(R.id.codeGeneratingItem)?.isVisible = false
        navView.menu.findItem(R.id.profileItem)?.isVisible = false
        bottomNavView.menu.findItem(R.id.profileItem)?.isVisible = false

        val headerView = navView.getHeaderView(0)
        val userEmailTextView: TextView = headerView.findViewById(R.id.userEmail)
        val userNameTextView: TextView = headerView.findViewById(R.id.userName)

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            userEmailTextView.text = currentUser.email

            // Pull name + role from Firestore, not FirebaseAuth
            // (displayName is only ever set for Google sign-in, so email/password users show blank otherwise)
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name")
                    val role = doc.getString("role") ?: "student"

                    userNameTextView.text = if (!name.isNullOrEmpty()) name else getString(R.string.student)

                    // Reveal role-gated menu items only after confirming role
                    val isInstructorOrAdmin = role == "instructor" || role == "admin"
                    val isAdmin = role == "admin"

                    navView.menu.findItem(R.id.codeGeneratingItem)?.isVisible = isInstructorOrAdmin
                    navView.menu.findItem(R.id.profileItem)?.isVisible = isInstructorOrAdmin
                    
                    bottomNavView.menu.findItem(R.id.profileItem)?.isVisible = isInstructorOrAdmin
                }
                .addOnFailureListener {
                    userNameTextView.text = getString(R.string.student)
                    // Menu items stay hidden — fail closed, not open
                }
        } else {
            userEmailTextView.text = getString(R.string.default_student_email)
            userNameTextView.text = getString(R.string.student_user)
        }

        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.signOutItem -> {
                    signOut()
                    true
                }
                else -> {
                    val handled = NavigationUI.onNavDestinationSelected(item, navController)
                    if (handled) {
                        drawerLayout.closeDrawers()
                    }
                    handled
                }
            }
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}