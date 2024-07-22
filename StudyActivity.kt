package com.example.project

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class StudyActivity : AppCompatActivity() {

    private lateinit var flashcardContent: TextView
    private lateinit var flashcardDefinition: TextView
    private lateinit var flashcardImage: ImageView
    private lateinit var btnKnow: Button
    private lateinit var btnDontKnow: Button

    private var flashcards: MutableList<Flashcard> = mutableListOf()
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)

        flashcardContent = findViewById(R.id.flashcardContent)
        flashcardDefinition = findViewById(R.id.flashcardDefinition)
        flashcardImage = findViewById(R.id.flashcardImage)
        btnKnow = findViewById(R.id.btnKnow)
        btnDontKnow = findViewById(R.id.btnDontKnow)

        loadFlashcards()

        btnKnow.setOnClickListener {
            markAsKnown()
            showNextFlashcard()
        }

        btnDontKnow.setOnClickListener {
            showNextFlashcard()
        }
    }

    private fun loadFlashcards() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = FirebaseFirestore.getInstance()
                val result = db.collection("flashcards").get().await()
                for (document in result.documents) {
                    val flashcard = document.toObject(Flashcard::class.java)
                    flashcard?.let { flashcards.add(it) }
                }
                flashcards.shuffle()

                withContext(Dispatchers.Main) {
                    if (flashcards.isNotEmpty()) {
                        showFlashcard(flashcards[currentIndex])
                    } else {
                        Toast.makeText(this@StudyActivity, "There are no availble cards", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@StudyActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showFlashcard(flashcard: Flashcard) {
        flashcardContent.text = flashcard.content
        flashcardDefinition.text = flashcard.definition

        if (!flashcard.imageUrl.isNullOrEmpty()) {
            flashcardImage.visibility = ImageView.VISIBLE
            Glide.with(this)
                .load(flashcard.imageUrl)
                .into(flashcardImage)
        } else {
            flashcardImage.visibility = ImageView.GONE
        }
    }

    private fun showNextFlashcard() {
        currentIndex++
        if (currentIndex >= flashcards.size) {
            currentIndex = 0
        }
        showFlashcard(flashcards[currentIndex])
    }

    private fun markAsKnown() {
        flashcards.removeAt(currentIndex)
        if (flashcards.isEmpty()) {
            Toast.makeText(this, "Well done!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            if (currentIndex >= flashcards.size) {
                currentIndex = 0
            }
        }
    }
}
