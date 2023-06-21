package edu.kit.tm.ps.embertalk.ui.settings

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.kit.tm.ps.embertalk.Preferences
import edu.kit.tm.ps.embertalk.crypto.CryptoService
import edu.kit.tm.ps.embertalk.crypto.SyncState
import edu.kit.tm.ps.embertalk.model.EmberObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val macAddress: String = "",
    val syncState: SyncState = SyncState.Initializing
)

class SettingsViewModel(
    private val prefs: SharedPreferences,
    private val cryptoService: CryptoService
) : ViewModel(), EmberObserver {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        cryptoService.register(this)
    }

    override fun notifyOfChange() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(macAddress = prefs.getString(Preferences.MAC_ADDRESS, "")!!, syncState = cryptoService.syncState())
            Log.d(TAG, "Updated Settings!")
        }
    }

    suspend fun regenerateKeys() {
        cryptoService.regenerate()
    }

    companion object {
        private const val TAG = "SettingsViewModel"
    }
}