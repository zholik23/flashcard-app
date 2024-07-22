package com.example.project

import android.graphics.Typeface
import android.net.Uri
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class FlashcardAdapter(
    private val flashcards: MutableList<Flashcard>,
    private val currentUserId: String,
    private val onEditClick: (Flashcard) -> Unit,
    private val onDeleteClick: (Flashcard) -> Unit
) : RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

    private val startIndicesMap = mutableMapOf<String, Int>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_flashcard, parent, false)
        return FlashcardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        try {
            val flashcard = flashcards[position]

            holder.textViewQuestion.text = applySpans(flashcard.content, flashcard.spanDataContent)
            holder.textViewAnswer.text = applySpans(flashcard.definition, flashcard.spanDataDefinition)

            updateStartIndices(flashcard.content, flashcard.spanDataContent)
            updateStartIndices(flashcard.definition, flashcard.spanDataDefinition)
            if (!flashcard.imageUrl.isNullOrEmpty()) {
                holder.imageViewFlashcard.visibility = View.VISIBLE
                Glide.with(holder.imageViewFlashcard.context)
                    .load(Uri.parse(flashcard.imageUrl))
                    .into(holder.imageViewFlashcard)
            } else {
                holder.imageViewFlashcard.visibility = View.GONE
            }

            if (flashcard.creatorId == currentUserId) {
                holder.buttonEdit.visibility = View.VISIBLE
                holder.buttonDelete.visibility = View.VISIBLE

                holder.buttonEdit.setOnClickListener { onEditClick(flashcard) }
                holder.buttonDelete.setOnClickListener { onDeleteClick(flashcard) }
            } else {
                holder.buttonEdit.visibility = View.GONE
                holder.buttonDelete.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e("FlashcardAdapter", "Error in onBindViewHolder at position $position: ${e.message}")
        }
    }

    private fun updateStartIndices(text: String, spans: List<SpanData>?) {
        spans?.forEach { span ->
            val type = span.type
            if (!startIndicesMap.containsKey(type)) {
                startIndicesMap[type] = span.start
            }
        }
    }

    override fun getItemCount(): Int = flashcards.size

    private fun applySpans(text: String, spans: List<SpanData>?): SpannableString {
        val spannable = SpannableString(text)
        spans?.forEach { span ->
            val type = span.type
            val startIndex = startIndicesMap[type] ?: 0
            val start = startIndex + span.start
            val end = startIndex + span.end

            when (span.type) {
                "bold" -> spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                "italic" -> spannable.setSpan(StyleSpan(Typeface.ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                "bold_italic" -> spannable.setSpan(StyleSpan(Typeface.BOLD_ITALIC), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                "underline" -> spannable.setSpan(UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                "color" -> {
                    val color = span.color ?: return@forEach
                    spannable.setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                }
            }
        }
        return spannable
    }


    class FlashcardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageViewFlashcard: ImageView = itemView.findViewById(R.id.imageViewFlashcard)
        val textViewQuestion: TextView = itemView.findViewById(R.id.textViewQuestion)
        val textViewAnswer: TextView = itemView.findViewById(R.id.textViewAnswer)
        val buttonEdit: Button = itemView.findViewById(R.id.buttonEdit)
        val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)
    }

    fun removeFlashcard(flashcard: Flashcard) {
        val position = flashcards.indexOf(flashcard)
        if (position != -1) {
            flashcards.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
