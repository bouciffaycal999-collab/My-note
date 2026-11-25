package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.Date

class DepotRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(expectedUserId: String): String {
        val uid = auth.currentUser?.uid
            ?: throw IllegalStateException("Vous devez être connecté avec Google.")
        check(uid == expectedUserId) { "Session utilisateur modifiée ; opération annulée." }
        return uid
    }

    private fun deliveryCol(uid: String) =
        db.collection("users").document(uid).collection("deliveryNotes")

    private fun restockCol(uid: String) =
        db.collection("users").document(uid).collection("restockNotes")

    private fun journalCol(uid: String) =
        db.collection("users").document(uid).collection("journalEntries")

    fun observeDeliveryNotes(userId: String): Flow<List<DeliveryNote>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/deliveryNotes"
        emitAll(
            deliveryCol(uid)
                .snapshots()
                .map { snapshot ->
                    snapshot
                        .toObjects(DeliveryNote::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeRestockNotes(userId: String): Flow<List<RestockNote>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/restockNotes"
        emitAll(
            restockCol(uid)
                .snapshots()
                .map { snapshot ->
                    snapshot
                        .toObjects(RestockNote::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedWith(
                            compareByDescending<RestockNote> { it.urgent }
                                .thenByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE }
                        )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observeJournalEntries(userId: String): Flow<List<JournalEntry>> = flow {
        val uid = requireUserId(userId)
        val path = "users/$uid/journalEntries"
        emitAll(
            journalCol(uid)
                .snapshots()
                .map { snapshot ->
                    snapshot
                        .toObjects(JournalEntry::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt?.seconds ?: Long.MAX_VALUE }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun createDeliveryNote(
        userId: String,
        clientName: String,
        clientPhone: String,
        clientAddress: String,
        items: List<DeliveryLineItem>
    ): Result<String> {
        var docPath = "users/$userId/deliveryNotes"
        return try {
            val uid = requireUserId(userId)
            val cleanItems = items
                .filter { it.text.isNotBlank() }
                .take(50)
                .map { it.copy(text = it.text.trim().take(200)) }
            require(cleanItems.isNotEmpty()) { "Ajoutez au moins un article à livrer." }

            val docRef = deliveryCol(uid).document()
            docPath = docRef.path
            val payload = mapOf(
                "userId" to uid,
                "clientName" to clientName.trim().take(120),
                "clientPhone" to clientPhone.trim().take(40),
                "clientAddress" to clientAddress.trim().take(200),
                "items" to cleanItems.map { it.toFirestoreMap() },
                "completed" to cleanItems.all { it.delivered },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateDeliveryNote(
        userId: String,
        noteId: String,
        clientName: String,
        clientPhone: String,
        clientAddress: String,
        items: List<DeliveryLineItem>
    ): Result<Unit> {
        val docPath = "users/$userId/deliveryNotes/$noteId"
        return try {
            val uid = requireUserId(userId)
            require(noteId.isNotBlank()) { "Identifiant de note manquant." }
            val cleanItems = items
                .filter { it.text.isNotBlank() }
                .take(50)
                .map { it.copy(text = it.text.trim().take(200)) }
            require(cleanItems.isNotEmpty()) { "La note doit contenir au moins un article." }

            deliveryCol(uid).document(noteId).update(
                mapOf(
                    "clientName" to clientName.trim().take(120),
                    "clientPhone" to clientPhone.trim().take(40),
                    "clientAddress" to clientAddress.trim().take(200),
                    "items" to cleanItems.map { it.toFirestoreMap() },
                    "completed" to cleanItems.all { it.delivered },
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteDeliveryNote(userId: String, noteId: String): Result<Unit> {
        val docPath = "users/$userId/deliveryNotes/$noteId"
        return try {
            val uid = requireUserId(userId)
            require(noteId.isNotBlank()) { "Identifiant de note manquant." }
            deliveryCol(uid).document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    /**
     * Atomically checks off one paid article on a client delivery note and logs it in today's Journal.
     * If all items in the note are now delivered, marks the note as completed so it clears the active list.
     */
    suspend fun markDeliveryLineDelivered(
        userId: String,
        note: DeliveryNote,
        lineId: String
    ): Result<UndoAction.UndoDeliveryLine> {
        val docPath = "users/$userId/deliveryNotes/${note.id}"
        return try {
            val uid = requireUserId(userId)
            require(note.id.isNotBlank()) { "Identifiant de note manquant." }
            val targetLine = note.items.firstOrNull { it.lineId == lineId }
                ?: throw IllegalArgumentException("Article introuvable dans cette note.")

            val nowMs = System.currentTimeMillis()
            val updatedItems = note.items.map { item ->
                if (item.lineId == lineId) item.copy(delivered = true, deliveredAtMs = nowMs) else item
            }
            val nowCompleted = updatedItems.all { it.delivered }

            val noteRef = deliveryCol(uid).document(note.id)
            val journalRef = journalCol(uid).document()
            val batch = db.batch()

            batch.update(
                noteRef,
                mapOf(
                    "items" to updatedItems.map { it.toFirestoreMap() },
                    "completed" to nowCompleted,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                journalRef,
                mapOf(
                    "userId" to uid,
                    "entryType" to JournalEntryType.DELIVERY.name,
                    "articleText" to targetLine.text.trim().take(240),
                    "quantityNote" to "",
                    "clientName" to note.clientName.trim().take(120),
                    "clientPhone" to note.clientPhone.trim().take(40),
                    "clientAddress" to note.clientAddress.trim().take(200),
                    "sourceNoteId" to note.id.take(128),
                    "sourceLineId" to lineId.take(64),
                    "dayKey" to formatDayKey(Date()),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            Result.success(
                UndoAction.UndoDeliveryLine(
                    noteId = note.id,
                    previousItems = note.items,
                    previousCompleted = note.completed,
                    journalEntryId = journalRef.id
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun undoDeliveryLineDelivered(
        userId: String,
        undo: UndoAction.UndoDeliveryLine
    ): Result<Unit> {
        val docPath = "users/$userId/deliveryNotes/${undo.noteId}"
        return try {
            val uid = requireUserId(userId)
            val batch = db.batch()
            batch.update(
                deliveryCol(uid).document(undo.noteId),
                mapOf(
                    "items" to undo.previousItems.map { it.toFirestoreMap() },
                    "completed" to undo.previousCompleted,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.delete(journalCol(uid).document(undo.journalEntryId))
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun createRestockNote(
        userId: String,
        articleName: String,
        quantityNote: String,
        urgent: Boolean
    ): Result<String> {
        var docPath = "users/$userId/restockNotes"
        return try {
            val uid = requireUserId(userId)
            val cleanArticle = articleName.trim().take(200)
            require(cleanArticle.isNotEmpty()) { "Indiquez le nom de l'article à commander." }

            val docRef = restockCol(uid).document()
            docPath = docRef.path
            docRef.set(
                mapOf(
                    "userId" to uid,
                    "articleName" to cleanArticle,
                    "quantityNote" to quantityNote.trim().take(100),
                    "urgent" to urgent,
                    "purchased" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateRestockNote(
        userId: String,
        noteId: String,
        articleName: String,
        quantityNote: String,
        urgent: Boolean
    ): Result<Unit> {
        val docPath = "users/$userId/restockNotes/$noteId"
        return try {
            val uid = requireUserId(userId)
            require(noteId.isNotBlank()) { "Identifiant manquant." }
            val cleanArticle = articleName.trim().take(200)
            require(cleanArticle.isNotEmpty()) { "Le nom de l'article ne peut pas être vide." }

            restockCol(uid).document(noteId).update(
                mapOf(
                    "articleName" to cleanArticle,
                    "quantityNote" to quantityNote.trim().take(100),
                    "urgent" to urgent,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteRestockNote(userId: String, noteId: String): Result<Unit> {
        val docPath = "users/$userId/restockNotes/$noteId"
        return try {
            val uid = requireUserId(userId)
            require(noteId.isNotBlank()) { "Identifiant manquant." }
            restockCol(uid).document(noteId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    /**
     * Atomically checks off a restock note as purchased (removes it from pending list)
     * and appends a PURCHASE entry in today's Journal.
     */
    suspend fun markRestockPurchased(
        userId: String,
        note: RestockNote
    ): Result<UndoAction.UndoRestockPurchase> {
        val docPath = "users/$userId/restockNotes/${note.id}"
        return try {
            val uid = requireUserId(userId)
            require(note.id.isNotBlank()) { "Identifiant manquant." }

            val noteRef = restockCol(uid).document(note.id)
            val journalRef = journalCol(uid).document()
            val batch = db.batch()

            batch.update(
                noteRef,
                mapOf(
                    "purchased" to true,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.set(
                journalRef,
                mapOf(
                    "userId" to uid,
                    "entryType" to JournalEntryType.PURCHASE.name,
                    "articleText" to note.articleName.trim().take(240),
                    "quantityNote" to note.quantityNote.trim().take(100),
                    "clientName" to "",
                    "clientPhone" to "",
                    "clientAddress" to "",
                    "sourceNoteId" to note.id.take(128),
                    "sourceLineId" to "",
                    "dayKey" to formatDayKey(Date()),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            )
            batch.commit().await()

            Result.success(
                UndoAction.UndoRestockPurchase(
                    noteId = note.id,
                    journalEntryId = journalRef.id
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun undoRestockPurchased(
        userId: String,
        undo: UndoAction.UndoRestockPurchase
    ): Result<Unit> {
        val docPath = "users/$userId/restockNotes/${undo.noteId}"
        return try {
            val uid = requireUserId(userId)
            val batch = db.batch()
            batch.update(
                restockCol(uid).document(undo.noteId),
                mapOf(
                    "purchased" to false,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
            batch.delete(journalCol(uid).document(undo.journalEntryId))
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteJournalEntry(userId: String, entryId: String): Result<Unit> {
        val docPath = "users/$userId/journalEntries/$entryId"
        return try {
            val uid = requireUserId(userId)
            require(entryId.isNotBlank()) { "Identifiant journal manquant." }
            journalCol(uid).document(entryId).delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}
