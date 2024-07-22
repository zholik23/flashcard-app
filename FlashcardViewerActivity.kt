package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FlashcardViewerActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: FlashcardAdapter
    private lateinit var currentUserId: String
    private lateinit var studybtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard_viewer)

        recyclerView = findViewById(R.id.recyclerViewFlashcards)
        recyclerView.layoutManager = LinearLayoutManager(this)
        studybtn=findViewById(R.id.btnStudy)


        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        loadFlashcardsFromFirestore()
        studybtn.setOnClickListener {

            val intent = Intent(this, StudyActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loadFlashcardsFromFirestore() {
        val db = FirebaseFirestore.getInstance()
        db.collection("flashcards").get()
            .addOnSuccessListener { result ->
                val flashcards = mutableListOf<Flashcard>()
                for (document in result) {
                    val flashcard = document.toObject(Flashcard::class.java)
                    flashcard.id = document.id
                    flashcards.add(flashcard)
                }
                adapter = FlashcardAdapter(flashcards, currentUserId, ::editFlashcard, ::deleteFlashcard)
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error loading flashcards: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun editFlashcard(flashcard: Flashcard) {
        Toast.makeText(this, "Editing flashcard: ${flashcard.content}", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, FlashcardEditorActivity::class.java)
        intent.putExtra("flashcard_id", flashcard.id)
        intent.putExtra("content", flashcard.content)
        intent.putExtra("definition", flashcard.definition)
        startActivity(intent)
    }

    private fun deleteFlashcard(flashcard: Flashcard) {
        val db = FirebaseFirestore.getInstance()
        db.collection("flashcards").document(flashcard.id)  // Assume flashcard has an `id` field
            .delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Flashcard deleted", Toast.LENGTH_SHORT).show()
                adapter.removeFlashcard(flashcard)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to delete flashcard: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

}
