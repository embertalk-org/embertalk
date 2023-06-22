package edu.kit.tm.ps.embertalk.ui.qr_code

import androidx.lifecycle.ViewModel
import edu.kit.tm.ps.embertalk.crypto.CryptoService

class QrCodeViewModel(
    private val cryptoService: CryptoService,
) : ViewModel() {

    suspend fun putKey(pubKey: String): Int {
        return cryptoService.emberKeydClient().putKey(pubKey)
    }

    fun isMyKey(pubKey: String): Boolean {
        return cryptoService.isMyKey(pubKey)
    }
}