package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DeliveryArticleEntity
import com.example.data.DeliveryNoteWithArticles
import com.example.data.DepotRepository
import com.example.data.JournalEntryEntity
import com.example.data.RestockNoteEntity
import com.example.data.UndoAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FeedbackMessage(
    val id: Long = System.nanoTime(),
    val text: String,
    val undoAction: UndoAction? = null
)

class DepotViewModel(
    private val repository: DepotRepository
) : ViewModel() {

    val deliveryNotes: StateFlow<List<DeliveryNoteWithArticles>> =
        repository.observePendingDeliveryNotes()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val restockNotes: StateFlow<List<RestockNoteEntity>> =
        repository.observePendingRestockNotes()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val journalEntries: StateFlow<List<JournalEntryEntity>> =
        repository.observeJournalEntries()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private val _feedback = MutableStateFlow<FeedbackMessage?>(null)
    val feedback: StateFlow<FeedbackMessage?> = _feedback.asStateFlow()

    fun consumeFeedback() {
        _feedback.value = null
    }

    fun saveDeliveryNote(
        noteId: Long?,
        clientName: String,
        clientPhone: String,
        clientAddress: String,
        waitingSinceMs: Long,
        urgent: Boolean,
        articles: List<DeliveryArticleEntity>
    ) {
        viewModelScope.launch {
            runCatching {
                if (noteId == null) {
                    repository.createDeliveryNote(
                        clientName = clientName,
                        clientPhone = clientPhone,
                        clientAddress = clientAddress,
                        waitingSinceMs = waitingSinceMs,
                        urgent = urgent,
                        articleTexts = articles.map { it.text }
                    )
                } else {
                    repository.updateDeliveryNote(
                        noteId = noteId,
                        clientName = clientName,
                        clientPhone = clientPhone,
                        clientAddress = clientAddress,
                        waitingSinceMs = waitingSinceMs,
                        urgent = urgent,
                        articles = articles
                    )
                }
            }.onSuccess {
                _feedback.value = FeedbackMessage(
                    text = if (noteId == null) "Note client enregistrée" else "Note mise à jour"
                )
            }.onFailure { err ->
                _feedback.value = FeedbackMessage(text = err.message ?: "Erreur d'enregistrement")
            }
        }
    }

    fun addArticleToExistingNote(noteId: Long, articleText: String) {
        viewModelScope.launch {
            runCatching {
                repository.addArticleToNote(noteId, articleText)
            }.onSuccess {
                _feedback.value = FeedbackMessage(text = "Article ajouté à la note")
            }.onFailure { err ->
                _feedback.value = FeedbackMessage(text = err.message ?: "Impossible d'ajouter l'article")
            }
        }
    }

    fun updateNoteWaitingInfo(noteId: Long, waitingSinceMs: Long, urgent: Boolean) {
        viewModelScope.launch {
            runCatching {
                repository.updateWaitingInfo(noteId, waitingSinceMs, urgent)
            }.onSuccess {
                _feedback.value = FeedbackMessage(text = "Date d'attente mise à jour")
            }
        }
    }

    fun deleteDeliveryNote(noteId: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteDeliveryNote(noteId) }
                .onSuccess { _feedback.value = FeedbackMessage(text = "Note supprimée") }
        }
    }

    fun checkOffDeliveryArticle(noteWithArticles: DeliveryNoteWithArticles, article: DeliveryArticleEntity) {
        viewModelScope.launch {
            runCatching {
                repository.markArticleDelivered(noteWithArticles.note, article)
            }.onSuccess { undo ->
                val remaining = (noteWithArticles.pendingCount - 1).coerceAtLeast(0)
                val msg = if (remaining == 0) {
                    "Tout livré · Note terminée et ajoutée au Journal"
                } else {
                    "Livré (${article.text}) · Reste $remaining article${if (remaining > 1) "s" else ""}"
                }
                _feedback.value = FeedbackMessage(text = msg, undoAction = undo)
            }.onFailure { err ->
                _feedback.value = FeedbackMessage(text = err.message ?: "Impossible de valider")
            }
        }
    }

    fun addRestockNote(articleName: String, quantityNote: String, urgent: Boolean) {
        viewModelScope.launch {
            runCatching {
                repository.createRestockNote(articleName, quantityNote, urgent)
            }.onSuccess {
                _feedback.value = FeedbackMessage(text = "Ajouté à la liste à commander")
            }.onFailure { err ->
                _feedback.value = FeedbackMessage(text = err.message ?: "Erreur d'ajout")
            }
        }
    }

    fun updateRestockNote(noteId: Long, articleName: String, quantityNote: String, urgent: Boolean) {
        viewModelScope.launch {
            runCatching {
                repository.updateRestockNote(noteId, articleName, quantityNote, urgent)
            }.onSuccess {
                _feedback.value = FeedbackMessage(text = "Article modifié")
            }.onFailure { err ->
                _feedback.value = FeedbackMessage(text = err.message ?: "Erreur de modification")
            }
        }
    }

    fun checkOffRestockNote(note: RestockNoteEntity) {
        viewModelScope.launch {
            runCatching {
                repository.markRestockPurchased(note)
            }.onSuccess { undo ->
                _feedback.value = FeedbackMessage(
                    text = "Acheté · Ajouté au Journal (${note.articleName})",
                    undoAction = undo
                )
            }
        }
    }

    fun deleteRestockNote(noteId: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteRestockNote(noteId) }
                .onSuccess { _feedback.value = FeedbackMessage(text = "Article supprimé") }
        }
    }

    fun deleteJournalEntry(entryId: Long) {
        viewModelScope.launch {
            runCatching { repository.deleteJournalEntry(entryId) }
                .onSuccess { _feedback.value = FeedbackMessage(text = "Entrée retirée du Journal") }
        }
    }

    fun performUndo(action: UndoAction) {
        viewModelScope.launch {
            runCatching {
                when (action) {
                    is UndoAction.UndoDeliveryArticle -> repository.undoArticleDelivered(action)
                    is UndoAction.UndoRestockPurchase -> repository.undoRestockPurchased(action)
                }
            }.onSuccess {
                _feedback.value = FeedbackMessage(text = "Action annulée")
            }
        }
    }
}
