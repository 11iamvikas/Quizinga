package com.example.quizinga.screens

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.quizinga.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var bottomNavigationView: BottomNavigationView

    private lateinit var profileImageView: ImageView
    private lateinit var nameTextView: TextView
    private lateinit var historyRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Reference Views
        profileImageView = findViewById(R.id.profileImageView)
        nameTextView = findViewById(R.id.nameTextView)
        historyRecyclerView = findViewById(R.id.historyRecyclerView)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Set up Bottom Navigation
        setupBottomNavigation()

        // Set up RecyclerView
        historyRecyclerView.layoutManager = LinearLayoutManager(this)

        // Fetch and display data
        fetchUserProfile()
        fetchQuizHistory()
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
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                R.id.history -> {
                    // Already on History Screen, do nothing
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

        // Highlight the active tab
        bottomNavigationView.selectedItemId = R.id.history
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
                        nameTextView.text = name
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
                    // Load the profile picture into ImageView using Glide
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

    private fun fetchQuizHistory() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("scores")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    val quizHistoryList = mutableListOf<QuizHistory>()
                    for (document in documents) {
                        val category = document.getString("category") ?: "Unknown"
                        val score = document.getLong("score")?.toInt() ?: 0
                        val totalQuestions = document.getLong("totalQuestions")?.toInt() ?: 0
                        val timestamp = document.getLong("timestamp") ?: 0

                        quizHistoryList.add(QuizHistory(category, score, totalQuestions, timestamp))
                    }

                    // Set up RecyclerView adapter
                    val adapter = HistoryAdapter(quizHistoryList)
                    historyRecyclerView.adapter = adapter
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to fetch quiz history: ${exception.message}", Toast.LENGTH_LONG).show()
                }
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }
}
data class QuizHistory(
    val category: String, // Quiz category (e.g., Sports, Entertainment)
    val score: Int, // User's score
    val totalQuestions: Int, // Total questions in the quiz
    val timestamp: Long // Timestamp of the quiz attempt
)
class HistoryAdapter(private val quizHistoryList: List<QuizHistory>) :
    RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val quizHistory = quizHistoryList[position]
        holder.bind(quizHistory)
    }

    override fun getItemCount(): Int {
        return quizHistoryList.size
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val quizCategoryTextView: TextView = itemView.findViewById(R.id.quizCategoryTextView)
        private val quizScoreTextView: TextView = itemView.findViewById(R.id.quizScoreTextView)
        private val quizDateTextView: TextView = itemView.findViewById(R.id.quizDateTextView)

        fun bind(quizHistory: QuizHistory) {
            quizCategoryTextView.text = quizHistory.category
            quizScoreTextView.text = "Score: ${quizHistory.score}/${quizHistory.totalQuestions}"

            // Format the timestamp to a readable date
            val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
            val date = Date(quizHistory.timestamp)
            quizDateTextView.text = dateFormat.format(date)
        }
    }
}