package edu.kit.tm.ps.embertalk.ui.qr_code

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.kit.tm.ps.embertalk.crypto.CryptoService
import edu.kit.tm.ps.embertalk.model.EmberObserver
import edu.kit.tm.ps.embertalk.model.contacts.Contact
import edu.kit.tm.ps.embertalk.model.contacts.ContactManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.UUID

data class QrCodeUiState(
    val contact: Contact
)

@HiltViewModel(assistedFactory = QrCodeViewModel.QrCodeViewModelFactory::class)
class QrCodeViewModel @AssistedInject constructor(
    @Assisted private val contactId: UUID,
    private val contactManager: ContactManager,
    private val cryptoService: CryptoService,
) : ViewModel(), EmberObserver {

    @AssistedFactory
    interface QrCodeViewModelFactory {
        fun create(id: UUID): QrCodeViewModel
    }

    private val _uiState = MutableStateFlow(QrCodeUiState(Contact.placeholder()))
    val uiState: StateFlow<QrCodeUiState> = _uiState.asStateFlow()

    init {
        updateView()
        contactManager.register(this)
    }

    private fun updateView() {
        viewModelScope.launch {
            val contact = contactManager.get(contactId) ?: contactManager.me()
            _uiState.value = _uiState.value.copy(
                contact
            )
        }
    }

    suspend fun putKey(userId: UUID): Int {
        return try {
            cryptoService.emberKeydClient().putKey(userId)
        } catch (e: IOException) {
            Log.w(TAG, "Failed to download key ", e)
            -1
        }
    }

    fun isMe(): Boolean {
        return contactId == contactManager.me().userId
    }

    companion object {
        const val TAG = "QrCodeViewModel"
    }

    override fun notifyOfChange() {
        updateView()
    }
}