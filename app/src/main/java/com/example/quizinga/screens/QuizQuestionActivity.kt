package com.example.quizinga.screens

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.quizinga.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class QuizQuestionActivity : AppCompatActivity() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var questionTextView: TextView
    private lateinit var optionsRadioGroup: RadioGroup
    private lateinit var nextButton: Button
    private lateinit var progressBar: ProgressBar

    private var currentQuestionIndex = 0
    private var score = 0
    private lateinit var questions: List<QuizQuestion>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz_question)

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Reference Views
        questionTextView = findViewById(R.id.questionTextView)
        optionsRadioGroup = findViewById(R.id.optionsRadioGroup)
        nextButton = findViewById(R.id.nextButton)
        progressBar = findViewById(R.id.progressBar)

        // Get the selected category from the intent
        val category = intent.getStringExtra("category") ?: "Sports"

        // Fetch quiz questions from Firestore
        fetchQuizQuestions(category)
    }

    private fun fetchQuizQuestions(category: String) {
        firestore.collection("quizzes")
            .document(category)
            .collection("questions")
            .get()
            .addOnSuccessListener { documents ->
                questions = documents.map { document ->
                    QuizQuestion(
                        document.getString("questionText") ?: "",
                        document.getString("option1") ?: "",
                        document.getString("option2") ?: "",
                        document.getString("option3") ?: "",
                        document.getString("option4") ?: "",
                        document.getString("correctAnswer") ?: ""
                    )
                }
                // Display the first question
                displayQuestion(currentQuestionIndex)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load questions", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayQuestion(index: Int) {
        if (index < questions.size) {
            val question = questions[index]
            questionTextView.text = question.questionText
            findViewById<RadioButton>(R.id.option1).text = question.option1
            findViewById<RadioButton>(R.id.option2).text = question.option2
            findViewById<RadioButton>(R.id.option3).text = question.option3
            findViewById<RadioButton>(R.id.option4).text = question.option4

            // Update progress bar
            val progress = ((index + 1) * 100) / questions.size
            progressBar.progress = progress

            // Set OnClickListener for the Next Button
            nextButton.setOnClickListener {
                // Check the selected answer
                val selectedOptionId = optionsRadioGroup.checkedRadioButtonId
                if (selectedOptionId != -1) {
                    val selectedOption = findViewById<RadioButton>(selectedOptionId)
                    val selectedAnswer = when (selectedOptionId) {
                        R.id.option1 -> "option1"
                        R.id.option2 -> "option2"
                        R.id.option3 -> "option3"
                        R.id.option4 -> "option4"
                        else -> ""
                    }

                    // Log the selected answer and correct answer for debugging
                    println("Selected Answer: $selectedAnswer")
                    println("Correct Answer: ${question.correctAnswer}")

                    if (selectedAnswer == question.correctAnswer) {
                        score++
                        println("Score incremented to: $score")
                    }

                    // Move to the next question
                    currentQuestionIndex++
                    if (currentQuestionIndex < questions.size) {
                        displayQuestion(currentQuestionIndex)
                        optionsRadioGroup.clearCheck() // Clear selected option
                    } else {
                        // Quiz completed
                        saveScoreToFirestore()
                        Toast.makeText(this, "Quiz completed! Your score: $score/${questions.size}", Toast.LENGTH_LONG).show()
                        finish() // Close the activity
                    }
                } else {
                    Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveScoreToFirestore() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val scoreData = hashMapOf(
            "userId" to userId,
            "score" to score,
            "totalQuestions" to questions.size,
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("scores")
            .add(scoreData)
            .addOnSuccessListener {
                Toast.makeText(this, "Score saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save score", Toast.LENGTH_SHORT).show()
            }
    }
}
data class QuizQuestion(
    val questionText: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val option4: String,
    val correctAnswer: String
)