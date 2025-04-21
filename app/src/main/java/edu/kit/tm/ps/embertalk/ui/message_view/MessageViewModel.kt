package edu.kit.tm.ps.embertalk.ui.message_view

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.kit.tm.ps.embertalk.model.EmberObserver
import edu.kit.tm.ps.embertalk.model.contacts.Contact
import edu.kit.tm.ps.embertalk.model.contacts.ContactManager
import edu.kit.tm.ps.embertalk.model.messages.MessageManager
import edu.kit.tm.ps.embertalk.model.messages.decrypted.Message
import edu.kit.tm.ps.embertalk.ui.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

data class MessageUiState(
    val contact: Contact = Contact.placeholder(),
    val messages: List<Message> = ArrayList()
)

@HiltViewModel(assistedFactory = MessageViewModel.MessageViewModelFactory::class)
class MessageViewModel @AssistedInject constructor(
    private val contactManager: ContactManager,
    private val messageManager: MessageManager,
    @Assisted private val contactId: UUID,
) : ViewModel(), EmberObserver {

    @AssistedFactory
    interface MessageViewModelFactory {
        fun create(id: UUID): MessageViewModel
    }

    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    init {
        updateView()
        messageManager.register(this)
    }

    private fun updateView() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                contact = contactManager.get(contactId)!!,
                messages = messageManager.messages().first()
                    .filter { contactId == it.recipient || contactId == it.senderUserId }
            )
        }
    }

    suspend fun saveMessage(message: String) {
        val contact = contactManager.get(contactId)!!
        messageManager.handle(message, contact.userId, contact.pubKey)
        updateView()
    }

    override fun notifyOfChange() {
        updateView()
        Log.d(TAG, "Got notification from repository!")
    }

    suspend fun deleteAll() {
        messageManager.deleteAll()
        updateView()
    }

    suspend fun deleteContact() {
        contactManager.delete(contactManager.get(contactId)!!)
        contactManager.notifyObservers()
    }

    fun isMe(senderUserId: UUID): Boolean {
        return contactId == senderUserId
    }

    fun shareContactRoute(): String {
        return Screen.qrCodeRoute(contactId)
    }

    companion object {
        private const val TAG = "MessageViewModel"
    }
}