package edu.kit.tm.ps.embertalk.model.contacts

import kotlinx.coroutines.flow.Flow

interface ContactRepository {
    fun all(): Flow<List<Contact>>

    suspend fun insert(contact: Contact)
    suspend fun delete(contact: Contact)
}