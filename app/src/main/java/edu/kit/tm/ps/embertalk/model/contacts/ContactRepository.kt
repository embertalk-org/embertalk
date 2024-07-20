package edu.kit.tm.ps.embertalk.model.contacts

import kotlinx.coroutines.flow.Flow
import java.util.UUID

interface ContactRepository {
    fun all(): Flow<List<Contact>>

    suspend fun byId(userId: UUID): Contact?
    suspend fun insert(contact: Contact)
    suspend fun delete(contact: Contact)
}