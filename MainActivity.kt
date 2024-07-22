package com.example.project

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mmenu)

        val viewFlashcardsButton: Button = findViewById(R.id.buttonViewFlashcards)
        val createFlashcardsButton: Button = findViewById(R.id.buttonCreateFlashcards)

        viewFlashcardsButton.setOnClickListener {
            val intent = Intent(this, FlashcardViewerActivity::class.java)
            startActivity(intent)
        }

        createFlashcardsButton.setOnClickListener {
            val intent = Intent(this, FlashcardEditorActivity::class.java)
            startActivity(intent)
        }
    }
}

