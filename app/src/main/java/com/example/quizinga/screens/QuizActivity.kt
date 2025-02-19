package com.example.quizinga.screens

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.quizinga.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class QuizActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        // Initialize Bottom Navigation
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        setupBottomNavigation()

        // Reference the Choose Category Button
        val chooseCategoryButton: Button = findViewById(R.id.chooseCategoryButton)

        // Set OnClickListener for the Choose Category Button
        chooseCategoryButton.setOnClickListener {
            showCategorySelectionDialog()
        }
    }

    private fun setupBottomNavigation() {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    // Navigate to HomeActivity
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
                    startActivity(Intent(this, CommunityActivity::class.java))
                    finish()
                    true
                }
                else -> false
            }
        }
    }

    private fun showCategorySelectionDialog() {
        // Create a Dialog
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_quiz_category)

        // Reference Buttons in the Dialog
        val sportsButton: Button = dialog.findViewById(R.id.sportsButton)
        val entertainmentButton: Button = dialog.findViewById(R.id.entertainmentButton)
        val politicsButton: Button = dialog.findViewById(R.id.politicsButton)
        val historyButton: Button = dialog.findViewById(R.id.historyButton)

        // Set OnClickListeners for Each Category Button
        sportsButton.setOnClickListener {
            startQuizActivity("Sports")
            dialog.dismiss()
        }

        entertainmentButton.setOnClickListener {
            startQuizActivity("Entertainment")
            dialog.dismiss()
        }

        politicsButton.setOnClickListener {
            startQuizActivity("Politics")
            dialog.dismiss()
        }

        historyButton.setOnClickListener {
            startQuizActivity("History")
            dialog.dismiss()
        }

        // Show the Dialog
        dialog.show()
    }

    private fun startQuizActivity(category: String) {
        // Start the QuizQuestionActivity with the selected category
        val intent = Intent(this, QuizQuestionActivity::class.java)
        intent.putExtra("category", category)
        startActivity(intent)
    }
}