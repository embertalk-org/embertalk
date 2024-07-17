package edu.kit.tm.ps.embertalk.ui.message_view

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.kit.tm.ps.embertalk.model.EmberObserver
import edu.kit.tm.ps.embertalk.model.messages.MessageManager
import edu.kit.tm.ps.embertalk.model.messages.decrypted.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

data class MessageUiState(
    val messages: List<Message> = ArrayList()
)

class MessageViewModel(
    private val messageManager: MessageManager,
) : ViewModel(), EmberObserver {

    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    init {
        updateMessages()
        messageManager.register(this)
    }

    private fun updateMessages() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(messages = messageManager.messages().first())
        }
    }

    suspend fun saveMessage(message: String, recipient: UUID, publicKey: String) {
        messageManager.handle(message, recipient, publicKey)
        updateMessages()
    }

    override fun notifyOfChange() {
        updateMessages()
        Log.d(TAG, "Got notification from repository!")
    }

    suspend fun deleteAll() {
        messageManager.deleteAll()
        updateMessages()
    }

    companion object {
        private const val TAG = "MessageViewModel"
    }
}