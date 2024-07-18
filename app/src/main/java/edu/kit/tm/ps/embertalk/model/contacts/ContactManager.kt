package edu.kit.tm.ps.embertalk.model.contacts

import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ContactManager(
    private val prefs: SharedPreferences,
    private val contactRepository: ContactRepository
) {
    fun contacts(): Flow<List<Contact>> = contactRepository.all()

    suspend fun add(contact: Contact) = contactRepository.insert(contact)

    suspend fun get(userId: UUID) = contactRepository.byId(userId)

    suspend fun delete(contact: Contact) = contactRepository.delete(contact)
    fun myId(): UUID {
        return UUID.fromString(prefs.getString("userId", ""))
    }
}