package edu.kit.tm.ps.embertalk.composables.message_view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.kit.tm.ps.embertalk.storage.Message
import edu.kit.tm.ps.embertalk.storage.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class MessageUiState(
    val messages: List<Message> = ArrayList()
)

class MessageViewModel(private val messageRepository: MessageRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MessageUiState())
    val uiState: StateFlow<MessageUiState> = _uiState.asStateFlow()

    init {
        updateMessages()
    }

    fun updateMessages() {
        viewModelScope.launch {
            _uiState.value = MessageUiState(messageRepository.all().first())
        }
    }

    suspend fun saveMessage(message: Message) {
        messageRepository.insert(message)
        updateMessages()
    }
}