package com.example.quizinga.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.quizinga.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var genderTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var logoutButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Reference Views
        profileImageView = findViewById(R.id.profileImageView)
        nameTextView = findViewById(R.id.nameTextView)
        genderTextView = findViewById(R.id.genderTextView)
        phoneTextView = findViewById(R.id.phoneTextView)
        logoutButton = findViewById(R.id.logoutButton)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Fetch and display user profile data
        fetchUserProfile()

        // Set up Bottom Navigation
        setupBottomNavigation()

        // Logout Button Click Listener
        logoutButton.setOnClickListener {
            logoutUser()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                    true
                }
                R.id.profile -> {
                    // Already on Profile Screen, do nothing
                    true
                }
                R.id.history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                    true
                }
                R.id.community -> {
                    startActivity(Intent(this, CommunityActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }

        // Highlight the "Profile" tab as active
        bottomNavigationView.selectedItemId = R.id.profile
    }

    private fun fetchUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            // Fetch profile details from Firestore
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "N/A"
                        val gender = document.getString("gender") ?: "N/A"
                        val phone = document.getString("phone") ?: "N/A"

                        // Update TextViews
                        nameTextView.text = name
                        genderTextView.text = gender
                        phoneTextView.text = phone
                    } else {
                        Toast.makeText(this, "Profile data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to fetch profile data: ${exception.message}", Toast.LENGTH_LONG).show()
                }

            // Fetch profile picture from Firebase Storage
            val storageRef = storage.reference.child("profile_images/$userId.jpg")
            storageRef.downloadUrl
                .addOnSuccessListener { uri ->
                    Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.ic_profile_placeholder)
                        .error(R.drawable.ic_error_placeholder)
                        .into(profileImageView)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to load profile picture: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logoutUser() {
        auth.signOut()
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        // Redirect to Login Activity
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}