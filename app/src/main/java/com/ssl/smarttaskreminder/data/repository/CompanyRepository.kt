package com.ssl.smarttaskreminder.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.ssl.smarttaskreminder.AppConstants
import com.ssl.smarttaskreminder.data.model.Company
import kotlinx.coroutines.tasks.await

/**
 * CompanyRepository — CRUD for companies collection.
 * Only accessible by Super Admin. No companyId filter needed (this IS the top-level collection).
 */
class CompanyRepository {

    private val db = FirebaseFirestore.getInstance()
    private val idGen = IdGeneratorRepository()

    /**
     * Creates a new company. Auto-generates a unique Cid using IdGeneratorRepository.
     * Returns the newly created Company.
     */
    suspend fun createCompany(
        name: String,
        industry: String,
        status: String
    ): Company {
        // Generate sequential CID
        val cidInt = idGen.nextCid()
        val cid = "CID$cidInt"

        // Auto-generate slug from name
        val slug = name.trim().lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .replace(Regex("\\s+"), "-")

        val company = Company(
            cid       = cid,
            name      = name.trim(),
            slug      = slug,
            industry  = industry.trim(),
            status    = status,
            createdAt = Timestamp.now()
        )

        // Use cid as Firestore document ID for easy lookup
        db.collection(AppConstants.COLLECTION_COMPANIES)
            .document(cid)
            .set(company.toMap())
            .await()

        return company
    }

    /** Returns all companies (Super Admin only). */
    suspend fun getAllCompanies(): List<Company> {
        val snapshot = db.collection(AppConstants.COLLECTION_COMPANIES)
            .get().await()
        return snapshot.documents.mapNotNull { doc ->
            doc.toObject(Company::class.java)?.copy(cid = doc.id)
        }
    }

    /** Returns a single company by cid. */
    suspend fun getCompany(cid: String): Company? {
        val doc = db.collection(AppConstants.COLLECTION_COMPANIES)
            .document(cid).get().await()
        return doc.toObject(Company::class.java)?.copy(cid = doc.id)
    }

    /** Updates company name, industry, or status. */
    suspend fun updateCompany(cid: String, name: String, industry: String, status: String) {
        db.collection(AppConstants.COLLECTION_COMPANIES)
            .document(cid)
            .update(mapOf(
                "name"     to name,
                "industry" to industry,
                "status"   to status
            ))
            .await()
    }

    /** Toggles company status between active/inactive. */
    suspend fun toggleStatus(cid: String, newStatus: String) {
        db.collection(AppConstants.COLLECTION_COMPANIES)
            .document(cid)
            .update("status", newStatus)
            .await()
    }
}
