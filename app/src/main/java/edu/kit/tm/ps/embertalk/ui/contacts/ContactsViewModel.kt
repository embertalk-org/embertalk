package edu.kit.tm.ps.embertalk.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.kit.tm.ps.embertalk.model.contacts.Contact
import edu.kit.tm.ps.embertalk.model.contacts.ContactManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class ContactsUiState(
    val contacts: List<Contact> = ArrayList()
)

class ContactsViewModel(
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
}