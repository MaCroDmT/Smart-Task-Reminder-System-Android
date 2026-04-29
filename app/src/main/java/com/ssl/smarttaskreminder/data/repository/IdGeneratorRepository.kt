package com.ssl.smarttaskreminder.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import com.ssl.smarttaskreminder.AppConstants
import kotlinx.coroutines.tasks.await

/**
 * IdGeneratorRepository
 *
 * Generates auto-incrementing sequential IDs scoped per company.
 * Uses Firestore transactions to avoid race conditions.
 *
 * Firestore structure:
 *   counters/global           → { nextCid: Int }
 *   counters/{companyId}      → { nextAid: Int, nextMid: Int, nextUid: Int, nextTid: Int }
 */
class IdGeneratorRepository {

    private val db = FirebaseFirestore.getInstance()

    /** Generates the next platform-wide Company ID (Cid). */
    suspend fun nextCid(): Int = getOrIncrementId(
        docPath    = "${AppConstants.COLLECTION_COUNTERS}/${AppConstants.COUNTER_GLOBAL}",
        field      = "nextCid",
        freedField = "freedCids"
    )

    /** Generates the next Admin ID (Aid) within the given company. */
    suspend fun nextAid(companyId: String): Int = getOrIncrementId(
        docPath    = "${AppConstants.COLLECTION_COUNTERS}/$companyId",
        field      = "nextAid",
        freedField = "freedAids"
    )

    suspend fun recycleAid(companyId: String, aid: Int) = recycleId(
        docPath    = "${AppConstants.COLLECTION_COUNTERS}/$companyId",
        freedField = "freedAids",
        idToFree   = aid
    )

    /** Generates the next Manager ID (Mid) within the given company. */
    suspend fun nextMid(companyId: String): Int = getOrIncrementId(
        docPath    = "${AppConstants.COLLECTION_COUNTERS}/$companyId",
        field      = "nextMid",
        freedField = "freedMids"
    )

    suspend fun recycleMid(companyId: String, mid: Int) = recycleId(
        docPath    = "${AppConstants.COLLECTION_COUNTERS}/$companyId",
        freedField = "freedMids",
        idToFree   = mid
    )

    /** Generates the next User ID (Uid) within the given company. */
    suspend fun nextUid(companyId: String): Int = getOrIncrementId(
        docPath    = "${AppConstants.COLLECTION_COUNTERS}/$companyId",
        field      = "nextUid",
        freedField = "freedUids"
    )

    suspend fun recycleUid(companyId: String, uid: Int) = recycleId(
        docPath    = "${AppConstants.COLLECTION_COUNTERS}/$companyId",
        freedField = "freedUids",
        idToFree   = uid
    )

    /** Generates the next Task ID (Tid) within the given company. */
    suspend fun nextTid(companyId: String): Int = getOrIncrementId(
        docPath    = "${AppConstants.COLLECTION_COUNTERS}/$companyId",
        field      = "nextTid",
        freedField = "freedTids"
    )

    suspend fun recycleTid(companyId: String, tid: Int) = recycleId(
        docPath    = "${AppConstants.COLLECTION_COUNTERS}/$companyId",
        freedField = "freedTids",
        idToFree   = tid
    )

    /**
     * Atomically reads a counter field, checking for recycled IDs first.
     */
    private suspend fun getOrIncrementId(docPath: String, field: String, freedField: String): Int {
        val docRef = db.document(docPath)
        return db.runTransaction { transaction: Transaction ->
            val snapshot = transaction.get(docRef)
            
            // 1. Check if there are any recycled IDs available
            val freedList = snapshot.get(freedField) as? List<*>
            if (freedList != null && freedList.isNotEmpty()) {
                val reusedId = (freedList.first() as Number).toInt()
                val updatedList = freedList.drop(1)
                transaction.update(docRef, freedField, updatedList)
                return@runTransaction reusedId
            }

            // 2. Otherwise increment the counter
            val currentVal = if (snapshot.exists()) {
                (snapshot.getLong(field) ?: 0L).toInt()
            } else {
                0
            }
            val newVal = currentVal + 1
            transaction.set(docRef, mapOf(field to newVal), com.google.firebase.firestore.SetOptions.merge())
            newVal
        }.await()
    }

    /**
     * Pushes a deleted ID back into the pool of available IDs for this company.
     */
    private suspend fun recycleId(docPath: String, freedField: String, idToFree: Int) {
        val docRef = db.document(docPath)
        db.runTransaction { transaction: Transaction ->
            transaction.set(
                docRef, 
                mapOf(freedField to com.google.firebase.firestore.FieldValue.arrayUnion(idToFree)),
                com.google.firebase.firestore.SetOptions.merge()
            )
        }.await()
    }
}
