package edu.kit.tm.ps.embertalk.model.contacts

import kotlinx.coroutines.flow.Flow

class ContactManager(
    private val contactRepository: ContactRepository
) {
    fun contacts(): Flow<List<Contact>> = contactRepository.all()

    suspend fun add(contact: Contact) = contactRepository.insert(contact)

    suspend fun delete(contact: Contact) = contactRepository.delete(contact)
}