package edu.kit.tm.ps.embertalk.model.contacts

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class OfflineContactRepository(private val contactDao: ContactDao) : ContactRepository {

    override suspend fun byId(userId: UUID) = contactDao.byId(userId)

    override fun all(): Flow<List<Contact>> = contactDao.all()

    override suspend fun insert(contact: Contact) = contactDao.insert(contact)

    override suspend fun delete(contact: Contact) = contactDao.delete(contact)
}