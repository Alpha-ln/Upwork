package com.example.upwork.view

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
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

        // Setup Navigation components
        navView.setupWithNavController(navController)
        bottomNavView.setupWithNavController(navController)

        // HIDE special menus by default before checking role
        navView.menu.findItem(R.id.codeGeneratingItem)?.isVisible = false
        navView.menu.findItem(R.id.adminPanelItem)?.isVisible = false

        // Update Nav Header with user info
        val headerView = navView.getHeaderView(0)
        val userEmailTextView: android.widget.TextView = headerView.findViewById(R.id.userEmail)
        val userNameTextView: android.widget.TextView = headerView.findViewById(R.id.userName)

        val currentUser = FirebaseAuth.getInstance().currentUser


        if (currentUser != null) {
            userEmailTextView.text = currentUser.email
            userNameTextView.text = currentUser.displayName ?: "Student"

        } else {
            userEmailTextView.text = "student@Upwork.com"
            userNameTextView.text = "student user"
        }
        // Professional Handle Sign Out
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.signOutItem -> {
                    signOut()
                    true
                }
                else -> {
                    // Handle other navigation items
                    val handled = androidx.navigation.ui.NavigationUI.onNavDestinationSelected(item, navController)
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

        // Redirect to Login
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}