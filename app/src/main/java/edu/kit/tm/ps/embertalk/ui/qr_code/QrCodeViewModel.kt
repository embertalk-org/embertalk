package edu.kit.tm.ps.embertalk.ui.qr_code

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import edu.kit.tm.ps.embertalk.crypto.CryptoService
import edu.kit.tm.ps.embertalk.emberkeyd.EmberKeydClient

class QrCodeViewModel(
    private val prefs: SharedPreferences,
    private val cryptoService: CryptoService,
) : ViewModel() {

    fun emberKeydClient(): EmberKeydClient {
        return cryptoService.emberKeydClient(prefs.getString(edu.kit.tm.ps.embertalk.Preferences.KEY_SERVER_URL, "")!!)
    }

    fun isMyKey(pubKey: String): Boolean {
        return cryptoService.isMyKey(pubKey)
    }
}