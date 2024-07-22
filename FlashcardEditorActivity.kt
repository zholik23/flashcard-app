package com.example.project

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*

class FlashcardEditorActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var editTextContent: EditText
    private lateinit var editTextDefinition: EditText
    private lateinit var btnBold: ImageButton
    private lateinit var btnItalic: ImageButton
    private lateinit var btnUnderline: ImageButton
    private lateinit var btnTextColor: ImageButton
    private lateinit var btnAddImage: ImageButton
    private lateinit var btnSave: Button

    private var selectedImageUri: Uri? = null
    private var flashcardId: String? = null

    private var lastClickTime: Long = 0

    companion object {
        const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard_editor)

        editTextContent = findViewById(R.id.editTextContent)
        editTextDefinition = findViewById(R.id.editTextDefinition)
        btnBold = findViewById(R.id.btnBold)
        btnItalic = findViewById(R.id.btnItalic)
        btnUnderline = findViewById(R.id.btnUnderline)
        btnTextColor = findViewById(R.id.btnTextColor)
        btnAddImage = findViewById(R.id.btnAddImage)
        btnSave = findViewById(R.id.btnSave)

        btnBold.setOnClickListener(this)
        btnItalic.setOnClickListener(this)
        btnUnderline.setOnClickListener(this)
        btnTextColor.setOnClickListener(this)
        btnAddImage.setOnClickListener(this)
        btnSave.setOnClickListener(this)

        flashcardId = intent.getStringExtra("flashcard_id")
        if (flashcardId != null) {
            loadFlashcardData(flashcardId!!)
        }
    }

    private fun loadFlashcardData(flashcardId: String) {
        val db = FirebaseFirestore.getInstance()
        db.collection("flashcards").document(flashcardId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val flashcard = document.toObject(Flashcard::class.java)
                    if (flashcard != null) {
                        editTextContent.setText(flashcard.content)
                        editTextDefinition.setText(flashcard.definition)
                        selectedImageUri = flashcard.imageUrl?.let { Uri.parse(it) }
                        applySpanData(editTextContent, flashcard.spanDataContent)
                        applySpanData(editTextDefinition, flashcard.spanDataDefinition)
                    }
                } else {
                    Toast.makeText(this, "Flashcard not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load flashcard data", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun applySpanData(editText: EditText, spanData: List<SpanData>?) {
        val spannable = SpannableStringBuilder(editText.text)
        spanData?.forEach { span ->
            when (span.type) {
                "bold" -> spannable.setSpan(StyleSpan(Typeface.BOLD), span.start, span.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                "italic" -> spannable.setSpan(StyleSpan(Typeface.ITALIC), span.start, span.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                "bold_italic" -> spannable.setSpan(StyleSpan(Typeface.BOLD_ITALIC), span.start, span.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                "underline" -> spannable.setSpan(UnderlineSpan(), span.start, span.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                "color" -> {
                    val color = span.color ?: return@forEach
                    spannable.setSpan(ForegroundColorSpan(color), span.start, span.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        editText.setText(spannable)
    }

    override fun onClick(view: View) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < 500) {
            return
        }
        lastClickTime = currentTime

        when (view.id) {
            R.id.btnBold -> toggleTextStyle(Typeface.BOLD)
            R.id.btnItalic -> toggleTextStyle(Typeface.ITALIC)
            R.id.btnUnderline -> toggleUnderline()
            R.id.btnTextColor -> changeTextColor()
            R.id.btnAddImage -> addImage()
            R.id.btnSave -> saveContent()
        }
    }

    private fun toggleTextStyle(style: Int) {
        val editText: EditText = if (editTextContent.hasFocus()) {
            editTextContent
        } else if (editTextDefinition.hasFocus()) {
            editTextDefinition
        } else {
            return
        }

        val selectionStart = editText.selectionStart
        val selectionEnd = editText.selectionEnd
        val spannable = SpannableStringBuilder(editText.text)

        val existingSpans = spannable.getSpans(selectionStart, selectionEnd, StyleSpan::class.java)
        var styleAlreadyApplied = false

        for (span in existingSpans) {
            if (span.style and style != 0) {
                styleAlreadyApplied = true
                break
            }
        }

        if (styleAlreadyApplied) {
            for (span in existingSpans) {
                if (span.style and style != 0) {
                    spannable.removeSpan(span)
                }
            }
        } else {
            spannable.setSpan(StyleSpan(style), selectionStart, selectionEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        editText.setText(spannable)
        editText.setSelection(selectionStart, selectionEnd)
    }

    private fun changeTextColor() {
        val editText: EditText = if (editTextContent.hasFocus()) {
            editTextContent
        } else if (editTextDefinition.hasFocus()) {
            editTextDefinition
        } else {
            return
        }

        val start = editText.selectionStart
        val end = editText.selectionEnd

        if (start == end) {
            return
        }

        val textColor = Color.RED
        val spannable = SpannableStringBuilder(editText.text)

        spannable.setSpan(ForegroundColorSpan(textColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        editText.text = spannable
    }

    private fun toggleUnderline() {
        val editText: EditText = if (editTextContent.hasFocus()) {
            editTextContent
        } else if (editTextDefinition.hasFocus()) {
            editTextDefinition
        } else {
            return
        }

        val start = editText.selectionStart
        val end = editText.selectionEnd

        val spannableStr = SpannableStringBuilder(editText.text)

        val underLineSpans = spannableStr.getSpans(start, end, UnderlineSpan::class.java)
        if (underLineSpans.isEmpty()) {
            spannableStr.setSpan(UnderlineSpan(), start, end, 0)
        } else {
            for (underlineSpan in underLineSpans) {
                spannableStr.removeSpan(underlineSpan)
            }
        }

        editText.text = spannableStr
        editText.setSelection(start, end)
    }

    private fun addImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun convertTextToSpanData(spannable: Spannable): List<SpanData> {
        val spanDataList = mutableListOf<SpanData>()

        val styleSpans = spannable.getSpans(0, spannable.length, StyleSpan::class.java)
        styleSpans.forEach { span ->
            val styleType = when (span.style) {
                Typeface.BOLD -> "bold"
                Typeface.ITALIC -> "italic"
                Typeface.BOLD_ITALIC -> "bold_italic"
                else -> "normal"
            }
            spanDataList.add(SpanData(styleType, spannable.getSpanStart(span), spannable.getSpanEnd(span)))
        }

        val colorSpans = spannable.getSpans(0, spannable.length, ForegroundColorSpan::class.java)
        colorSpans.forEach { span ->
            spanDataList.add(SpanData("color", spannable.getSpanStart(span), spannable.getSpanEnd(span), span.foregroundColor))
        }

        val underlineSpans = spannable.getSpans(0, spannable.length, UnderlineSpan::class.java)
        underlineSpans.forEach { span ->
            spanDataList.add(SpanData("underline", spannable.getSpanStart(span), spannable.getSpanEnd(span)))
        }

        return spanDataList
    }

    private fun saveContent() {
        val content = editTextContent.text
        val definition = editTextDefinition.text

        val spanDataContent = convertTextToSpanData(content as Spannable)
        val spanDataDefinition = convertTextToSpanData(definition as Spannable)

        if (selectedImageUri != null) {
            if (flashcardId != null) {
                uploadImageToFirebaseStorage(
                    content.toString(),
                    definition.toString(),
                    spanDataContent,
                    spanDataDefinition,
                    isUpdate = true
                )
            } else {
                uploadImageToFirebaseStorage(
                    content.toString(),
                    definition.toString(),
                    spanDataContent,
                    spanDataDefinition,
                    isUpdate = false
                )
            }
        } else {
            Toast.makeText(this, "Image must be selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebaseStorage(
        content: String,
        definition: String,
        spanDataContent: List<SpanData>,
        spanDataDefinition: List<SpanData>,
        isUpdate: Boolean
    ) {
        selectedImageUri?.let { imageUri ->
            val storageRef = FirebaseStorage.getInstance().reference
            val fileName = "images/${UUID.randomUUID()}.jpg"
            val imageRef = storageRef.child(fileName)

            imageRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        val downloadUrl = uri.toString()
                        Toast.makeText(this, "Image uploaded successfully: $downloadUrl", Toast.LENGTH_SHORT).show()
                        if (isUpdate) {
                            updateFlashcard(
                                flashcardId!!,
                                content,
                                definition,
                                spanDataContent,
                                spanDataDefinition,
                                FirebaseAuth.getInstance().currentUser?.uid ?: "",
                                downloadUrl
                            )
                        } else {
                            addNewFlashcard(content, definition, downloadUrl, spanDataContent, spanDataDefinition)
                        }
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to upload image: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateFlashcard(
        flashcardId: String,
        content: String,
        definition: String,
        spanDataContent: List<SpanData>,
        spanDataDefinition: List<SpanData>,
        creatorId: String,
        imageUrl: String?
    ) {
        if (imageUrl == null) {
            Toast.makeText(this, "Image must be selected", Toast.LENGTH_SHORT).show()
            return
        }

        val db = FirebaseFirestore.getInstance()

        val flashcardData = hashMapOf(
            "content" to content,
            "definition" to definition,
            "spanDataContent" to spanDataContent,
            "spanDataDefinition" to spanDataDefinition,
            "creatorId" to creatorId,
            "imageUrl" to imageUrl
        )

        db.collection("flashcards").document(flashcardId)
            .set(flashcardData)
            .addOnSuccessListener {
                Toast.makeText(this, "Flashcard updated successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update flashcard: $e", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addNewFlashcard(
        content: String,
        definition: String,
        imageUrl: String,
        spanDataContent: List<SpanData>,
        spanDataDefinition: List<SpanData>
    ) {
        val db = FirebaseFirestore.getInstance()
        val creatorId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val flashcardData = hashMapOf(
            "content" to content,
            "definition" to definition,
            "imageUrl" to imageUrl,
            "spanDataContent" to spanDataContent,
            "spanDataDefinition" to spanDataDefinition,
            "creatorId" to creatorId
        )

        db.collection("flashcards")
            .add(flashcardData)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "Flashcard saved successfully", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save flashcard: $e", Toast.LENGTH_SHORT).show()
            }
    }
}
