package edu.kit.tm.ps.embertalk.ui.settings

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.kit.tm.ps.embertalk.Preferences
import edu.kit.tm.ps.embertalk.crypto.CryptoService
import edu.kit.tm.ps.embertalk.model.EmberObserver
import edu.kit.tm.ps.embertalk.model.messages.MessageManager
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val keyServerUrl: String = "",
    val syncInterval: Long = 5,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: SharedPreferences,
    private val messageManager: MessageManager,
    private val cryptoService: CryptoService
) : ViewModel(), EmberObserver {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        cryptoService.register(this)
        if (!prefs.contains(Preferences.KEY_SERVER_URL)) {
            updateKeyServer("https://emberkeyd.eloque.nz")
        }
        if (!prefs.contains(Preferences.SYNC_INTERVAL)) {
            updateSyncInterval(5)
        }
        notifyOfChange()
    }

    override fun notifyOfChange() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                keyServerUrl = prefs.getString(Preferences.KEY_SERVER_URL, "")!!,
            )
            Log.d(TAG, "Updated Settings!")
        }
    }

    fun updateKeyServer(url: String) {
        prefs.edit().putString(Preferences.KEY_SERVER_URL, url).apply()
        notifyOfChange()
    }

    fun updateSyncInterval(interval: Long) {
        prefs.edit().putLong(Preferences.SYNC_INTERVAL, interval).apply()
        notifyOfChange()
    }

    suspend fun deleteAll() {
        messageManager.deleteAll()
    }

    suspend fun regenerateKeys() {
        cryptoService.regenerate()
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}