package edu.kit.tm.ps.embertalk.ui.message_view

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.kit.tm.ps.embertalk.storage.EmberObserver
import edu.kit.tm.ps.embertalk.storage.MessageManager
import edu.kit.tm.ps.embertalk.storage.decrypted.Message
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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

    suspend fun saveMessage(message: Message) {
        messageManager.handle(message)
        updateMessages()
    }

    override fun notifyOfChange() {
        updateMessages()
        Log.d(TAG, "Got notification from repository!")
    }

    companion object {
        private const val TAG = "MessageViewModel"
    }
}