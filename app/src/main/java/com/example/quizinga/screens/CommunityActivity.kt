package com.example.quizinga.screens

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quizinga.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class CommunityActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var bottomNavigationView: BottomNavigationView

    // Input fields
    private lateinit var questionEditText: TextInputEditText
    private lateinit var option1EditText: TextInputEditText
    private lateinit var option2EditText: TextInputEditText
    private lateinit var option3EditText: TextInputEditText
    private lateinit var option4EditText: TextInputEditText
    private lateinit var correctAnswerEditText: TextInputEditText
    private lateinit var contributeButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Reference views
        questionEditText = findViewById(R.id.questionEditText)
        option1EditText = findViewById(R.id.option1EditText)
        option2EditText = findViewById(R.id.option2EditText)
        option3EditText = findViewById(R.id.option3EditText)
        option4EditText = findViewById(R.id.option4EditText)
        correctAnswerEditText = findViewById(R.id.correctAnswerEditText)
        contributeButton = findViewById(R.id.contributeButton)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Set up Bottom Navigation
        setupBottomNavigation()

        // Handle contribute button click
        contributeButton.setOnClickListener {
            uploadContribution()
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
                    startActivity(Intent(this, ProfileActivity::class.java))
                    finish()
                    true
                }
                R.id.history -> {
                    startActivity(Intent(this, HistoryActivity::class.java))
                    finish()
                    true
                }
                R.id.community -> {
                    // Already on Community Screen, do nothing
                    true
                }
                else -> false
            }
        }

        // Highlight the active tab
        bottomNavigationView.selectedItemId = R.id.community
    }

    private fun uploadContribution() {
        // Get input values
        val question = questionEditText.text.toString().trim()
        val option1 = option1EditText.text.toString().trim()
        val option2 = option2EditText.text.toString().trim()
        val option3 = option3EditText.text.toString().trim()
        val option4 = option4EditText.text.toString().trim()
        val correctAnswer = correctAnswerEditText.text.toString().trim()

        // Validate inputs
        if (question.isEmpty() || option1.isEmpty() || option2.isEmpty() || option3.isEmpty() || option4.isEmpty() || correctAnswer.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a contribution object
        val contribution = hashMapOf(
            "question" to question,
            "option1" to option1,
            "option2" to option2,
            "option3" to option3,
            "option4" to option4,
            "correctAnswer" to correctAnswer,
            "userId" to auth.currentUser?.uid,
            "timestamp" to Calendar.getInstance().timeInMillis
        )

        // Upload to Firestore
        firestore.collection("contributions")
            .add(contribution)
            .addOnSuccessListener {
                Toast.makeText(this, "Thank you for your contribution!", Toast.LENGTH_SHORT).show()
                clearInputs()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to submit: ${exception.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun clearInputs() {
        questionEditText.text?.clear()
        option1EditText.text?.clear()
        option2EditText.text?.clear()
        option3EditText.text?.clear()
        option4EditText.text?.clear()
        correctAnswerEditText.text?.clear()
    }
}