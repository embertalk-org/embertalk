package edu.kit.tm.ps.embertalk.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.kit.tm.ps.embertalk.crypto.CryptoService
import edu.kit.tm.ps.embertalk.model.contacts.Contact
import edu.kit.tm.ps.embertalk.model.contacts.ContactManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

data class ContactsUiState(
    val contacts: List<Contact> = ArrayList()
)

class ContactsViewModel(
    private val cryptoService: CryptoService,
    private val contactManager: ContactManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        updateContacts()
    }

    private fun updateContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(contacts = contactManager.contacts().first())
        }
    }

    suspend fun add(contact: Contact) {
        contactManager.add(contact)
        updateContacts()
    }

    suspend fun get(userId: UUID): Contact {
        return contactManager.get(userId)
    }

    suspend fun downloadKey(userId: UUID): String? {
        return try {
            cryptoService.emberKeydClient().downloadKey(userId)
        } catch (e: IOException) {
            null
        }
    }

    suspend fun delete(contact: Contact) {
        contactManager.delete(contact)
        updateContacts()
    }

    fun myId(): UUID {
        return contactManager.myId()
    }
}