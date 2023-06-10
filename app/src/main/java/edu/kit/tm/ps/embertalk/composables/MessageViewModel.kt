package edu.kit.tm.ps.embertalk.composables

import androidx.lifecycle.ViewModel
import edu.kit.tm.ps.embertalk.storage.Message
import edu.kit.tm.ps.embertalk.sync.Synchronizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MessageUiState(
    val messages: List<Message> = ArrayList()
)

class MessageViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    init {
        updateMessages()
    }

    fun updateMessages() {
        _uiState.value = MessageUiState(Synchronizer.store.messages().toList())
    }

    fun saveMessage(message: Message) {
        Synchronizer.store.save(message)
        updateMessages()
    }
}