package edu.kit.tm.ps.embertalk.model.contacts

import android.content.SharedPreferences
import edu.kit.tm.ps.embertalk.model.EmberObservable
import edu.kit.tm.ps.embertalk.model.EmberObserver
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class ContactManager(
    private val prefs: SharedPreferences,
    private val contactRepository: ContactRepository
) : EmberObservable {
    private val observers = HashSet<EmberObserver>()

    fun contacts(): Flow<List<Contact>> = contactRepository.all()

    suspend fun add(contact: Contact) = contactRepository.insert(contact)

    suspend fun get(userId: UUID) = contactRepository.byId(userId)

    suspend fun delete(contact: Contact) = contactRepository.delete(contact)
    fun myId(): UUID {
        return UUID.fromString(prefs.getString("userId", ""))
    }

    override fun register(observer: EmberObserver) {
        observers.add(observer)
    }

    override fun notifyObservers() {
        observers.forEach { it.notifyOfChange() }
    }
}